package com.mindseek.podcast.data.remote

import com.mindseek.podcast.data.remote.api.PodcastApiService
import com.mindseek.podcast.data.remote.dto.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wrapper class that provides safe API calls with error handling and retry logic
 */
@Singleton
class ApiServiceWrapper @Inject constructor(
    private val podcastApiService: PodcastApiService
) {
    
    // Podcast API calls
    suspend fun getRecommendedPodcasts(
        page: Int = 1,
        limit: Int = 20,
        category: String? = null
    ): NetworkResult<PaginatedResponse<PodcastDto>> {
        return safeApiCallWithResponse {
            podcastApiService.getRecommendedPodcasts(page, limit, category)
        }
    }
    
    suspend fun getTrendingPodcasts(
        page: Int = 1,
        limit: Int = 20,
        timeframe: String = "week"
    ): NetworkResult<PaginatedResponse<PodcastDto>> {
        return safeApiCallWithResponse {
            podcastApiService.getTrendingPodcasts(page, limit, timeframe)
        }
    }
    
    suspend fun getCategories(): NetworkResult<List<String>> {
        return safeApiCallWithResponse {
            podcastApiService.getCategories()
        }
    }
    
    suspend fun getPodcastsByCategory(
        category: String,
        page: Int = 1,
        limit: Int = 20,
        sort: String = "popular"
    ): NetworkResult<PaginatedResponse<PodcastDto>> {
        return safeApiCallWithResponse {
            podcastApiService.getPodcastsByCategory(category, page, limit, sort)
        }
    }
    
    suspend fun searchPodcasts(
        query: String,
        page: Int = 1,
        limit: Int = 20,
        category: String? = null,
        sort: String = "relevance"
    ): NetworkResult<SearchResponse<PodcastDto>> {
        return safeApiCallWithResponse {
            podcastApiService.searchPodcasts(query, page, limit, category, sort)
        }
    }
    
    suspend fun searchEpisodes(
        query: String,
        page: Int = 1,
        limit: Int = 20,
        podcastId: String? = null
    ): NetworkResult<SearchResponse<EpisodeDto>> {
        return safeApiCallWithResponse {
            podcastApiService.searchEpisodes(query, page, limit, podcastId)
        }
    }
    
    suspend fun getPodcastById(id: String): NetworkResult<PodcastDto> {
        return safeApiCallWithResponse {
            podcastApiService.getPodcastById(id)
        }.let { result ->
            when (result) {
                is NetworkResult.Success -> {
                    result.data.data?.let { NetworkResult.Success(it) }
                        ?: NetworkResult.Error(NetworkException.ParseError("Podcast data is null"))
                }
                is NetworkResult.Error -> NetworkResult.Error(result.exception, result.message)
                is NetworkResult.Loading -> NetworkResult.Loading()
            }
        }
    }
    
    suspend fun getEpisodesByPodcastId(
        podcastId: String,
        page: Int = 1,
        limit: Int = 50,
        sort: String = "recent"
    ): NetworkResult<PaginatedResponse<EpisodeDto>> {
        return safeApiCallWithResponse {
            podcastApiService.getEpisodesByPodcastId(podcastId, page, limit, sort)
        }
    }
    
    suspend fun getEpisodeById(id: String): NetworkResult<EpisodeDto> {
        return safeApiCallWithResponse {
            podcastApiService.getEpisodeById(id)
        }.let { result ->
            when (result) {
                is NetworkResult.Success -> {
                    result.data.data?.let { NetworkResult.Success(it) }
                        ?: NetworkResult.Error(NetworkException.ParseError("Episode data is null"))
                }
                is NetworkResult.Error -> NetworkResult.Error(result.exception, result.message)
                is NetworkResult.Loading -> NetworkResult.Loading()
            }
        }
    }

    suspend fun getPlayHistory(
        page: Int = 1,
        limit: Int = 50
    ): NetworkResult<PaginatedResponse<EpisodeDto>> {
        return safeApiCallWithResponse {
            podcastApiService.getPlayHistory(page, limit)
        }
    }
    
    suspend fun recordPlayHistory(
        episodeId: String,
        playData: Map<String, Any>
    ): NetworkResult<Unit> {
        return safeApiCallWithResponse {
            podcastApiService.recordPlayHistory(episodeId, playData)
        }.let { result ->
            when (result) {
                is NetworkResult.Success -> NetworkResult.Success(Unit)
                is NetworkResult.Error -> NetworkResult.Error(result.exception, result.message)
                is NetworkResult.Loading -> NetworkResult.Loading()
            }
        }
    }
    
    suspend fun getFavoriteEpisodes(
        page: Int = 1,
        limit: Int = 50
    ): NetworkResult<PaginatedResponse<EpisodeDto>> {
        return safeApiCallWithResponse {
            podcastApiService.getFavoriteEpisodes(page, limit)
        }
    }
    
    suspend fun addToFavorites(episodeId: String): NetworkResult<Unit> {
        return safeApiCallWithResponse {
            podcastApiService.addToFavorites(episodeId)
        }.let { result ->
            when (result) {
                is NetworkResult.Success -> NetworkResult.Success(Unit)
                is NetworkResult.Error -> NetworkResult.Error(result.exception, result.message)
                is NetworkResult.Loading -> NetworkResult.Loading()
            }
        }
    }
    
    suspend fun removeFromFavorites(episodeId: String): NetworkResult<Unit> {
        return safeApiCallWithResponse {
            podcastApiService.removeFromFavorites(episodeId)
        }.let { result ->
            when (result) {
                is NetworkResult.Success -> NetworkResult.Success(Unit)
                is NetworkResult.Error -> NetworkResult.Error(result.exception, result.message)
                is NetworkResult.Loading -> NetworkResult.Loading()
            }
        }
    }
}