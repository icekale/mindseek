package com.mindseek.podcast.data.repository

import com.mindseek.podcast.data.local.dao.EpisodeDao
import com.mindseek.podcast.data.local.dao.PlayHistoryDao
import com.mindseek.podcast.data.local.entity.PlayHistory
import com.mindseek.podcast.data.mapper.toDomain
import com.mindseek.podcast.domain.model.PlayHistoryDomain
import com.mindseek.podcast.domain.repository.PlayHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayHistoryRepositoryImpl @Inject constructor(
    private val playHistoryDao: PlayHistoryDao,
    private val episodeDao: EpisodeDao
) : PlayHistoryRepository {

    override fun getAllPlayHistory(): Flow<List<PlayHistoryDomain>> {
        return playHistoryDao.getAllPlayHistory().map { histories ->
            histories.mapNotNull { history ->
                episodeDao.getEpisodeById(history.episodeId)?.let { episode ->
                    history.toDomain(episode.toDomain())
                }
            }
        }
    }

    override fun getRecentPlayHistory(limit: Int): Flow<List<PlayHistoryDomain>> {
        return playHistoryDao.getRecentPlayHistory(limit).map { histories ->
            histories.mapNotNull { history ->
                episodeDao.getEpisodeById(history.episodeId)?.let { episode ->
                    history.toDomain(episode.toDomain())
                }
            }
        }
    }

    override suspend fun getPlayHistoryByEpisodeId(episodeId: String): PlayHistoryDomain? {
        val history = playHistoryDao.getPlayHistoryByEpisodeId(episodeId) ?: return null
        val episode = episodeDao.getEpisodeById(episodeId) ?: return null
        return history.toDomain(episode.toDomain())
    }

    override suspend fun savePlayHistory(episodeId: String, position: Long) {
        val existingHistory = playHistoryDao.getPlayHistoryByEpisodeId(episodeId)
        
        if (existingHistory != null) {
            // Update existing history
            playHistoryDao.updatePlayPosition(episodeId, position, System.currentTimeMillis())
        } else {
            // Create new history entry
            val playHistory = PlayHistory(
                id = UUID.randomUUID().toString(),
                episodeId = episodeId,
                playPosition = position,
                playDate = System.currentTimeMillis()
            )
            playHistoryDao.insertPlayHistory(playHistory)
        }
    }

    override suspend fun updatePlayPosition(episodeId: String, position: Long) {
        playHistoryDao.updatePlayPosition(episodeId, position, System.currentTimeMillis())
    }

    override suspend fun clearAllHistory() {
        playHistoryDao.clearAllHistory()
    }

    override suspend fun getTotalListeningTime(): Long {
        return playHistoryDao.getTotalListeningTime() ?: 0L
    }

    override suspend fun getPlayHistoryCount(): Int {
        return playHistoryDao.getPlayHistoryCount()
    }
}