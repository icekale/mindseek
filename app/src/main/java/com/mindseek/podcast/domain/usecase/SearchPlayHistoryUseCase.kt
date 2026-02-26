package com.mindseek.podcast.domain.usecase

import com.mindseek.podcast.domain.model.PlayHistoryDomain
import com.mindseek.podcast.domain.repository.PlayHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SearchPlayHistoryUseCase @Inject constructor(
    private val playHistoryRepository: PlayHistoryRepository
) {
    operator fun invoke(query: String): Flow<List<PlayHistoryDomain>> {
        return playHistoryRepository.getAllPlayHistory().map { historyList ->
            if (query.isBlank()) {
                historyList
            } else {
                historyList.filter { history ->
                    history.episode.title.contains(query, ignoreCase = true) ||
                    history.episode.description.contains(query, ignoreCase = true)
                }
            }
        }
    }
}