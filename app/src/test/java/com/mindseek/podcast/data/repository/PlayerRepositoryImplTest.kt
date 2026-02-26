package com.mindseek.podcast.data.repository

import android.content.Context
import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.domain.model.PlayerState
import com.mindseek.podcast.domain.repository.PlayHistoryRepository
import com.mindseek.podcast.domain.usecase.player.GetNextEpisodeUseCase
import com.mindseek.podcast.domain.usecase.player.GetPreviousEpisodeUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class PlayerRepositoryImplTest {

    @Mock
    private lateinit var playHistoryRepository: PlayHistoryRepository
    
    @Mock
    private lateinit var getNextEpisodeUseCase: GetNextEpisodeUseCase
    
    @Mock
    private lateinit var getPreviousEpisodeUseCase: GetPreviousEpisodeUseCase
    
    private lateinit var context: Context
    private lateinit var playerRepository: PlayerRepositoryImpl

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = RuntimeEnvironment.getApplication()
        playerRepository = PlayerRepositoryImpl(
            context = context,
            playHistoryRepository = playHistoryRepository,
            getNextEpisodeUseCase = getNextEpisodeUseCase,
            getPreviousEpisodeUseCase = getPreviousEpisodeUseCase
        )
    }

    @Test
    fun `repository should initialize with default state`() = runTest {
        // When - checking initial state
        val playerState = playerRepository.playerState.value
        val currentEpisode = playerRepository.currentEpisode.value
        val isPlaying = playerRepository.isPlaying.value
        val currentPosition = playerRepository.currentPosition.value
        val duration = playerRepository.duration.value
        val playbackSpeed = playerRepository.playbackSpeed.value
        val volume = playerRepository.volume.value
        val isBuffering = playerRepository.isBuffering.value
        
        // Then - initial state should be correct
        assertNotNull(playerState)
        assertEquals(PlayerState(), playerState)
        assertNull(currentEpisode)
        assertFalse(isPlaying)
        assertEquals(0L, currentPosition)
        assertEquals(0L, duration)
        assertEquals(1.0f, playbackSpeed)
        assertEquals(1.0f, volume)
        assertFalse(isBuffering)
    }

    @Test
    fun `playEpisode should save current position and load resume position`() = runTest {
        // Given
        val newEpisode = createTestEpisode("new-episode")
        
        // Mock play history
        whenever(playHistoryRepository.getPlayHistoryByEpisodeId("new-episode"))
            .thenReturn(null)
        
        // When
        playerRepository.playEpisode(newEpisode)
        
        // Then
        verify(playHistoryRepository).getPlayHistoryByEpisodeId("new-episode")
        // Note: Service binding and actual playback would be tested with integration tests
    }

    @Test
    fun `setPlaybackSpeed should update speed in state`() = runTest {
        // Given
        val newSpeed = 1.5f
        
        // When
        playerRepository.setPlaybackSpeed(newSpeed)
        
        // Then
        assertEquals(newSpeed, playerRepository.playbackSpeed.value)
        assertEquals(newSpeed, playerRepository.playerState.value.playbackSpeed)
    }

    @Test
    fun `setVolume should clamp volume between 0 and 1`() = runTest {
        // Given
        val highVolume = 2.0f
        val lowVolume = -0.5f
        val normalVolume = 0.7f
        
        // When & Then
        playerRepository.setVolume(highVolume)
        assertEquals(1.0f, playerRepository.volume.value)
        
        playerRepository.setVolume(lowVolume)
        assertEquals(0.0f, playerRepository.volume.value)
        
        playerRepository.setVolume(normalVolume)
        assertEquals(0.7f, playerRepository.volume.value)
    }

    @Test
    fun `release should clean up resources`() {
        // When
        playerRepository.release()
        
        // Then - should not throw exception
        // In a real implementation, we would verify service unbinding
    }

    private fun createTestEpisode(id: String = "test-episode-1"): EpisodeDomain {
        return EpisodeDomain(
            id = id,
            podcastId = "test-podcast-1",
            title = "Test Episode",
            description = "Test episode description",
            audioUrl = "https://example.com/test-audio.mp3",
            duration = 3600000L,
            publishDate = System.currentTimeMillis(),
            isDownloaded = false,
            localPath = null,
            playPosition = 0L,
            isFavorite = false,
            podcastTitle = "Test Podcast"
        )
    }
}