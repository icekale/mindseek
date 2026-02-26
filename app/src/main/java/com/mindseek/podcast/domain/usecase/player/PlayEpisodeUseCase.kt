package com.mindseek.podcast.domain.usecase.player

import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.domain.repository.PlayerRepository
import javax.inject.Inject

class PlayEpisodeUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) {
    suspend operator fun invoke(episode: EpisodeDomain) {
        playerRepository.playEpisode(episode)
    }
}