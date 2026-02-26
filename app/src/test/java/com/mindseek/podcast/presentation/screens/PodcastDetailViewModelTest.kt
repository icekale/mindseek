package com.mindseek.podcast.presentation.screens

import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.domain.model.PodcastDomain
import com.mindseek.podcast.domain.usecase.GetPodcastDetailUseCase
import com.mindseek.podcast.domain.usecase.GetPodcastEpisodesUseCase
import com.mindseek.podcast.domain.usecase.SubscribeToPodcastUseCase
import com.mindseek.podcast.domain.usecase.UnsubscribeFromPodcastUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PodcastDetailViewModelTest {

    private lateinit var getPodcastDetailUseCase: GetPodcastDetailUseCase
    private lateinit var getPodcastEpisodesUseCase: GetPodcastEpisodesUseCase
    private lateinit var subscribeToPodcastUseCase: SubscribeToPodcastUseCase
    private lateinit var unsubscribeFromPodcastUseCase: UnsubscribeFromPodcastUseCase
    private lateinit var viewModel: PodcastDetailViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        getPodcastDetailUseCase = mockk()
        getPodcastEpisodesUseCase = mockk()
        subscribeToPodcastUseCase = mockk(relaxed = true)
        unsubscribeFromPodcastUseCase = mockk(relaxed = true)
        
        viewModel = PodcastDetailViewModel(
            getPodcastDetailUseCase,
            getPodcastEpisodesUseCase,
            subscribeToPodcastUseCase,
            unsubscribeFromPodcastUseCase
        )
    }

    @Test
    fun `loadPodcastDetail should update uiState with podcast and episodes`() = runTest {
        // Given
        val podcastId = "test-podcast-id"
        val podcast = PodcastDomain(
            id = podcastId,
            title = "Test Podcast",
            description = "Test Description",
            imageUrl = "https://example.com/image.jpg",
            author = "Test Author",
            category = "Technology",
            isSubscribed = false,
            lastUpdated = System.currentTimeMillis()
        )
        val episodes = listOf(
            EpisodeDomain(
                id = "episode-1",
                podcastId = podcastId,
                title = "Episode 1",
                description = "Episode 1 Description",
                audioUrl = "https://example.com/audio1.mp3",
                duration = 3600000L,
                publishDate = System.currentTimeMillis()
            )
        )

        coEvery { getPodcastDetailUseCase(podcastId) } returns podcast
        coEvery { getPodcastEpisodesUseCase(podcastId) } returns flowOf(episodes)

        // When
        viewModel.loadPodcastDetail(podcastId)
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertEquals(podcast, uiState.podcast)
        assertEquals(episodes, uiState.episodes)
        assertFalse(uiState.isLoading)
        assertNull(uiState.errorMessage)
    }

    @Test
    fun `loadPodcastDetail should handle error when use case throws exception`() = runTest {
        // Given
        val podcastId = "test-podcast-id"
        val errorMessage = "Network error"
        coEvery { getPodcastDetailUseCase(podcastId) } throws Exception(errorMessage)

        // When
        viewModel.loadPodcastDetail(podcastId)
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertNull(uiState.podcast)
        assertTrue(uiState.episodes.isEmpty())
        assertFalse(uiState.isLoading)
        assertNotNull(uiState.errorMessage)
        assertTrue(uiState.errorMessage!!.contains(errorMessage))
    }

    @Test
    fun `toggleSubscription should subscribe when podcast is not subscribed`() = runTest {
        // Given
        val podcastId = "test-podcast-id"
        val podcast = PodcastDomain(
            id = podcastId,
            title = "Test Podcast",
            description = "Test Description",
            imageUrl = "https://example.com/image.jpg",
            author = "Test Author",
            category = "Technology",
            isSubscribed = false,
            lastUpdated = System.currentTimeMillis()
        )

        coEvery { getPodcastDetailUseCase(podcastId) } returns podcast
        coEvery { getPodcastEpisodesUseCase(podcastId) } returns flowOf(emptyList())

        // Load podcast first
        viewModel.loadPodcastDetail(podcastId)
        advanceUntilIdle()

        // When
        viewModel.toggleSubscription()
        advanceUntilIdle()

        // Then
        coVerify { subscribeToPodcastUseCase(podcastId) }
        val uiState = viewModel.uiState.value
        assertTrue(uiState.podcast?.isSubscribed == true)
        assertFalse(uiState.isSubscribing)
    }

    @Test
    fun `toggleSubscription should unsubscribe when podcast is subscribed`() = runTest {
        // Given
        val podcastId = "test-podcast-id"
        val podcast = PodcastDomain(
            id = podcastId,
            title = "Test Podcast",
            description = "Test Description",
            imageUrl = "https://example.com/image.jpg",
            author = "Test Author",
            category = "Technology",
            isSubscribed = true,
            lastUpdated = System.currentTimeMillis()
        )

        coEvery { getPodcastDetailUseCase(podcastId) } returns podcast
        coEvery { getPodcastEpisodesUseCase(podcastId) } returns flowOf(emptyList())

        // Load podcast first
        viewModel.loadPodcastDetail(podcastId)
        advanceUntilIdle()

        // When
        viewModel.toggleSubscription()
        advanceUntilIdle()

        // Then
        coVerify { unsubscribeFromPodcastUseCase(podcastId) }
        val uiState = viewModel.uiState.value
        assertFalse(uiState.podcast?.isSubscribed == true)
        assertFalse(uiState.isSubscribing)
    }

    @Test
    fun `clearError should clear error message`() = runTest {
        // Given
        val podcastId = "test-podcast-id"
        coEvery { getPodcastDetailUseCase(podcastId) } throws Exception("Test error")

        viewModel.loadPodcastDetail(podcastId)
        advanceUntilIdle()

        // Verify error is set
        assertNotNull(viewModel.uiState.value.errorMessage)

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.uiState.value.errorMessage)
    }
}