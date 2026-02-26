package com.mindseek.podcast.presentation.player

import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.domain.model.PlaybackSpeed
import com.mindseek.podcast.domain.model.PlayerState
import com.mindseek.podcast.domain.repository.PlayerRepository
import com.mindseek.podcast.domain.usecase.player.ControlPlaybackUseCase
import com.mindseek.podcast.domain.usecase.player.PlayEpisodeUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class PlayerViewModelTest {

    @Mock
    private lateinit var playerRepository: PlayerRepository

    @Mock
    private lateinit var playEpisodeUseCase: PlayEpisodeUseCase

    @Mock
    private lateinit var controlPlaybackUseCase: ControlPlaybackUseCase

    private lateinit var viewModel: PlayerViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val playerStateFlow = MutableStateFlow(PlayerState())

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        whenever(playerRepository.playerState).thenReturn(playerStateFlow)
        
        viewModel = PlayerViewModel(
            playerRepository = playerRepository,
            playEpisodeUseCase = playEpisodeUseCase,
            controlPlaybackUseCase = controlPlaybackUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial ui state should be correct`() = runTest {
        val uiState = viewModel.uiState.first()
        
        assertEquals(PlaybackSpeed.SPEED_1X, uiState.selectedSpeed)
        assertFalse(uiState.showSpeedSelector)
        assertFalse(uiState.showVolumeControl)
        assertEquals(null, uiState.error)
    }

    @Test
    fun `playEpisode should call use case`() = runTest {
        val episode = createTestEpisode()
        
        viewModel.playEpisode(episode)
        testDispatcher.scheduler.advanceUntilIdle()
        
        verify(playEpisodeUseCase).invoke(episode)
    }

    @Test
    fun `togglePlayPause should call pause when playing`() = runTest {
        // Setup playing state
        playerStateFlow.value = PlayerState(isPlaying = true)
        
        viewModel.togglePlayPause()
        testDispatcher.scheduler.advanceUntilIdle()
        
        verify(controlPlaybackUseCase).pause()
    }

    @Test
    fun `togglePlayPause should call resume when not playing`() = runTest {
        // Setup not playing state
        playerStateFlow.value = PlayerState(isPlaying = false)
        
        viewModel.togglePlayPause()
        testDispatcher.scheduler.advanceUntilIdle()
        
        verify(controlPlaybackUseCase).resume()
    }

    @Test
    fun `seekTo should call control playback use case`() = runTest {
        val position = 30000L
        
        viewModel.seekTo(position)
        testDispatcher.scheduler.advanceUntilIdle()
        
        verify(controlPlaybackUseCase).seekTo(position)
    }

    @Test
    fun `setPlaybackSpeed should update ui state and call use case`() = runTest {
        val speed = PlaybackSpeed.SPEED_1_5X
        
        viewModel.setPlaybackSpeed(speed)
        testDispatcher.scheduler.advanceUntilIdle()
        
        verify(controlPlaybackUseCase).setPlaybackSpeed(speed.value)
        
        val uiState = viewModel.uiState.first()
        assertEquals(speed, uiState.selectedSpeed)
    }

    @Test
    fun `setVolume should call control playback use case`() = runTest {
        val volume = 0.7f
        
        viewModel.setVolume(volume)
        testDispatcher.scheduler.advanceUntilIdle()
        
        verify(controlPlaybackUseCase).setVolume(volume)
    }

    @Test
    fun `onSeekStarted should set seeking flag to true`() = runTest {
        viewModel.onSeekStarted()
        
        val isSeekingByUser = viewModel.isSeekingByUser.first()
        assertTrue(isSeekingByUser)
    }

    @Test
    fun `onSeekFinished should set seeking flag to false`() = runTest {
        viewModel.onSeekStarted() // First set to true
        viewModel.onSeekFinished()
        
        val isSeekingByUser = viewModel.isSeekingByUser.first()
        assertFalse(isSeekingByUser)
    }

    @Test
    fun `formatTime should format correctly`() {
        assertEquals("0:00", viewModel.formatTime(0L))
        assertEquals("0:30", viewModel.formatTime(30000L))
        assertEquals("1:00", viewModel.formatTime(60000L))
        assertEquals("1:30", viewModel.formatTime(90000L))
        assertEquals("10:05", viewModel.formatTime(605000L))
    }

    private fun createTestEpisode(): EpisodeDomain {
        return EpisodeDomain(
            id = "test-episode-1",
            podcastId = "test-podcast-1",
            title = "Test Episode",
            description = "This is a test episode",
            audioUrl = "https://example.com/audio.mp3",
            duration = 3600000L,
            publishDate = System.currentTimeMillis(),
            isDownloaded = false,
            localPath = null,
            playPosition = 0L,
            isFavorite = false
        )
    }
}