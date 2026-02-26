package com.mindseek.podcast.domain.usecase

import com.mindseek.podcast.data.mapper.toDomain
import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.domain.model.Resource
import com.mindseek.podcast.domain.repository.PodcastRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case for searching episodes
 */
class SearchEpisodesUseCase @Inject constructor(
    private val podcastRepository: PodcastRepository
) {
    operator fun invoke(query: String, page: Int = 1): Flow<Resource<List<EpisodeDomain>>> = flow {
        try {
            emit(Resource.Loading())
            
            if (query.isBlank()) {
                emit(Resource.Success(emptyList()))
                return@flow
            }
            
            // For now, we'll search through episodes of all podcasts
            // In a real implementation, this would be a separate API call
            val episodes = searchEpisodesLocally(query.trim())
            emit(Resource.Success(episodes))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "搜索节目失败"))
        }
    }
    
    private suspend fun searchEpisodesLocally(query: String): List<EpisodeDomain> {
        // This is a simplified implementation
        // In a real app, this would use a proper search API or database query
        return emptyList()
    }
}