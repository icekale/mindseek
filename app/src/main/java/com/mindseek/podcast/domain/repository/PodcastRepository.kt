package com.mindseek.podcast.domain.repository

import com.mindseek.podcast.data.local.entity.Episode
import com.mindseek.podcast.data.local.entity.Podcast
import kotlinx.coroutines.flow.Flow

interface PodcastRepository {
    fun getAllPodcasts(): Flow<List<Podcast>>
    suspend fun getPodcastById(id: String): Podcast?
    suspend fun getRecommendedPodcasts(page: Int = 1): List<Podcast>
    suspend fun searchPodcasts(query: String, page: Int = 1): List<Podcast>
    suspend fun getEpisodesByPodcastId(podcastId: String): Flow<List<Episode>>
    suspend fun refreshPodcastData(podcastId: String)
    fun getDownloadedEpisodes(): Flow<List<Episode>>
}