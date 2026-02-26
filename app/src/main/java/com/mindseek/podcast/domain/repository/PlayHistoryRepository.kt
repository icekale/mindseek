package com.mindseek.podcast.domain.repository

import com.mindseek.podcast.domain.model.PlayHistoryDomain
import kotlinx.coroutines.flow.Flow

interface PlayHistoryRepository {
    fun getAllPlayHistory(): Flow<List<PlayHistoryDomain>>
    fun getRecentPlayHistory(limit: Int = 50): Flow<List<PlayHistoryDomain>>
    suspend fun getPlayHistoryByEpisodeId(episodeId: String): PlayHistoryDomain?
    suspend fun savePlayHistory(episodeId: String, position: Long)
    suspend fun updatePlayPosition(episodeId: String, position: Long)
    suspend fun clearAllHistory()
    suspend fun getTotalListeningTime(): Long
    suspend fun getPlayHistoryCount(): Int
}