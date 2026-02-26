package com.mindseek.podcast.domain.usecase

import com.mindseek.podcast.data.mapper.toDomain
import com.mindseek.podcast.domain.model.PodcastDomain
import com.mindseek.podcast.domain.repository.PodcastRepository
import javax.inject.Inject

class GetRecommendedPodcastsUseCase @Inject constructor(
    private val podcastRepository: PodcastRepository
) {
    suspend operator fun invoke(page: Int = 1): List<PodcastDomain> {
        return try {
            val podcasts = podcastRepository.getRecommendedPodcasts(page)
            podcasts.map { it.toDomain() }
        } catch (e: Exception) {
            throw e
        }
    }
}