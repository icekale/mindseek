package com.mindseek.podcast.presentation.screens

import com.mindseek.podcast.core.error.ErrorHandler
import com.mindseek.podcast.core.error.RetryManager
import com.mindseek.podcast.data.repository.PodcastRepositoryImpl
import com.mindseek.podcast.domain.model.PodcastDomain
import com.mindseek.podcast.domain.usecase.GetRecommendedPodcastsUseCase
import com.mindseek.podcast.presentation.ui.state.HomeUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @Mock
    private lateinit var getRecommendedPodcastsUseCase: GetRecommendedPodcastsUseCase

    @Mock
    private lateinit var podcastRepository: PodcastRepositoryImpl

    @Mock
    private lateinit var errorHandler: ErrorHandler

    @Mock
    private lateinit var retryManager: RetryManager

    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be loading`() = runTest {
        // Given
        val mockPodcasts = listOf(
            PodcastDomain(
                id = "1",
                title = "Test Podcast",
                description = "Test Description",
                imageUrl = "test.jpg",
                author = "Test Author",
                category = "Test Category",
                lastUpdated = System.currentTimeMillis()
            )
        )
        whenever(getRecommendedPodcastsUseCase(1)).thenReturn(mockPodcasts)

        // When
        viewModel = HomeViewModel(getRecommendedPodcastsUseCase, podcastRepository, errorHandler, retryManager)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertEquals(mockPodcasts, state.recommendedPodcasts)
        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
        assertNull(state.errorMessage)
    }

    @Test
    fun `refresh should update podcasts`() = runTest {
        // Given
        val initialPodcasts = listOf(
            PodcastDomain(
                id = "1",
                title = "Initial Podcast",
                description = "Initial Description",
                imageUrl = "initial.jpg",
                author = "Initial Author",
                category = "Initial Category",
                lastUpdated = System.currentTimeMillis()
            )
        )
        val refreshedPodcasts = listOf(
            PodcastDomain(
                id = "2",
                title = "Refreshed Podcast",
                description = "Refreshed Description",
                imageUrl = "refreshed.jpg",
                author = "Refreshed Author",
                category = "Refreshed Category",
                lastUpdated = System.currentTimeMillis()
            )
        )

        whenever(getRecommendedPodcastsUseCase(1)).thenReturn(initialPodcasts, refreshedPodcasts)

        viewModel = HomeViewModel(getRecommendedPodcastsUseCase, podcastRepository, errorHandler, retryManager)
        advanceUntilIdle()

        // When
        viewModel.refresh()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertEquals(refreshedPodcasts, state.recommendedPodcasts)
        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
        assertNull(state.errorMessage)
    }

    @Test
    fun `error should be handled correctly`() = runTest {
        // Given
        val errorMessage = "Network error"
        whenever(getRecommendedPodcastsUseCase(1)).thenThrow(RuntimeException(errorMessage))

        // When
        viewModel = HomeViewModel(getRecommendedPodcastsUseCase, podcastRepository, errorHandler, retryManager)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.recommendedPodcasts.isEmpty())
        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
        assertEquals("加载推荐播客失败", state.errorMessage)
    }
}