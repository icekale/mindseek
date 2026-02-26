package com.mindseek.podcast.domain.usecase

import com.mindseek.podcast.data.mapper.toDomain
import com.mindseek.podcast.domain.model.PodcastDomain
import com.mindseek.podcast.domain.repository.PodcastRepository
import javax.inject.Inject

class GetPodcastDetailUseCase @Inject constructor(
    private val podcastRepository: PodcastRepository
) {
    suspend operator fun invoke(podcastId: String): PodcastDomain? {
        return try {
            val podcast = podcastRepository.getPodcastById(podcastId)
            podcast?.toDomain()
        } catch (e: Exception) {
            throw e
        }
    }
}