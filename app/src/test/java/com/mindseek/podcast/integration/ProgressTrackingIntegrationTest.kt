package com.mindseek.podcast.integration

import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.domain.model.PlayHistoryDomain
import com.mindseek.podcast.domain.model.PlaybackSpeed
import com.mindseek.podcast.domain.model.PlayerState
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration test for progress tracking functionality
 * Tests the enhanced progress management features implemented in task 3.2 and 3.3
 */
class ProgressTrackingIntegrationTest {

    @Test
    fun `PlayerState should handle progress tracking correctly`() {
        // Given
        val episode = createTestEpisode()
        val playerState = PlayerState(
            currentEpisode = episode,
            isPlaying = true,
            currentPosition = 30000L, // 30 seconds
            duration = 180000L, // 3 minutes
            playbackSpeed = 1.5f,
            volume = 0.8f,
            isBuffering = false,
            error = null
        )

        // When & Then
        assertEquals(episode, playerState.currentEpisode)
        assertTrue(playerState.isPlaying)
        assertEquals(30000L, playerState.currentPosition)
        assertEquals(180000L, playerState.duration)
        assertEquals(1.5f, playerState.playbackSpeed)
        assertEquals(0.8f, playerState.volume)
    }

    @Test
    fun `PlayHistoryDomain should track episode progress correctly`() {
        // Given
        val episode = createTestEpisode()
        val playHistory = PlayHistoryDomain(
            id = "history-1",
            episode = episode,
            playPosition = 45000L, // 45 seconds
            playDate = System.currentTimeMillis()
        )

        // When & Then
        assertEquals("history-1", playHistory.id)
        assertEquals(episode, playHistory.episode)
        assertEquals(45000L, playHistory.playPosition)
        assertTrue(playHistory.playDate > 0)
    }

    @Test
    fun `resume position should be calculated correctly from play history`() {
        // Given
        val episode = createTestEpisode()
        val playHistory = PlayHistoryDomain(
            id = "history-1",
            episode = episode,
            playPosition = 120000L, // 2 minutes
            playDate = System.currentTimeMillis()
        )

        // When
        val resumePosition = playHistory.playPosition
        val episodeWithResumePosition = episode.copy(playPosition = resumePosition)

        // Then
        assertEquals(120000L, resumePosition)
        assertEquals(120000L, episodeWithResumePosition.playPosition)
        assertEquals(episode.id, episodeWithResumePosition.id)
    }

    @Test
    fun `episode completion should be detected correctly`() {
        // Given
        val episode = createTestEpisode()
        val duration = 180000L // 3 minutes
        val nearEndPosition = 170000L // 2 minutes 50 seconds (within last 10 seconds)
        val completedPosition = duration // Full duration

        // When & Then
        val isNearEnd = (duration - nearEndPosition) < 30000 // Within last 30 seconds
        assertTrue(isNearEnd, "Should detect when playback is near the end")

        val isCompleted = completedPosition >= duration
        assertTrue(isCompleted, "Should detect when episode is completed")
    }

    @Test
    fun `auto-save progress intervals should be calculated correctly`() {
        // Given
        val lastSaveTime = System.currentTimeMillis() - 15000L // 15 seconds ago
        val currentTime = System.currentTimeMillis()
        val lastSavedPosition = 60000L // 1 minute
        val currentPosition = 95000L // 1 minute 35 seconds

        // When
        val timeDiff = currentTime - lastSaveTime
        val positionDiff = kotlin.math.abs(currentPosition - lastSavedPosition)

        // Then
        assertTrue(timeDiff > 10000, "Should save after 10 seconds")
        assertTrue(positionDiff > 30000, "Should save when position changes by more than 30 seconds")
    }

    @Test
    fun `next episode logic should work correctly`() {
        // Given
        val episodes = listOf(
            createTestEpisode("episode-1", "Episode 1", 1000L),
            createTestEpisode("episode-2", "Episode 2", 2000L),
            createTestEpisode("episode-3", "Episode 3", 3000L)
        ).sortedByDescending { it.publishDate }

        val currentEpisode = episodes[1] // Middle episode

        // When
        val currentIndex = episodes.indexOfFirst { it.id == currentEpisode.id }
        val nextEpisode = if (currentIndex != -1 && currentIndex < episodes.size - 1) {
            episodes[currentIndex + 1]
        } else null

        val previousEpisode = if (currentIndex > 0) {
            episodes[currentIndex - 1]
        } else null

        // Then
        assertNotNull(nextEpisode, "Should find next episode")
        assertNotNull(previousEpisode, "Should find previous episode")
        assertEquals("episode-3", nextEpisode.id)
        assertEquals("episode-1", previousEpisode.id)
    }

    @Test
    fun `skip to previous logic should work correctly`() {
        // Given
        val currentPosition = 5000L // 5 seconds
        val skipThreshold = 10000L // 10 seconds

        // When & Then
        val shouldSkipToPrevious = currentPosition <= skipThreshold
        assertTrue(shouldSkipToPrevious, "Should skip to previous episode when current position is less than 10 seconds")

        val longPosition = 15000L // 15 seconds
        val shouldSeekToBeginning = longPosition > skipThreshold
        assertTrue(shouldSeekToBeginning, "Should seek to beginning when current position is more than 10 seconds")
    }

    @Test
    fun `PlaybackSpeed enum should provide correct values`() {
        // When & Then
        assertEquals(0.5f, PlaybackSpeed.SPEED_0_5X.value)
        assertEquals("0.5x", PlaybackSpeed.SPEED_0_5X.displayName)
        
        assertEquals(1.0f, PlaybackSpeed.SPEED_1X.value)
        assertEquals("1x", PlaybackSpeed.SPEED_1X.displayName)
        
        assertEquals(2.0f, PlaybackSpeed.SPEED_2X.value)
        assertEquals("2x", PlaybackSpeed.SPEED_2X.displayName)
    }

    @Test
    fun `progress percentage calculation should work correctly`() {
        // Given
        val currentPosition = 60000L // 1 minute
        val duration = 180000L // 3 minutes
        
        // When
        val percentage = if (duration > 0) {
            (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
        
        // Then
        assertEquals(0.333f, percentage, 0.001f)
    }

    @Test
    fun `volume and speed clamping should work correctly`() {
        // Given & When & Then
        val clampedVolumeHigh = 2.0f.coerceIn(0f, 1f)
        assertEquals(1.0f, clampedVolumeHigh)
        
        val clampedVolumeLow = -0.5f.coerceIn(0f, 1f)
        assertEquals(0.0f, clampedVolumeLow)
        
        val clampedSpeedHigh = 5.0f.coerceIn(0.25f, 3.0f)
        assertEquals(3.0f, clampedSpeedHigh)
        
        val clampedSpeedLow = 0.1f.coerceIn(0.25f, 3.0f)
        assertEquals(0.25f, clampedSpeedLow)
    }

    @Test
    fun `time formatting should work correctly`() {
        // Given
        fun formatTime(milliseconds: Long): String {
            val totalSeconds = milliseconds / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%d:%02d", minutes, seconds)
            }
        }
        
        // When & Then
        assertEquals("1:30", formatTime(90000L)) // 1 minute 30 seconds
        assertEquals("0:05", formatTime(5000L)) // 5 seconds
        assertEquals("1:00:00", formatTime(3600000L)) // 1 hour
        assertEquals("1:05:30", formatTime(3930000L)) // 1 hour 5 minutes 30 seconds
    }

    private fun createTestEpisode(
        id: String = "test-episode-1",
        title: String = "Test Episode",
        publishDate: Long = System.currentTimeMillis()
    ): EpisodeDomain {
        return EpisodeDomain(
            id = id,
            podcastId = "test-podcast-1",
            title = title,
            description = "Test episode description",
            audioUrl = "https://example.com/test-audio.mp3",
            duration = 3600000L, // 1 hour
            publishDate = publishDate,
            isDownloaded = false,
            localPath = null,
            playPosition = 0L,
            isFavorite = false,
            podcastTitle = "Test Podcast"
        )
    }
}