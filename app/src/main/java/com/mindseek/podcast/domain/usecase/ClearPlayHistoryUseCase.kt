package com.mindseek.podcast.domain.usecase

import com.mindseek.podcast.domain.repository.PlayHistoryRepository
import javax.inject.Inject

class ClearPlayHistoryUseCase @Inject constructor(
    private val playHistoryRepository: PlayHistoryRepository
) {
    suspend operator fun invoke() {
        playHistoryRepository.clearAllHistory()
    }
}