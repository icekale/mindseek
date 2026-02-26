package com.mindseek.podcast.domain.usecase

import com.mindseek.podcast.data.mapper.toDomain
import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.domain.repository.PodcastRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetPodcastEpisodesUseCase @Inject constructor(
    private val podcastRepository: PodcastRepository
) {
    suspend operator fun invoke(podcastId: String): Flow<List<EpisodeDomain>> {
        return try {
            podcastRepository.getEpisodesByPodcastId(podcastId).map { episodes ->
                episodes.map { it.toDomain() }
            }
        } catch (e: Exception) {
            throw e
        }
    }
}