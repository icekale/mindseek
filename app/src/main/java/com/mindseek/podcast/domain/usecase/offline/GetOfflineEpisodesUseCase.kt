package com.mindseek.podcast.domain.usecase.offline

import com.mindseek.podcast.data.mapper.toDomain
import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.domain.repository.PodcastRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetOfflineEpisodesUseCase @Inject constructor(
    private val podcastRepository: PodcastRepository,
    private val checkOfflineAvailabilityUseCase: CheckOfflineAvailabilityUseCase
) {
    
    /**
     * Get all episodes that are available for offline playback
     */
    operator fun invoke(): Flow<List<EpisodeDomain>> {
        return podcastRepository.getDownloadedEpisodes()
            .map { episodes ->
                episodes.map { it.toDomain() }
                    .filter { episode ->
                        checkOfflineAvailabilityUseCase(episode)
                    }
            }
    }
}