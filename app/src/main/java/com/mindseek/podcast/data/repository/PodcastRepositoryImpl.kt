package com.mindseek.podcast.data.repository

import com.mindseek.podcast.data.local.dao.EpisodeDao
import com.mindseek.podcast.data.local.dao.PodcastDao
import com.mindseek.podcast.data.local.entity.Episode
import com.mindseek.podcast.data.local.entity.Podcast
import com.mindseek.podcast.data.mapper.toEpisode
import com.mindseek.podcast.data.mapper.toPodcast
import com.mindseek.podcast.data.remote.ApiServiceWrapper
import com.mindseek.podcast.data.remote.NetworkResult
import com.mindseek.podcast.data.remote.api.NioRadioApi
import com.mindseek.podcast.data.remote.dto.EpisodeDto
import com.mindseek.podcast.data.remote.dto.PodcastDto
import com.mindseek.podcast.domain.repository.PodcastRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PodcastRepositoryImpl @Inject constructor(
    private val podcastDao: PodcastDao,
    private val episodeDao: EpisodeDao,
    private val apiServiceWrapper: ApiServiceWrapper,
    private val nioRadioApi: NioRadioApi
) : PodcastRepository {

    companion object {
        /** Preset album IDs as seed data to ensure content before discovery completes. */
        val SEED_ALBUM_IDS: List<Long> = listOf(
            5L, 23L, 18L, 148L, 11L, 120L, 121L, 136L, 103L, 147L, 149L, 547L
        )

        /** Batch size for concurrent album probing during discovery. */
        private const val DISCOVERY_BATCH_SIZE = 20

        /** Maximum album ID to probe during discovery. */
        private const val DISCOVERY_MAX_ID = 2000L
    }

    override fun getAllPodcasts(): Flow<List<Podcast>> {
        return podcastDao.getAllPodcasts()
    }

    override suspend fun getPodcastById(id: String): Podcast? {
        return podcastDao.getPodcastById(id)
    }

    /**
     * Discovers active albums on Nio Radio by probing album IDs.
     * Starts with seed albums for instant content, then probes IDs 1-2000 in
     * concurrent batches of 20.
     *
     * @param onProgress Optional callback receiving (discovered, total) for UI updates.
     * @return The list of discovered Podcast entities.
     */
    suspend fun discoverAlbums(
        onProgress: ((discovered: Int, total: Int) -> Unit)? = null
    ): List<Podcast> = coroutineScope {
        val discoveredAlbums = mutableListOf<Podcast>()
        val seenAlbumIds = mutableSetOf<Long>()

        // Phase 1: Fetch seed albums (instant content)
        for (albumId in SEED_ALBUM_IDS) {
            try {
                val response = nioRadioApi.getEpisodeList(
                    albumId = albumId,
                    sortType = 2,
                    pageNum = 1,
                    pageSize = 1
                )
                if (response.result.totalCount > 0 && response.result.dataList.isNotEmpty()) {
                    val firstEpisode = response.result.dataList.first()
                    if (seenAlbumIds.add(albumId)) {
                        val podcast = firstEpisode.toPodcast()
                        discoveredAlbums.add(podcast)
                        onProgress?.invoke(discoveredAlbums.size, discoveredAlbums.size)
                    }
                }
            } catch (_: Exception) {
                // Album not found or network error — skip
            }
        }

        // Persist seed results immediately
        if (discoveredAlbums.isNotEmpty()) {
            podcastDao.insertPodcasts(discoveredAlbums)
        }

        // Phase 2: Probe remaining album IDs in batches
        val idsToProbe = (1L..DISCOVERY_MAX_ID).filter { it !in SEED_ALBUM_IDS }.toList()

        idsToProbe.chunked(DISCOVERY_BATCH_SIZE).forEach { batch ->
            val batchResults = batch.map { albumId ->
                async {
                    try {
                        val response = nioRadioApi.getEpisodeList(
                            albumId = albumId,
                            sortType = 2,
                            pageNum = 1,
                            pageSize = 1
                        )
                        if (response.result.totalCount > 0 && response.result.dataList.isNotEmpty()) {
                            response.result.dataList.first()
                        } else {
                            null
                        }
                    } catch (_: Exception) {
                        null
                    }
                }
            }.awaitAll().filterNotNull()

            for (episodeDto in batchResults) {
                if (seenAlbumIds.add(episodeDto.albumId)) {
                    discoveredAlbums.add(episodeDto.toPodcast())
                }
            }

            // Persist batch results
            if (batchResults.isNotEmpty()) {
                val newPodcasts = batchResults
                    .filter { seenAlbumIds.contains(it.albumId) }
                    .distinctBy { it.albumId }
                    .map { it.toPodcast() }
                if (newPodcasts.isNotEmpty()) {
                    podcastDao.insertPodcasts(newPodcasts)
                }
            }

            onProgress?.invoke(discoveredAlbums.size, idsToProbe.size + SEED_ALBUM_IDS.size)
        }

        discoveredAlbums
    }

    /**
     * Fetches episodes for a specific album from Nio Radio.
     *
     * @param albumId The Nio Radio album ID.
     * @param page Page number (1-based).
     * @param pageSize Number of episodes per page.
     * @return List of Episode entities from this album page.
     */
    suspend fun getAlbumEpisodes(
        albumId: Long,
        page: Int = 1,
        pageSize: Int = 20
    ): List<Episode> {
        val response = nioRadioApi.getEpisodeList(
            albumId = albumId,
            sortType = 2,
            pageNum = page,
            pageSize = pageSize
        )
        val episodes = response.result.dataList.map { it.toEpisode() }
        if (episodes.isNotEmpty()) {
            episodeDao.insertEpisodes(episodes)
        }
        return episodes
    }

    override suspend fun getRecommendedPodcasts(page: Int): List<Podcast> {
        return try {
            val result = apiServiceWrapper.getRecommendedPodcasts(page)
            when (result) {
                is NetworkResult.Success -> {
                    val localPodcasts = result.data.items.map { it.toEntity() }
                    
                    // Cache the podcasts locally
                    podcastDao.insertPodcasts(localPodcasts)
                    
                    localPodcasts
                }
                is NetworkResult.Error -> {
                    // Fallback to local data if network fails
                    podcastDao.getAllPodcasts().first()
                }
                is NetworkResult.Loading -> {
                    // Return local data while loading
                    podcastDao.getAllPodcasts().first()
                }
            }
        } catch (e: Exception) {
            // Fallback to local data if network fails
            podcastDao.getAllPodcasts().first()
        }
    }

    override suspend fun searchPodcasts(query: String, page: Int): List<Podcast> {
        return try {
            val result = apiServiceWrapper.searchPodcasts(query, page)
            when (result) {
                is NetworkResult.Success -> {
                    val localPodcasts = result.data.results.map { it.toEntity() }
                    
                    // Cache search results
                    podcastDao.insertPodcasts(localPodcasts)
                    
                    localPodcasts
                }
                is NetworkResult.Error -> {
                    // Fallback to local search if network fails
                    podcastDao.searchPodcasts(query).first()
                }
                is NetworkResult.Loading -> {
                    // Return local search results while loading
                    podcastDao.searchPodcasts(query).first()
                }
            }
        } catch (e: Exception) {
            // Fallback to local search if network fails
            podcastDao.searchPodcasts(query).first()
        }
    }

    override suspend fun getEpisodesByPodcastId(podcastId: String): Flow<List<Episode>> {
        // Try to refresh episodes from Nio Radio first
        try {
            val albumId = podcastId.toLongOrNull()
            if (albumId != null) {
                getAlbumEpisodes(albumId = albumId, page = 1, pageSize = 20)
            }
        } catch (_: Exception) {
            // Continue with local data if remote fails
        }
        
        // Also try the generic API as fallback
        try {
            val result = apiServiceWrapper.getEpisodesByPodcastId(podcastId)
            when (result) {
                is NetworkResult.Success -> {
                    val localEpisodes = result.data.items.map { it.toEntity() }
                    episodeDao.insertEpisodes(localEpisodes)
                }
                is NetworkResult.Error -> {
                    // Continue with local data if remote fails
                }
                is NetworkResult.Loading -> {
                    // Continue with local data while loading
                }
            }
        } catch (e: Exception) {
            // Continue with local data if remote fails
        }
        
        return episodeDao.getEpisodesByPodcastId(podcastId)
    }

    override suspend fun refreshPodcastData(podcastId: String) {
        try {
            // Refresh podcast info
            val podcastResult = apiServiceWrapper.getPodcastById(podcastId)
            when (podcastResult) {
                is NetworkResult.Success -> {
                    val localPodcast = podcastResult.data.toEntity()
                    
                    // Preserve local subscription status
                    val existingPodcast = podcastDao.getPodcastById(podcastId)
                    val updatedPodcast = localPodcast.copy(
                        lastUpdated = System.currentTimeMillis()
                    )
                    
                    podcastDao.insertPodcast(updatedPodcast)
                }
                is NetworkResult.Error -> {
                    throw Exception("Failed to refresh podcast: ${podcastResult.message}")
                }
                is NetworkResult.Loading -> {
                    // Continue with episodes refresh
                }
            }
            
            // Refresh episodes
            val episodesResult = apiServiceWrapper.getEpisodesByPodcastId(podcastId)
            when (episodesResult) {
                is NetworkResult.Success -> {
                    val localEpisodes = episodesResult.data.items.map { it.toEntity() }
                    episodeDao.insertEpisodes(localEpisodes)
                }
                is NetworkResult.Error -> {
                    throw Exception("Failed to refresh episodes: ${episodesResult.message}")
                }
                is NetworkResult.Loading -> {
                    // Loading state handled
                }
            }
            
        } catch (e: Exception) {
            throw e
        }
    }

    override fun getDownloadedEpisodes(): Flow<List<Episode>> {
        return episodeDao.getDownloadedEpisodes()
    }

    // Extension functions to convert DTOs to entities
    private fun PodcastDto.toEntity(): Podcast {
        return Podcast(
            id = id,
            title = title,
            description = description,
            imageUrl = imageUrl,
            author = author,
            category = category,
            isSubscribed = false,
            lastUpdated = System.currentTimeMillis()
        )
    }

    private fun EpisodeDto.toEntity(): Episode {
        return Episode(
            id = id,
            podcastId = podcastId,
            title = title,
            description = description,
            audioUrl = audioUrl,
            imageUrl = imageUrl,
            duration = duration,
            publishDate = publishDate,
            source = "",
            author = "",
            fileSize = fileSize,
            isDownloaded = false,
            localPath = null
        )
    }
}