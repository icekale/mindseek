package com.mindseek.podcast.domain.usecase.player

import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.domain.repository.PlayerRepository
import com.mindseek.podcast.domain.repository.PlayHistoryRepository
import javax.inject.Inject

class PlayEpisodeWithHistoryUseCase @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val playHistoryRepository: PlayHistoryRepository
) {
    suspend operator fun invoke(episode: EpisodeDomain) {
        // 获取播放历史中的位置信息
        val playHistory = playHistoryRepository.getPlayHistoryByEpisodeId(episode.id)
        val episodeWithHistory = if (playHistory != null) {
            episode.copy(playPosition = playHistory.playPosition)
        } else {
            episode
        }
        
        // 播放节目
        playerRepository.playEpisode(episodeWithHistory)
    }
}