package com.mindseek.podcast.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mindseek.podcast.data.local.PodcastDatabase
import com.mindseek.podcast.data.local.entity.PlayHistory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayHistoryDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: PodcastDatabase
    private lateinit var playHistoryDao: PlayHistoryDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PodcastDatabase::class.java
        ).allowMainThreadQueries().build()
        
        playHistoryDao = database.playHistoryDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetPlayHistory() = runTest {
        // Given
        val playHistory = createTestPlayHistory("1", "episode1", 30000L)

        // When
        playHistoryDao.insertPlayHistory(playHistory)
        val retrieved = playHistoryDao.getPlayHistoryByEpisodeId("episode1")

        // Then
        assertNotNull(retrieved)
        assertEquals(playHistory.id, retrieved?.id)
        assertEquals(playHistory.episodeId, retrieved?.episodeId)
        assertEquals(playHistory.playPosition, retrieved?.playPosition)
    }

    @Test
    fun getAllPlayHistory() = runTest {
        // Given
        val playHistories = listOf(
            createTestPlayHistory("1", "episode1", 30000L),
            createTestPlayHistory("2", "episode2", 60000L),
            createTestPlayHistory("3", "episode3", 90000L)
        )

        // When
        playHistoryDao.insertPlayHistories(playHistories)
        val allHistory = playHistoryDao.getAllPlayHistory().first()

        // Then
        assertEquals(3, allHistory.size)
        // Should be ordered by playDate DESC (most recent first)
        assertTrue(allHistory[0].playDate >= allHistory[1].playDate)
        assertTrue(allHistory[1].playDate >= allHistory[2].playDate)
    }

    @Test
    fun getRecentPlayHistory() = runTest {
        // Given
        val playHistories = (1..60).map { i ->
            createTestPlayHistory(
                id = i.toString(),
                episodeId = "episode$i",
                playPosition = i * 1000L,
                playDate = System.currentTimeMillis() - (i * 1000L)
            )
        }

        // When
        playHistoryDao.insertPlayHistories(playHistories)
        val recentHistory = playHistoryDao.getRecentPlayHistory(20).first()

        // Then
        assertEquals(20, recentHistory.size)
        // Should be ordered by playDate DESC (most recent first)
        for (i in 0 until recentHistory.size - 1) {
            assertTrue(recentHistory[i].playDate >= recentHistory[i + 1].playDate)
        }
    }

    @Test
    fun getPlayHistoryByDateRange() = runTest {
        // Given
        val baseTime = System.currentTimeMillis()
        val playHistories = listOf(
            createTestPlayHistory("1", "episode1", 30000L, baseTime - 20000), // Before range
            createTestPlayHistory("2", "episode2", 60000L, baseTime - 5000),  // In range
            createTestPlayHistory("3", "episode3", 90000L, baseTime),         // In range
            createTestPlayHistory("4", "episode4", 120000L, baseTime + 10000) // After range
        )

        // When
        playHistoryDao.insertPlayHistories(playHistories)
        val rangeHistory = playHistoryDao.getPlayHistoryByDateRange(
            startDate = baseTime - 10000,
            endDate = baseTime + 5000
        ).first()

        // Then
        assertEquals(2, rangeHistory.size)
        assertTrue(rangeHistory.any { it.episodeId == "episode2" })
        assertTrue(rangeHistory.any { it.episodeId == "episode3" })
    }

    @Test
    fun getPlayHistoryCount() = runTest {
        // Given
        val playHistories = listOf(
            createTestPlayHistory("1", "episode1", 30000L),
            createTestPlayHistory("2", "episode2", 60000L),
            createTestPlayHistory("3", "episode3", 90000L)
        )

        // When
        playHistoryDao.insertPlayHistories(playHistories)
        val count = playHistoryDao.getPlayHistoryCount()

        // Then
        assertEquals(3, count)
    }

    @Test
    fun getTotalListeningTime() = runTest {
        // Given
        val playHistories = listOf(
            createTestPlayHistory("1", "episode1", 30000L),
            createTestPlayHistory("2", "episode2", 60000L),
            createTestPlayHistory("3", "episode3", 90000L)
        )

        // When
        playHistoryDao.insertPlayHistories(playHistories)
        val totalTime = playHistoryDao.getTotalListeningTime()

        // Then
        assertEquals(180000L, totalTime) // 30000 + 60000 + 90000
    }

    @Test
    fun getPartiallyPlayedEpisodes() = runTest {
        // Given
        val playHistories = listOf(
            createTestPlayHistory("1", "episode1", 30000L), // Partially played
            createTestPlayHistory("2", "episode2", 0L),     // Not played
            createTestPlayHistory("3", "episode3", 60000L)  // Partially played
        )

        // When
        playHistoryDao.insertPlayHistories(playHistories)
        val partiallyPlayed = playHistoryDao.getPartiallyPlayedEpisodes().first()

        // Then
        assertEquals(2, partiallyPlayed.size)
        assertTrue(partiallyPlayed.all { it.playPosition > 0 })
        assertTrue(partiallyPlayed.any { it.episodeId == "episode1" })
        assertTrue(partiallyPlayed.any { it.episodeId == "episode3" })
    }

    @Test
    fun getUniquePlayedEpisodeIds() = runTest {
        // Given
        val playHistories = listOf(
            createTestPlayHistory("1", "episode1", 30000L),
            createTestPlayHistory("2", "episode2", 60000L),
            createTestPlayHistory("3", "episode1", 90000L), // Duplicate episode
            createTestPlayHistory("4", "episode3", 120000L)
        )

        // When
        playHistoryDao.insertPlayHistories(playHistories)
        val uniqueEpisodeIds = playHistoryDao.getUniquePlayedEpisodeIds().first()

        // Then
        assertEquals(3, uniqueEpisodeIds.size)
        assertTrue(uniqueEpisodeIds.contains("episode1"))
        assertTrue(uniqueEpisodeIds.contains("episode2"))
        assertTrue(uniqueEpisodeIds.contains("episode3"))
    }

    @Test
    fun updatePlayPosition() = runTest {
        // Given
        val playHistory = createTestPlayHistory("1", "episode1", 30000L)
        val newPosition = 60000L
        val newPlayDate = System.currentTimeMillis() + 5000

        // When
        playHistoryDao.insertPlayHistory(playHistory)
        playHistoryDao.updatePlayPosition("episode1", newPosition, newPlayDate)
        val updated = playHistoryDao.getPlayHistoryByEpisodeId("episode1")

        // Then
        assertNotNull(updated)
        assertEquals(newPosition, updated?.playPosition)
        assertEquals(newPlayDate, updated?.playDate)
    }

    @Test
    fun updatePlayHistory() = runTest {
        // Given
        val originalHistory = createTestPlayHistory("1", "episode1", 30000L)
        val updatedHistory = originalHistory.copy(playPosition = 60000L)

        // When
        playHistoryDao.insertPlayHistory(originalHistory)
        playHistoryDao.updatePlayHistory(updatedHistory)
        val retrieved = playHistoryDao.getPlayHistoryByEpisodeId("episode1")

        // Then
        assertNotNull(retrieved)
        assertEquals(60000L, retrieved?.playPosition)
    }

    @Test
    fun deletePlayHistory() = runTest {
        // Given
        val playHistory = createTestPlayHistory("1", "episode1", 30000L)

        // When
        playHistoryDao.insertPlayHistory(playHistory)
        playHistoryDao.deletePlayHistory(playHistory)
        val retrieved = playHistoryDao.getPlayHistoryByEpisodeId("episode1")

        // Then
        assertNull(retrieved)
    }

    @Test
    fun deleteHistoryByEpisodeId() = runTest {
        // Given
        val playHistories = listOf(
            createTestPlayHistory("1", "episode1", 30000L),
            createTestPlayHistory("2", "episode2", 60000L),
            createTestPlayHistory("3", "episode1", 90000L) // Another entry for episode1
        )

        // When
        playHistoryDao.insertPlayHistories(playHistories)
        playHistoryDao.deleteHistoryByEpisodeId("episode1")
        val episode1History = playHistoryDao.getPlayHistoryByEpisodeId("episode1")
        val episode2History = playHistoryDao.getPlayHistoryByEpisodeId("episode2")

        // Then
        assertNull(episode1History)
        assertNotNull(episode2History)
    }

    @Test
    fun clearAllHistory() = runTest {
        // Given
        val playHistories = listOf(
            createTestPlayHistory("1", "episode1", 30000L),
            createTestPlayHistory("2", "episode2", 60000L),
            createTestPlayHistory("3", "episode3", 90000L)
        )

        // When
        playHistoryDao.insertPlayHistories(playHistories)
        playHistoryDao.clearAllHistory()
        val allHistory = playHistoryDao.getAllPlayHistory().first()

        // Then
        assertEquals(0, allHistory.size)
    }

    @Test
    fun deleteOldHistory() = runTest {
        // Given
        val baseTime = System.currentTimeMillis()
        val cutoffTime = baseTime - 10000
        val playHistories = listOf(
            createTestPlayHistory("1", "episode1", 30000L, baseTime - 20000), // Old
            createTestPlayHistory("2", "episode2", 60000L, baseTime - 5000),  // Recent
            createTestPlayHistory("3", "episode3", 90000L, baseTime)          // Recent
        )

        // When
        playHistoryDao.insertPlayHistories(playHistories)
        playHistoryDao.deleteOldHistory(cutoffTime)
        val remainingHistory = playHistoryDao.getAllPlayHistory().first()

        // Then
        assertEquals(2, remainingHistory.size)
        assertTrue(remainingHistory.all { it.playDate >= cutoffTime })
    }

    private fun createTestPlayHistory(
        id: String,
        episodeId: String,
        playPosition: Long,
        playDate: Long = System.currentTimeMillis()
    ) = PlayHistory(
        id = id,
        episodeId = episodeId,
        playPosition = playPosition,
        playDate = playDate
    )
}