package com.mindseek.podcast.data.repository

import com.mindseek.podcast.data.local.dao.EpisodeDao
import com.mindseek.podcast.data.local.dao.PodcastDao
import com.mindseek.podcast.data.local.entity.Episode
import com.mindseek.podcast.data.local.entity.Podcast
import com.mindseek.podcast.data.remote.ApiServiceWrapper
import com.mindseek.podcast.data.remote.NetworkResult
import com.mindseek.podcast.data.remote.dto.EpisodeDto
import com.mindseek.podcast.data.remote.dto.PodcastDto
import com.mindseek.podcast.domain.repository.PodcastRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PodcastRepositoryImpl @Inject constructor(
    private val podcastDao: PodcastDao,
    private val episodeDao: EpisodeDao,
    private val apiServiceWrapper: ApiServiceWrapper
) : PodcastRepository {

    override fun getAllPodcasts(): Flow<List<Podcast>> {
        return podcastDao.getAllPodcasts()
    }

    override fun getSubscribedPodcasts(): Flow<List<Podcast>> {
        return podcastDao.getSubscribedPodcasts()
    }

    override suspend fun getPodcastById(id: String): Podcast? {
        return podcastDao.getPodcastById(id)
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

    override suspend fun subscribeToPodcast(podcastId: String) {
        podcastDao.updateSubscriptionStatus(podcastId, true)
        
        // Refresh podcast data when subscribing
        try {
            refreshPodcastData(podcastId)
        } catch (e: Exception) {
            // Subscription status is already updated locally, continue silently
        }
    }

    override suspend fun unsubscribeFromPodcast(podcastId: String) {
        podcastDao.updateSubscriptionStatus(podcastId, false)
    }

    override suspend fun getEpisodesByPodcastId(podcastId: String): Flow<List<Episode>> {
        // Try to refresh episodes from remote first
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
                        isSubscribed = existingPodcast?.isSubscribed ?: false,
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
            isSubscribed = false, // Will be updated based on local state
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
            duration = duration,
            publishDate = publishDate,
            isDownloaded = false,
            localPath = null
        )
    }
}