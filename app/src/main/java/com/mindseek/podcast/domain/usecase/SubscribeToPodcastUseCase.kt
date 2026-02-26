package com.mindseek.podcast.domain.usecase

import com.mindseek.podcast.domain.repository.PodcastRepository
import javax.inject.Inject

class SubscribeToPodcastUseCase @Inject constructor(
    private val podcastRepository: PodcastRepository
) {
    suspend operator fun invoke(podcastId: String) {
        try {
            podcastRepository.subscribeToPodcast(podcastId)
        } catch (e: Exception) {
            throw e
        }
    }
}