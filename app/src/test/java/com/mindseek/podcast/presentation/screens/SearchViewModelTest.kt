package com.mindseek.podcast.presentation.screens

import com.mindseek.podcast.data.local.entity.SearchHistory
import com.mindseek.podcast.domain.model.PodcastDomain
import com.mindseek.podcast.domain.model.Resource
import com.mindseek.podcast.domain.repository.SearchRepository
import com.mindseek.podcast.domain.usecase.SearchEpisodesUseCase
import com.mindseek.podcast.domain.usecase.SearchPodcastsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @Mock
    private lateinit var searchPodcastsUseCase: SearchPodcastsUseCase

    @Mock
    private lateinit var searchEpisodesUseCase: SearchEpisodesUseCase

    @Mock
    private lateinit var searchRepository: SearchRepository

    private lateinit var viewModel: SearchViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        // Setup default mock behaviors
        whenever(searchRepository.getRecentSearchHistory(10))
            .thenReturn(flowOf(emptyList()))
        
        viewModel = SearchViewModel(
            searchPodcastsUseCase = searchPodcastsUseCase,
            searchEpisodesUseCase = searchEpisodesUseCase,
            searchRepository = searchRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be correct`() {
        val initialState = viewModel.uiState.value
        
        assertEquals("", initialState.query)
        assertEquals(SearchType.PODCASTS, initialState.searchType)
        assertFalse(initialState.isLoading)
        assertEquals(0, initialState.searchResults.totalResults)
    }

    @Test
    fun `updateSearchQuery should update query in state`() = runTest {
        val query = "test query"
        
        viewModel.updateSearchQuery(query)
        advanceUntilIdle()
        
        assertEquals(query, viewModel.uiState.value.query)
    }

    @Test
    fun `selectSearchType should update search type`() {
        viewModel.selectSearchType(SearchType.EPISODES)
        
        assertEquals(SearchType.EPISODES, viewModel.uiState.value.searchType)
    }

    @Test
    fun `search should call appropriate use case based on search type`() = runTest {
        val query = "test"
        val mockPodcasts = listOf(
            PodcastDomain(
                id = "1",
                title = "Test Podcast",
                description = "Description",
                imageUrl = "url",
                author = "Author",
                category = "Category",
                isSubscribed = false,
                lastUpdated = System.currentTimeMillis(),
                episodeCount = 0
            )
        )
        
        whenever(searchPodcastsUseCase(query))
            .thenReturn(flowOf(Resource.Success(mockPodcasts)))
        
        viewModel.selectSearchType(SearchType.PODCASTS)
        viewModel.updateSearchQuery(query)
        advanceUntilIdle()
        
        verify(searchPodcastsUseCase).invoke(query)
        assertEquals(1, viewModel.uiState.value.searchResults.podcasts.size)
    }

    @Test
    fun `selectHistoryItem should update search query`() = runTest {
        val searchHistory = SearchHistory(
            id = "1",
            query = "history query",
            timestamp = System.currentTimeMillis(),
            resultCount = 5
        )
        
        viewModel.selectHistoryItem(searchHistory)
        advanceUntilIdle()
        
        assertEquals(searchHistory.query, viewModel.uiState.value.query)
    }

    @Test
    fun `deleteHistoryItem should call repository delete method`() = runTest {
        val query = "test query"
        
        viewModel.deleteHistoryItem(query)
        advanceUntilIdle()
        
        verify(searchRepository).deleteSearchHistory(query)
    }

    @Test
    fun `clearAllHistory should call repository clear method`() = runTest {
        viewModel.clearAllHistory()
        advanceUntilIdle()
        
        verify(searchRepository).clearAllSearchHistory()
    }

    @Test
    fun `search loading state should be handled correctly`() = runTest {
        val query = "test"
        
        whenever(searchPodcastsUseCase(query))
            .thenReturn(flowOf(Resource.Loading()))
        
        viewModel.updateSearchQuery(query)
        advanceUntilIdle()
        
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `search error state should be handled correctly`() = runTest {
        val query = "test"
        val errorMessage = "Search failed"
        
        whenever(searchPodcastsUseCase(query))
            .thenReturn(flowOf(Resource.Error(errorMessage)))
        
        viewModel.updateSearchQuery(query)
        advanceUntilIdle()
        
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(errorMessage, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `clearError should clear error message`() {
        // Set an error state first
        viewModel.updateSearchQuery("test")
        
        viewModel.clearError()
        
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }
}