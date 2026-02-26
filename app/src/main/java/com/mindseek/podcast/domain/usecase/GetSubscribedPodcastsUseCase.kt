package com.mindseek.podcast.domain.usecase

import com.mindseek.podcast.data.local.entity.Podcast
import com.mindseek.podcast.domain.repository.PodcastRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSubscribedPodcastsUseCase @Inject constructor(
    private val podcastRepository: PodcastRepository
) {
    operator fun invoke(): Flow<List<Podcast>> {
        return podcastRepository.getSubscribedPodcasts()
    }
}