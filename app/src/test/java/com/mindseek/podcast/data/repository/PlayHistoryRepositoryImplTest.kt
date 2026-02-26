package com.mindseek.podcast.data.repository

import com.mindseek.podcast.data.local.dao.PlayHistoryDao
import com.mindseek.podcast.data.local.entity.PlayHistory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlayHistoryRepositoryImplTest {

    @Mock
    private lateinit var playHistoryDao: PlayHistoryDao

    private lateinit var repository: PlayHistoryRepositoryImpl

    private val samplePlayHistory = PlayHistory(
        id = "history1",
        episodeId = "episode1",
        playPosition = 1800000L, // 30 minutes
        playDate = System.currentTimeMillis()
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = PlayHistoryRepositoryImpl(playHistoryDao)
    }

    @Test
    fun `getAllPlayHistory returns all history from dao`() = runTest {
        // Given
        val histories = listOf(samplePlayHistory)
        whenever(playHistoryDao.getAllPlayHistory()).thenReturn(flowOf(histories))

        // When
        val result = repository.getAllPlayHistory().first()

        // Then
        assertEquals(1, result.size)
        assertEquals(samplePlayHistory.episodeId, result[0].episodeId)
        assertEquals(samplePlayHistory.playPosition, result[0].playPosition)
        verify(playHistoryDao).getAllPlayHistory()
    }

    @Test
    fun `getRecentPlayHistory returns limited history from dao`() = runTest {
        // Given
        val histories = listOf(samplePlayHistory)
        whenever(playHistoryDao.getRecentPlayHistory(20)).thenReturn(flowOf(histories))

        // When
        val result = repository.getRecentPlayHistory(20).first()

        // Then
        assertEquals(1, result.size)
        assertEquals(samplePlayHistory.episodeId, result[0].episodeId)
        verify(playHistoryDao).getRecentPlayHistory(20)
    }

    @Test
    fun `getPlayHistoryByEpisodeId returns history for episode`() = runTest {
        // Given
        whenever(playHistoryDao.getPlayHistoryByEpisodeId("episode1")).thenReturn(samplePlayHistory)

        // When
        val result = repository.getPlayHistoryByEpisodeId("episode1")

        // Then
        assertEquals(samplePlayHistory.episodeId, result?.episodeId)
        assertEquals(samplePlayHistory.playPosition, result?.playPosition)
        verify(playHistoryDao).getPlayHistoryByEpisodeId("episode1")
    }

    @Test
    fun `getPlayHistoryByEpisodeId returns null when not found`() = runTest {
        // Given
        whenever(playHistoryDao.getPlayHistoryByEpisodeId("episode1")).thenReturn(null)

        // When
        val result = repository.getPlayHistoryByEpisodeId("episode1")

        // Then
        assertNull(result)
        verify(playHistoryDao).getPlayHistoryByEpisodeId("episode1")
    }

    @Test
    fun `savePlayHistory creates new history when none exists`() = runTest {
        // Given
        whenever(playHistoryDao.getPlayHistoryByEpisodeId("episode1")).thenReturn(null)

        // When
        repository.savePlayHistory("episode1", 1800000L)

        // Then
        verify(playHistoryDao).getPlayHistoryByEpisodeId("episode1")
        verify(playHistoryDao).insertPlayHistory(any())
        verify(playHistoryDao, never()).updatePlayPosition(any(), any(), any())
    }

    @Test
    fun `savePlayHistory updates existing history when it exists`() = runTest {
        // Given
        whenever(playHistoryDao.getPlayHistoryByEpisodeId("episode1")).thenReturn(samplePlayHistory)

        // When
        repository.savePlayHistory("episode1", 2400000L)

        // Then
        verify(playHistoryDao).getPlayHistoryByEpisodeId("episode1")
        verify(playHistoryDao).updatePlayPosition(eq("episode1"), eq(2400000L), any())
        verify(playHistoryDao, never()).insertPlayHistory(any())
    }

    @Test
    fun `updatePlayPosition updates position and date`() = runTest {
        // When
        repository.updatePlayPosition("episode1", 3000000L)

        // Then
        verify(playHistoryDao).updatePlayPosition(eq("episode1"), eq(3000000L), any())
    }

    @Test
    fun `clearAllHistory clears all history`() = runTest {
        // When
        repository.clearAllHistory()

        // Then
        verify(playHistoryDao).clearAllHistory()
    }

    @Test
    fun `getTotalListeningTime returns total time from dao`() = runTest {
        // Given
        whenever(playHistoryDao.getTotalListeningTime()).thenReturn(7200000L) // 2 hours

        // When
        val result = repository.getTotalListeningTime()

        // Then
        assertEquals(7200000L, result)
        verify(playHistoryDao).getTotalListeningTime()
    }

    @Test
    fun `getTotalListeningTime returns 0 when dao returns null`() = runTest {
        // Given
        whenever(playHistoryDao.getTotalListeningTime()).thenReturn(null)

        // When
        val result = repository.getTotalListeningTime()

        // Then
        assertEquals(0L, result)
        verify(playHistoryDao).getTotalListeningTime()
    }

    @Test
    fun `getPlayHistoryCount returns count from dao`() = runTest {
        // Given
        whenever(playHistoryDao.getPlayHistoryCount()).thenReturn(15)

        // When
        val result = repository.getPlayHistoryCount()

        // Then
        assertEquals(15, result)
        verify(playHistoryDao).getPlayHistoryCount()
    }
}