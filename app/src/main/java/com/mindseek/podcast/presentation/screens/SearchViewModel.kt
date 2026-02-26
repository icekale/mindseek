package com.mindseek.podcast.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindseek.podcast.data.local.entity.SearchHistory
import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.domain.model.PodcastDomain
import com.mindseek.podcast.domain.model.Resource
import com.mindseek.podcast.domain.repository.SearchRepository
import com.mindseek.podcast.domain.usecase.SearchEpisodesUseCase
import com.mindseek.podcast.domain.usecase.SearchPodcastsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Search screen UI state
 */
data class SearchUiState(
    val query: String = "",
    val searchResults: SearchResults = SearchResults(),
    val searchHistory: List<SearchHistory> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchType: SearchType = SearchType.PODCASTS,
    val searchFilters: SearchFilters = SearchFilters()
)

/**
 * Search results container
 */
data class SearchResults(
    val podcasts: List<PodcastDomain> = emptyList(),
    val episodes: List<EpisodeDomain> = emptyList(),
    val totalResults: Int = 0
)

/**
 * Search type enum
 */
enum class SearchType {
    PODCASTS, EPISODES, ALL
}

/**
 * Search filters
 */
data class SearchFilters(
    val category: String? = null,
    val sortBy: SortBy = SortBy.RELEVANCE
)

/**
 * Sort options
 */
enum class SortBy {
    RELEVANCE, POPULAR, RECENT
}

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchPodcastsUseCase: SearchPodcastsUseCase,
    private val searchEpisodesUseCase: SearchEpisodesUseCase,
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    
    init {
        // Load search history
        loadSearchHistory()
        
        // Set up real-time search with debounce
        _searchQuery
            .debounce(800) // Wait 800ms after user stops typing
            .distinctUntilChanged()
            .onEach { query ->
                // Only perform search, don't update query in UI state here
                // because it's already updated immediately in updateSearchQuery
                if (query.isNotBlank()) {
                    performSearch(query)
                } else {
                    clearSearchResults()
                }
            }
            .launchIn(viewModelScope)
    }

    fun updateSearchQuery(query: String) {
        // Immediately update UI state to show user input
        _uiState.value = _uiState.value.copy(query = query)
        // Update search query for debounced search
        _searchQuery.value = query
    }

    fun selectSearchType(searchType: SearchType) {
        _uiState.value = _uiState.value.copy(searchType = searchType)
        val currentQuery = _uiState.value.query
        if (currentQuery.isNotBlank()) {
            performSearch(currentQuery)
        }
    }

    fun updateSearchFilters(filters: SearchFilters) {
        _uiState.value = _uiState.value.copy(searchFilters = filters)
        val currentQuery = _uiState.value.query
        if (currentQuery.isNotBlank()) {
            performSearch(currentQuery)
        }
    }

    fun selectHistoryItem(searchHistory: SearchHistory) {
        // Immediately update UI state to show selected query
        _uiState.value = _uiState.value.copy(query = searchHistory.query)
        // Update search query for debounced search
        _searchQuery.value = searchHistory.query
    }

    fun deleteHistoryItem(query: String) {
        viewModelScope.launch {
            try {
                searchRepository.deleteSearchHistory(query)
                loadSearchHistory()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "删除搜索历史失败: ${e.message}"
                )
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            try {
                searchRepository.clearAllSearchHistory()
                loadSearchHistory()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "清除搜索历史失败: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun loadSearchHistory() {
        viewModelScope.launch {
            searchRepository.getRecentSearchHistory(10)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "加载搜索历史失败: ${e.message}"
                    )
                }
                .collect { history ->
                    _uiState.value = _uiState.value.copy(searchHistory = history)
                }
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                when (_uiState.value.searchType) {
                    SearchType.PODCASTS -> searchPodcasts(query)
                    SearchType.EPISODES -> searchEpisodes(query)
                    SearchType.ALL -> searchAll(query)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "搜索失败: ${e.message}"
                )
            }
        }
    }

    private suspend fun searchPodcasts(query: String) {
        searchPodcastsUseCase(query).collect { resource ->
            when (resource) {
                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
                is Resource.Success -> {
                    val podcasts = resource.data ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        searchResults = SearchResults(
                            podcasts = podcasts,
                            episodes = emptyList(),
                            totalResults = podcasts.size
                        )
                    )
                    
                    // Save search history
                    searchRepository.saveSearchHistory(query, podcasts.size)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = resource.message
                    )
                }
            }
        }
    }

    private suspend fun searchEpisodes(query: String) {
        searchEpisodesUseCase(query).collect { resource ->
            when (resource) {
                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
                is Resource.Success -> {
                    val episodes = resource.data ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        searchResults = SearchResults(
                            podcasts = emptyList(),
                            episodes = episodes,
                            totalResults = episodes.size
                        )
                    )
                    
                    // Save search history
                    searchRepository.saveSearchHistory(query, episodes.size)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = resource.message
                    )
                }
            }
        }
    }

    private suspend fun searchAll(query: String) {
        // Launch both searches concurrently
        val podcastsFlow = searchPodcastsUseCase(query)
        val episodesFlow = searchEpisodesUseCase(query)

        combine(podcastsFlow, episodesFlow) { podcastsResource, episodesResource ->
            Pair(podcastsResource, episodesResource)
        }.collect { (podcastsResource, episodesResource) ->
            val isLoading = podcastsResource is Resource.Loading || episodesResource is Resource.Loading
            
            if (isLoading) {
                _uiState.value = _uiState.value.copy(isLoading = true)
                return@collect
            }

            val podcasts = if (podcastsResource is Resource.Success) {
                podcastsResource.data ?: emptyList()
            } else emptyList()

            val episodes = if (episodesResource is Resource.Success) {
                episodesResource.data ?: emptyList()
            } else emptyList()

            val errorMessage = when {
                podcastsResource is Resource.Error && episodesResource is Resource.Error -> {
                    "搜索失败: ${podcastsResource.message}"
                }
                podcastsResource is Resource.Error -> podcastsResource.message
                episodesResource is Resource.Error -> episodesResource.message
                else -> null
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                searchResults = SearchResults(
                    podcasts = podcasts,
                    episodes = episodes,
                    totalResults = podcasts.size + episodes.size
                ),
                errorMessage = errorMessage
            )

            // Save search history
            if (errorMessage == null) {
                searchRepository.saveSearchHistory(query, podcasts.size + episodes.size)
            }
        }
    }

    private fun clearSearchResults() {
        _uiState.value = _uiState.value.copy(
            searchResults = SearchResults(),
            isLoading = false,
            errorMessage = null
        )
    }
}