package com.mindseek.podcast.presentation.screens

import com.mindseek.podcast.domain.model.PlayHistoryDomain
import com.mindseek.podcast.domain.usecase.ClearPlayHistoryUseCase
import com.mindseek.podcast.domain.usecase.GetAllPlayHistoryUseCase
import com.mindseek.podcast.domain.usecase.SearchPlayHistoryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class HistoryViewModelTest {

    @Mock
    private lateinit var getAllPlayHistoryUseCase: GetAllPlayHistoryUseCase

    @Mock
    private lateinit var searchPlayHistoryUseCase: SearchPlayHistoryUseCase

    @Mock
    private lateinit var clearPlayHistoryUseCase: ClearPlayHistoryUseCase

    private lateinit var viewModel: HistoryViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val sampleHistory = listOf(
        PlayHistoryDomain(
            id = "1",
            episodeId = "episode1",
            episodeTitle = "Episode 1",
            podcastTitle = "Podcast 1",
            playPosition = 1000L,
            playDate = System.currentTimeMillis(),
            duration = 3600000L,
            imageUrl = "https://example.com/image1.jpg"
        ),
        PlayHistoryDomain(
            id = "2",
            episodeId = "episode2",
            episodeTitle = "Episode 2",
            podcastTitle = "Podcast 2",
            playPosition = 2000L,
            playDate = System.currentTimeMillis() - 86400000L,
            duration = 3000000L,
            imageUrl = "https://example.com/image2.jpg"
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        whenever(getAllPlayHistoryUseCase()).thenReturn(flowOf(sampleHistory))
        viewModel = HistoryViewModel(
            getAllPlayHistoryUseCase,
            searchPlayHistoryUseCase,
            clearPlayHistoryUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be loading`() = runTest {
        // Given - ViewModel is created
        val initialState = viewModel.uiState.value

        // Then
        assertTrue(initialState.isLoading)
        assertEquals(emptyList<PlayHistoryDomain>(), initialState.playHistory)
        assertEquals("", initialState.searchQuery)
        assertFalse(initialState.isSearching)
        assertFalse(initialState.showClearDialog)
    }

    @Test
    fun `should load play history successfully`() = runTest {
        // When
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(sampleHistory, state.playHistory)
        assertEquals(sampleHistory, state.filteredHistory)
        assertEquals(null, state.errorMessage)
        verify(getAllPlayHistoryUseCase).invoke()
    }

    @Test
    fun `should handle error when loading play history fails`() = runTest {
        // Given
        val errorMessage = "Network error"
        whenever(getAllPlayHistoryUseCase()).thenThrow(RuntimeException(errorMessage))
        
        // When
        viewModel = HistoryViewModel(
            getAllPlayHistoryUseCase,
            searchPlayHistoryUseCase,
            clearPlayHistoryUseCase
        )
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.errorMessage?.contains("加载播放历史失败") == true)
    }

    @Test
    fun `should search history successfully`() = runTest {
        // Given
        advanceUntilIdle()
        val searchQuery = "Episode 1"
        val filteredHistory = listOf(sampleHistory[0])
        whenever(searchPlayHistoryUseCase(searchQuery)).thenReturn(flowOf(filteredHistory))

        // When
        viewModel.searchHistory(searchQuery)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(searchQuery, state.searchQuery)
        assertTrue(state.isSearching)
        assertEquals(filteredHistory, state.filteredHistory)
        verify(searchPlayHistoryUseCase).invoke(searchQuery)
    }

    @Test
    fun `should clear search when query is blank`() = runTest {
        // Given
        advanceUntilIdle()
        viewModel.searchHistory("test")
        advanceUntilIdle()

        // When
        viewModel.searchHistory("")

        // Then
        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertFalse(state.isSearching)
        assertEquals(sampleHistory, state.filteredHistory)
    }

    @Test
    fun `should clear search query`() = runTest {
        // Given
        advanceUntilIdle()
        viewModel.searchHistory("test query")

        // When
        viewModel.clearSearchQuery()

        // Then
        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertFalse(state.isSearching)
        assertEquals(sampleHistory, state.filteredHistory)
    }

    @Test
    fun `should show clear dialog`() {
        // When
        viewModel.showClearDialog()

        // Then
        assertTrue(viewModel.uiState.value.showClearDialog)
    }

    @Test
    fun `should hide clear dialog`() {
        // Given
        viewModel.showClearDialog()

        // When
        viewModel.hideClearDialog()

        // Then
        assertFalse(viewModel.uiState.value.showClearDialog)
    }

    @Test
    fun `should clear all history successfully`() = runTest {
        // Given
        advanceUntilIdle()
        viewModel.showClearDialog()

        // When
        viewModel.clearAllHistory()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(emptyList<PlayHistoryDomain>(), state.playHistory)
        assertEquals(emptyList<PlayHistoryDomain>(), state.filteredHistory)
        assertFalse(state.showClearDialog)
        verify(clearPlayHistoryUseCase).invoke()
    }

    @Test
    fun `should handle error when clearing history fails`() = runTest {
        // Given
        advanceUntilIdle()
        val errorMessage = "Clear failed"
        whenever(clearPlayHistoryUseCase()).thenThrow(RuntimeException(errorMessage))
        viewModel.showClearDialog()

        // When
        viewModel.clearAllHistory()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.errorMessage?.contains("清除历史记录失败") == true)
        assertFalse(state.showClearDialog)
    }

    @Test
    fun `should refresh history`() = runTest {
        // Given
        advanceUntilIdle()

        // When
        viewModel.refreshHistory()
        advanceUntilIdle()

        // Then
        verify(getAllPlayHistoryUseCase).invoke()
    }

    @Test
    fun `should clear error message`() = runTest {
        // Given
        whenever(getAllPlayHistoryUseCase()).thenThrow(RuntimeException("Error"))
        viewModel = HistoryViewModel(
            getAllPlayHistoryUseCase,
            searchPlayHistoryUseCase,
            clearPlayHistoryUseCase
        )
        advanceUntilIdle()

        // When
        viewModel.clearError()

        // Then
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `should handle search error gracefully`() = runTest {
        // Given
        advanceUntilIdle()
        val searchQuery = "test"
        whenever(searchPlayHistoryUseCase(searchQuery)).thenThrow(RuntimeException("Search failed"))

        // When
        viewModel.searchHistory(searchQuery)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.errorMessage?.contains("搜索失败") == true)
    }
}