package com.mindseek.podcast.domain.usecase.player

import com.mindseek.podcast.data.mapper.toDomain
import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.domain.repository.PodcastRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetPreviousEpisodeUseCase @Inject constructor(
    private val podcastRepository: PodcastRepository
) {
    suspend operator fun invoke(currentEpisode: EpisodeDomain): EpisodeDomain? {
        return try {
            val episodes = podcastRepository.getEpisodesByPodcastId(currentEpisode.podcastId)
                .first()
                .map { it.toDomain() }
                .sortedByDescending { it.publishDate }
            
            val currentIndex = episodes.indexOfFirst { it.id == currentEpisode.id }
            
            if (currentIndex > 0) {
                episodes[currentIndex - 1]
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}