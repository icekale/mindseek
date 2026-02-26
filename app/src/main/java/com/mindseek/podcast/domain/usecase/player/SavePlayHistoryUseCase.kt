package com.mindseek.podcast.domain.usecase.player

import com.mindseek.podcast.domain.repository.PlayHistoryRepository
import javax.inject.Inject

class SavePlayHistoryUseCase @Inject constructor(
    private val playHistoryRepository: PlayHistoryRepository
) {
    suspend operator fun invoke(episodeId: String, position: Long) {
        playHistoryRepository.savePlayHistory(episodeId, position)
    }
}