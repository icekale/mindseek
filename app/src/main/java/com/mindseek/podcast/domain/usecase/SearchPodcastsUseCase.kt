package com.mindseek.podcast.domain.usecase

import com.mindseek.podcast.data.mapper.toDomain
import com.mindseek.podcast.domain.model.PodcastDomain
import com.mindseek.podcast.domain.model.Resource
import com.mindseek.podcast.domain.repository.PodcastRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case for searching podcasts
 */
class SearchPodcastsUseCase @Inject constructor(
    private val podcastRepository: PodcastRepository
) {
    operator fun invoke(query: String, page: Int = 1): Flow<Resource<List<PodcastDomain>>> = flow {
        try {
            emit(Resource.Loading())
            
            if (query.isBlank()) {
                emit(Resource.Success(emptyList()))
                return@flow
            }
            
            val podcasts = podcastRepository.searchPodcasts(query.trim(), page)
            val domainPodcasts = podcasts.map { it.toDomain() }
            emit(Resource.Success(domainPodcasts))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "搜索失败"))
        }
    }
}