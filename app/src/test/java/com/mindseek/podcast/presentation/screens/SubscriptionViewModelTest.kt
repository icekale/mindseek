package com.mindseek.podcast.presentation.screens

import com.mindseek.podcast.data.local.entity.Podcast
import com.mindseek.podcast.domain.usecase.GetSubscribedPodcastsUseCase
import com.mindseek.podcast.domain.usecase.UnsubscribeFromPodcastUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionViewModelTest {

    private lateinit var getSubscribedPodcastsUseCase: GetSubscribedPodcastsUseCase
    private lateinit var unsubscribeFromPodcastUseCase: UnsubscribeFromPodcastUseCase
    private lateinit var viewModel: SubscriptionViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getSubscribedPodcastsUseCase = mockk()
        unsubscribeFromPodcastUseCase = mockk()
    }

    @Test
    fun `initial state should be loading`() = runTest {
        // Given
        every { getSubscribedPodcastsUseCase() } returns flowOf(emptyList())

        // When
        viewModel = SubscriptionViewModel(getSubscribedPodcastsUseCase, unsubscribeFromPodcastUseCase)

        // Then
        assertTrue(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.subscribedPodcasts.isEmpty())
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `should load subscribed podcasts successfully`() = runTest {
        // Given
        val subscribedPodcasts = listOf(
            Podcast(
                id = "1",
                title = "Test Podcast",
                description = "Description",
                imageUrl = "image.jpg",
                author = "Author",
                category = "Technology",
                isSubscribed = true,
                lastUpdated = System.currentTimeMillis()
            )
        )
        every { getSubscribedPodcastsUseCase() } returns flowOf(subscribedPodcasts)

        // When
        viewModel = SubscriptionViewModel(getSubscribedPodcastsUseCase, unsubscribeFromPodcastUseCase)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(subscribedPodcasts, viewModel.uiState.value.subscribedPodcasts)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `should handle error when loading subscribed podcasts fails`() = runTest {
        // Given
        val errorMessage = "Network error"
        every { getSubscribedPodcastsUseCase() } throws RuntimeException(errorMessage)

        // When
        viewModel = SubscriptionViewModel(getSubscribedPodcastsUseCase, unsubscribeFromPodcastUseCase)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.subscribedPodcasts.isEmpty())
        assertEquals("加载订阅列表失败", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `should unsubscribe from podcast successfully`() = runTest {
        // Given
        val podcastId = "1"
        every { getSubscribedPodcastsUseCase() } returns flowOf(emptyList())
        coEvery { unsubscribeFromPodcastUseCase(podcastId) } returns Unit
        
        viewModel = SubscriptionViewModel(getSubscribedPodcastsUseCase, unsubscribeFromPodcastUseCase)
        advanceUntilIdle()

        // When
        viewModel.unsubscribeFromPodcast(podcastId)
        advanceUntilIdle()

        // Then
        coVerify { unsubscribeFromPodcastUseCase(podcastId) }
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `should handle error when unsubscribing fails`() = runTest {
        // Given
        val podcastId = "1"
        val errorMessage = "Unsubscribe failed"
        every { getSubscribedPodcastsUseCase() } returns flowOf(emptyList())
        coEvery { unsubscribeFromPodcastUseCase(podcastId) } throws RuntimeException(errorMessage)
        
        viewModel = SubscriptionViewModel(getSubscribedPodcastsUseCase, unsubscribeFromPodcastUseCase)
        advanceUntilIdle()

        // When
        viewModel.unsubscribeFromPodcast(podcastId)
        advanceUntilIdle()

        // Then
        assertEquals("取消订阅失败: $errorMessage", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `should refresh subscriptions`() = runTest {
        // Given
        every { getSubscribedPodcastsUseCase() } returns flowOf(emptyList())
        viewModel = SubscriptionViewModel(getSubscribedPodcastsUseCase, unsubscribeFromPodcastUseCase)
        advanceUntilIdle()

        // When
        viewModel.refreshSubscriptions()

        // Then
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `should clear error message`() = runTest {
        // Given
        every { getSubscribedPodcastsUseCase() } returns flowOf(emptyList())
        viewModel = SubscriptionViewModel(getSubscribedPodcastsUseCase, unsubscribeFromPodcastUseCase)
        advanceUntilIdle()
        
        // Set an error message
        viewModel.unsubscribeFromPodcast("invalid")
        coEvery { unsubscribeFromPodcastUseCase("invalid") } throws RuntimeException("Error")
        advanceUntilIdle()

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `should detect new updates correctly`() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val recentPodcast = Podcast(
            id = "1",
            title = "Recent Podcast",
            description = "Description",
            imageUrl = "image.jpg",
            author = "Author",
            category = "Technology",
            isSubscribed = true,
            lastUpdated = currentTime - (12 * 60 * 60 * 1000L) // 12 hours ago
        )
        val oldPodcast = Podcast(
            id = "2",
            title = "Old Podcast",
            description = "Description",
            imageUrl = "image.jpg",
            author = "Author",
            category = "Technology",
            isSubscribed = true,
            lastUpdated = currentTime - (48 * 60 * 60 * 1000L) // 48 hours ago
        )
        
        every { getSubscribedPodcastsUseCase() } returns flowOf(listOf(recentPodcast, oldPodcast))

        // When
        viewModel = SubscriptionViewModel(getSubscribedPodcastsUseCase, unsubscribeFromPodcastUseCase)
        advanceUntilIdle()

        // Then
        val hasNewUpdates = viewModel.uiState.value.hasNewUpdates
        assertTrue(hasNewUpdates["1"] ?: false) // Recent podcast should have new update
        assertFalse(hasNewUpdates["2"] ?: true) // Old podcast should not have new update
    }
}