package com.mindseek.podcast.presentation.screens

import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.domain.usecase.GetAllFavoritesUseCase
import com.mindseek.podcast.domain.usecase.RemoveFromFavoritesUseCase
import com.mindseek.podcast.domain.usecase.SearchFavoritesUseCase
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
class FavoritesViewModelTest {

    @Mock
    private lateinit var getAllFavoritesUseCase: GetAllFavoritesUseCase

    @Mock
    private lateinit var searchFavoritesUseCase: SearchFavoritesUseCase

    @Mock
    private lateinit var removeFromFavoritesUseCase: RemoveFromFavoritesUseCase

    private lateinit var viewModel: FavoritesViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val sampleEpisodes = listOf(
        EpisodeDomain(
            id = "episode1",
            podcastId = "podcast1",
            title = "Episode 1",
            description = "Description 1",
            audioUrl = "https://example.com/audio1.mp3",
            duration = 3600000L,
            publishDate = System.currentTimeMillis(),
            imageUrl = "https://example.com/image1.jpg",
            isDownloaded = false,
            localPath = null,
            isFavorite = true
        ),
        EpisodeDomain(
            id = "episode2",
            podcastId = "podcast2",
            title = "Episode 2",
            description = "Description 2",
            audioUrl = "https://example.com/audio2.mp3",
            duration = 2400000L,
            publishDate = System.currentTimeMillis() - 86400000L,
            imageUrl = "https://example.com/image2.jpg",
            isDownloaded = false,
            localPath = null,
            isFavorite = true
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        whenever(getAllFavoritesUseCase()).thenReturn(flowOf(sampleEpisodes))
        viewModel = FavoritesViewModel(
            getAllFavoritesUseCase,
            searchFavoritesUseCase,
            removeFromFavoritesUseCase
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
        assertEquals(emptyList<EpisodeDomain>(), initialState.favorites)
        assertEquals("", initialState.searchQuery)
        assertFalse(initialState.isSearching)
        assertFalse(initialState.isSelectionMode)
        assertEquals(emptySet<String>(), initialState.selectedEpisodes)
    }

    @Test
    fun `should load favorites successfully`() = runTest {
        // When
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(sampleEpisodes, state.favorites)
        assertEquals(sampleEpisodes, state.filteredFavorites)
        assertEquals(null, state.errorMessage)
        verify(getAllFavoritesUseCase).invoke()
    }

    @Test
    fun `should handle error when loading favorites fails`() = runTest {
        // Given
        val errorMessage = "Network error"
        whenever(getAllFavoritesUseCase()).thenThrow(RuntimeException(errorMessage))
        
        // When
        viewModel = FavoritesViewModel(
            getAllFavoritesUseCase,
            searchFavoritesUseCase,
            removeFromFavoritesUseCase
        )
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.errorMessage?.contains("加载收藏列表失败") == true)
    }

    @Test
    fun `should search favorites successfully`() = runTest {
        // Given
        advanceUntilIdle()
        val searchQuery = "Episode 1"
        val filteredFavorites = listOf(sampleEpisodes[0])
        whenever(searchFavoritesUseCase(searchQuery)).thenReturn(flowOf(filteredFavorites))

        // When
        viewModel.searchFavorites(searchQuery)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(searchQuery, state.searchQuery)
        assertTrue(state.isSearching)
        assertEquals(filteredFavorites, state.filteredFavorites)
        verify(searchFavoritesUseCase).invoke(searchQuery)
    }

    @Test
    fun `should clear search when query is blank`() = runTest {
        // Given
        advanceUntilIdle()
        viewModel.searchFavorites("test")
        advanceUntilIdle()

        // When
        viewModel.searchFavorites("")

        // Then
        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertFalse(state.isSearching)
        assertEquals(sampleEpisodes, state.filteredFavorites)
    }

    @Test
    fun `should clear search query`() = runTest {
        // Given
        advanceUntilIdle()
        viewModel.searchFavorites("test query")

        // When
        viewModel.clearSearchQuery()

        // Then
        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertFalse(state.isSearching)
        assertEquals(sampleEpisodes, state.filteredFavorites)
    }

    @Test
    fun `should remove from favorites successfully`() = runTest {
        // Given
        advanceUntilIdle()
        val episodeId = "episode1"
        whenever(removeFromFavoritesUseCase(episodeId)).thenReturn(true)

        // When
        viewModel.removeFromFavorites(episodeId)
        advanceUntilIdle()

        // Then
        verify(removeFromFavoritesUseCase).invoke(episodeId)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `should handle error when removing from favorites fails`() = runTest {
        // Given
        advanceUntilIdle()
        val episodeId = "episode1"
        whenever(removeFromFavoritesUseCase(episodeId)).thenReturn(false)

        // When
        viewModel.removeFromFavorites(episodeId)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.errorMessage?.contains("取消收藏失败") == true)
    }

    @Test
    fun `should toggle episode selection`() = runTest {
        // Given
        advanceUntilIdle()
        val episodeId = "episode1"

        // When - select episode
        viewModel.toggleEpisodeSelection(episodeId)

        // Then
        var state = viewModel.uiState.value
        assertTrue(state.selectedEpisodes.contains(episodeId))
        assertTrue(state.isSelectionMode)

        // When - deselect episode
        viewModel.toggleEpisodeSelection(episodeId)

        // Then
        state = viewModel.uiState.value
        assertFalse(state.selectedEpisodes.contains(episodeId))
        assertFalse(state.isSelectionMode)
    }

    @Test
    fun `should select all episodes`() = runTest {
        // Given
        advanceUntilIdle()

        // When
        viewModel.selectAllEpisodes()

        // Then
        val state = viewModel.uiState.value
        assertEquals(sampleEpisodes.map { it.id }.toSet(), state.selectedEpisodes)
        assertTrue(state.isSelectionMode)
    }

    @Test
    fun `should clear selection`() = runTest {
        // Given
        advanceUntilIdle()
        viewModel.selectAllEpisodes()

        // When
        viewModel.clearSelection()

        // Then
        val state = viewModel.uiState.value
        assertEquals(emptySet<String>(), state.selectedEpisodes)
        assertFalse(state.isSelectionMode)
    }

    @Test
    fun `should remove selected episodes from favorites`() = runTest {
        // Given
        advanceUntilIdle()
        viewModel.toggleEpisodeSelection("episode1")
        viewModel.toggleEpisodeSelection("episode2")
        whenever(removeFromFavoritesUseCase("episode1")).thenReturn(true)
        whenever(removeFromFavoritesUseCase("episode2")).thenReturn(true)

        // When
        viewModel.removeSelectedFromFavorites()
        advanceUntilIdle()

        // Then
        verify(removeFromFavoritesUseCase).invoke("episode1")
        verify(removeFromFavoritesUseCase).invoke("episode2")
        val state = viewModel.uiState.value
        assertEquals(emptySet<String>(), state.selectedEpisodes)
        assertFalse(state.isSelectionMode)
    }

    @Test
    fun `should handle partial failure when removing selected episodes`() = runTest {
        // Given
        advanceUntilIdle()
        viewModel.toggleEpisodeSelection("episode1")
        viewModel.toggleEpisodeSelection("episode2")
        whenever(removeFromFavoritesUseCase("episode1")).thenReturn(true)
        whenever(removeFromFavoritesUseCase("episode2")).thenReturn(false)

        // When
        viewModel.removeSelectedFromFavorites()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.errorMessage?.contains("有1个节目取消收藏失败") == true)
        assertEquals(emptySet<String>(), state.selectedEpisodes)
        assertFalse(state.isSelectionMode)
    }

    @Test
    fun `should refresh favorites`() = runTest {
        // Given
        advanceUntilIdle()

        // When
        viewModel.refreshFavorites()
        advanceUntilIdle()

        // Then
        verify(getAllFavoritesUseCase).invoke()
    }

    @Test
    fun `should clear error message`() = runTest {
        // Given
        whenever(getAllFavoritesUseCase()).thenThrow(RuntimeException("Error"))
        viewModel = FavoritesViewModel(
            getAllFavoritesUseCase,
            searchFavoritesUseCase,
            removeFromFavoritesUseCase
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
        whenever(searchFavoritesUseCase(searchQuery)).thenThrow(RuntimeException("Search failed"))

        // When
        viewModel.searchFavorites(searchQuery)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.errorMessage?.contains("搜索失败") == true)
    }
}