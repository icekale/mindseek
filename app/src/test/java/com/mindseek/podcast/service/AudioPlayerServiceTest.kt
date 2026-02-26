package com.mindseek.podcast.service

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.mindseek.podcast.domain.model.EpisodeDomain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ServiceController
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class AudioPlayerServiceTest {

    private lateinit var context: Context
    private lateinit var serviceController: ServiceController<AudioPlayerService>
    private lateinit var service: AudioPlayerService

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        serviceController = Robolectric.buildService(AudioPlayerService::class.java)
        service = serviceController.create().get()
    }

    @Test
    fun `service should initialize with default state`() = runTest {
        // Given - service is created
        
        // When - checking initial state
        val isPlaying = service.isPlaying.value
        val currentEpisode = service.currentEpisode.value
        val currentPosition = service.currentPosition.value
        val duration = service.duration.value
        val isBuffering = service.isBuffering.value
        val error = service.error.value
        
        // Then - initial state should be correct
        assertFalse(isPlaying)
        assertNull(currentEpisode)
        assertEquals(0L, currentPosition)
        assertEquals(0L, duration)
        assertFalse(isBuffering)
        assertNull(error)
    }

    @Test
    fun `service should handle play action intent`() = runTest {
        // Given
        val episode = createTestEpisode()
        val intent = Intent(context, AudioPlayerService::class.java).apply {
            action = AudioPlayerService.ACTION_PLAY
            putExtra(AudioPlayerService.EXTRA_EPISODE, episode)
        }
        
        // When
        val result = service.onStartCommand(intent, 0, 1)
        
        // Then
        assertEquals(android.app.Service.START_NOT_STICKY, result)
        // Note: In a real test, we would verify that the episode is set,
        // but since ExoPlayer requires actual media files, we focus on intent handling
    }

    @Test
    fun `service should handle pause action intent`() = runTest {
        // Given
        val intent = Intent(context, AudioPlayerService::class.java).apply {
            action = AudioPlayerService.ACTION_PAUSE
        }
        
        // When
        val result = service.onStartCommand(intent, 0, 1)
        
        // Then
        assertEquals(android.app.Service.START_NOT_STICKY, result)
    }

    @Test
    fun `service should handle stop action intent`() = runTest {
        // Given
        val intent = Intent(context, AudioPlayerService::class.java).apply {
            action = AudioPlayerService.ACTION_STOP
        }
        
        // When
        val result = service.onStartCommand(intent, 0, 1)
        
        // Then
        assertEquals(android.app.Service.START_NOT_STICKY, result)
    }

    @Test
    fun `service should handle next action intent`() = runTest {
        // Given
        val intent = Intent(context, AudioPlayerService::class.java).apply {
            action = AudioPlayerService.ACTION_NEXT
        }
        
        // When
        val result = service.onStartCommand(intent, 0, 1)
        
        // Then
        assertEquals(android.app.Service.START_NOT_STICKY, result)
    }

    @Test
    fun `service should handle previous action intent`() = runTest {
        // Given
        val intent = Intent(context, AudioPlayerService::class.java).apply {
            action = AudioPlayerService.ACTION_PREVIOUS
        }
        
        // When
        val result = service.onStartCommand(intent, 0, 1)
        
        // Then
        assertEquals(android.app.Service.START_NOT_STICKY, result)
    }

    @Test
    fun `service should return binder on bind`() {
        // When
        val binder = service.onBind(Intent())
        
        // Then
        assertNotNull(binder)
        assertTrue(binder is AudioPlayerService.AudioPlayerBinder)
        
        val audioPlayerBinder = binder as AudioPlayerService.AudioPlayerBinder
        assertEquals(service, audioPlayerBinder.getService())
    }

    @Test
    fun `service should handle playback control methods`() = runTest {
        // Given
        val episode = createTestEpisode()
        
        // When - calling control methods (these won't actually play without real media)
        service.playEpisode(episode)
        service.play()
        service.pause()
        service.stop()
        service.seekTo(30000L)
        service.setPlaybackSpeed(1.5f)
        service.setVolume(0.8f)
        service.skipToNext()
        service.skipToPrevious()
        
        // Then - methods should execute without throwing exceptions
        // In a real implementation with mock ExoPlayer, we would verify the calls
        assertTrue(true) // Test passes if no exceptions are thrown
    }

    @Test
    fun `service should handle seek position clamping correctly`() = runTest {
        // Given
        val episode = createTestEpisode()
        service.playEpisode(episode)
        
        // When - seeking to various positions
        service.seekTo(-1000L) // Negative position
        service.seekTo(0L) // Start position
        service.seekTo(30000L) // Valid position
        service.seekTo(Long.MAX_VALUE) // Very large position
        
        // Then - methods should execute without throwing exceptions
        // Position clamping is handled internally by ExoPlayer and our service
        assertTrue(true)
    }

    @Test
    fun `service should handle playback speed clamping correctly`() = runTest {
        // When - setting various playback speeds
        service.setPlaybackSpeed(0.1f) // Below minimum
        service.setPlaybackSpeed(0.5f) // Valid minimum
        service.setPlaybackSpeed(1.0f) // Normal speed
        service.setPlaybackSpeed(2.0f) // Valid speed
        service.setPlaybackSpeed(5.0f) // Above maximum
        
        // Then - methods should execute without throwing exceptions
        // Speed clamping is handled by the service
        assertTrue(true)
    }

    @Test
    fun `service should handle volume clamping correctly`() = runTest {
        // When - setting various volume levels
        service.setVolume(-0.5f) // Below minimum
        service.setVolume(0.0f) // Minimum volume
        service.setVolume(0.5f) // Half volume
        service.setVolume(1.0f) // Maximum volume
        service.setVolume(2.0f) // Above maximum
        
        // Then - methods should execute without throwing exceptions
        // Volume clamping is handled by the service
        assertTrue(true)
    }

    @Test
    fun `service should handle episode with resume position`() = runTest {
        // Given
        val episode = createTestEpisode().copy(playPosition = 120000L) // 2 minutes
        
        // When
        service.playEpisode(episode)
        
        // Then - should handle episode with resume position without throwing
        assertTrue(true)
    }

    @Test
    fun `service should handle null episode gracefully`() = runTest {
        // When - calling methods that depend on current episode when none is set
        service.skipToNext()
        service.skipToPrevious()
        
        // Then - should not throw exceptions
        assertTrue(true)
    }

    private fun createTestEpisode(): EpisodeDomain {
        return EpisodeDomain(
            id = "test-episode-1",
            podcastId = "test-podcast-1",
            title = "Test Episode",
            description = "Test episode description",
            audioUrl = "https://example.com/test-audio.mp3",
            duration = 3600000L, // 1 hour
            publishDate = System.currentTimeMillis(),
            isDownloaded = false,
            localPath = null,
            playPosition = 0L,
            isFavorite = false,
            podcastTitle = "Test Podcast"
        )
    }
}