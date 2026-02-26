package com.mindseek.podcast.domain.usecase

import com.mindseek.podcast.domain.model.PlayHistoryDomain
import com.mindseek.podcast.domain.repository.PlayHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllPlayHistoryUseCase @Inject constructor(
    private val playHistoryRepository: PlayHistoryRepository
) {
    operator fun invoke(): Flow<List<PlayHistoryDomain>> {
        return playHistoryRepository.getAllPlayHistory()
    }
    
    fun getRecent(limit: Int = 50): Flow<List<PlayHistoryDomain>> {
        return playHistoryRepository.getRecentPlayHistory(limit)
    }
}