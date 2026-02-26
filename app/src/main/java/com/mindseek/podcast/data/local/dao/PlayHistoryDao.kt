package com.mindseek.podcast.data.local.dao

import androidx.room.*
import com.mindseek.podcast.data.local.entity.PlayHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayHistoryDao {
    @Query("SELECT * FROM play_history ORDER BY playDate DESC")
    fun getAllPlayHistory(): Flow<List<PlayHistory>>

    @Query("SELECT * FROM play_history WHERE episodeId = :episodeId")
    suspend fun getPlayHistoryByEpisodeId(episodeId: String): PlayHistory?

    @Query("SELECT * FROM play_history ORDER BY playDate DESC LIMIT :limit")
    fun getRecentPlayHistory(limit: Int = 50): Flow<List<PlayHistory>>

    @Query("SELECT * FROM play_history WHERE playDate >= :startDate AND playDate <= :endDate ORDER BY playDate DESC")
    fun getPlayHistoryByDateRange(startDate: Long, endDate: Long): Flow<List<PlayHistory>>

    @Query("SELECT COUNT(*) FROM play_history")
    suspend fun getPlayHistoryCount(): Int

    @Query("SELECT SUM(playPosition) FROM play_history")
    suspend fun getTotalListeningTime(): Long?

    @Query("SELECT * FROM play_history WHERE playPosition > 0 ORDER BY playDate DESC")
    fun getPartiallyPlayedEpisodes(): Flow<List<PlayHistory>>

    @Query("SELECT DISTINCT episodeId FROM play_history ORDER BY playDate DESC")
    fun getUniquePlayedEpisodeIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayHistory(playHistory: PlayHistory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayHistories(playHistories: List<PlayHistory>)

    @Update
    suspend fun updatePlayHistory(playHistory: PlayHistory)

    @Delete
    suspend fun deletePlayHistory(playHistory: PlayHistory)

    @Query("UPDATE play_history SET playPosition = :position, playDate = :playDate WHERE episodeId = :episodeId")
    suspend fun updatePlayPosition(episodeId: String, position: Long, playDate: Long)

    @Query("DELETE FROM play_history")
    suspend fun clearAllHistory()

    @Query("DELETE FROM play_history WHERE playDate < :cutoffDate")
    suspend fun deleteOldHistory(cutoffDate: Long)

    @Query("DELETE FROM play_history WHERE episodeId = :episodeId")
    suspend fun deleteHistoryByEpisodeId(episodeId: String)
}