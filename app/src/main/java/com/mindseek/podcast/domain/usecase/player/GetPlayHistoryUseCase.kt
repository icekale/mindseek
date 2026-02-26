package com.mindseek.podcast.domain.usecase.player

import com.mindseek.podcast.domain.model.PlayHistoryDomain
import com.mindseek.podcast.domain.repository.PlayHistoryRepository
import javax.inject.Inject

class GetPlayHistoryUseCase @Inject constructor(
    private val playHistoryRepository: PlayHistoryRepository
) {
    suspend fun getByEpisodeId(episodeId: String): PlayHistoryDomain? {
        return playHistoryRepository.getPlayHistoryByEpisodeId(episodeId)
    }
    
    suspend fun updatePlayPosition(episodeId: String, position: Long) {
        playHistoryRepository.updatePlayPosition(episodeId, position)
    }
}