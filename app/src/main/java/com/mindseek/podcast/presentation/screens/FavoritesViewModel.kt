package com.mindseek.podcast.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.domain.usecase.GetAllFavoritesUseCase
import com.mindseek.podcast.domain.usecase.RemoveFromFavoritesUseCase
import com.mindseek.podcast.domain.usecase.SearchFavoritesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val favorites: List<EpisodeDomain> = emptyList(),
    val filteredFavorites: List<EpisodeDomain> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val selectedEpisodes: Set<String> = emptySet(),
    val isSelectionMode: Boolean = false
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getAllFavoritesUseCase: GetAllFavoritesUseCase,
    private val searchFavoritesUseCase: SearchFavoritesUseCase,
    private val removeFromFavoritesUseCase: RemoveFromFavoritesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            getAllFavoritesUseCase()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "加载收藏列表失败"
                    )
                }
                .collect { favorites ->
                    _uiState.value = _uiState.value.copy(
                        favorites = favorites,
                        filteredFavorites = if (_uiState.value.searchQuery.isBlank()) favorites else _uiState.value.filteredFavorites,
                        isLoading = false,
                        errorMessage = null
                    )
                }
        }
    }

    fun searchFavorites(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            isSearching = query.isNotBlank()
        )
        
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                filteredFavorites = _uiState.value.favorites,
                isSearching = false
            )
            return
        }
        
        viewModelScope.launch {
            searchFavoritesUseCase(query)
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "搜索失败: ${exception.message}"
                    )
                }
                .collect { filteredFavorites ->
                    _uiState.value = _uiState.value.copy(
                        filteredFavorites = filteredFavorites
                    )
                }
        }
    }

    fun clearSearchQuery() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            filteredFavorites = _uiState.value.favorites,
            isSearching = false
        )
    }

    fun removeFromFavorites(episodeId: String) {
        viewModelScope.launch {
            try {
                val success = removeFromFavoritesUseCase(episodeId)
                if (!success) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "取消收藏失败"
                    )
                }
                // The UI will automatically update through the Flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "取消收藏失败: ${e.message}"
                )
            }
        }
    }

    fun toggleEpisodeSelection(episodeId: String) {
        val currentSelection = _uiState.value.selectedEpisodes
        val newSelection = if (currentSelection.contains(episodeId)) {
            currentSelection - episodeId
        } else {
            currentSelection + episodeId
        }
        
        _uiState.value = _uiState.value.copy(
            selectedEpisodes = newSelection,
            isSelectionMode = newSelection.isNotEmpty()
        )
    }

    fun selectAllEpisodes() {
        val allEpisodeIds = _uiState.value.filteredFavorites.map { it.id }.toSet()
        _uiState.value = _uiState.value.copy(
            selectedEpisodes = allEpisodeIds,
            isSelectionMode = true
        )
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(
            selectedEpisodes = emptySet(),
            isSelectionMode = false
        )
    }

    fun removeSelectedFromFavorites() {
        viewModelScope.launch {
            val selectedIds = _uiState.value.selectedEpisodes
            var failedCount = 0
            
            selectedIds.forEach { episodeId ->
                try {
                    val success = removeFromFavoritesUseCase(episodeId)
                    if (!success) {
                        failedCount++
                    }
                } catch (e: Exception) {
                    failedCount++
                }
            }
            
            if (failedCount > 0) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "有${failedCount}个节目取消收藏失败"
                )
            }
            
            // Clear selection after operation
            clearSelection()
        }
    }

    fun refreshFavorites() {
        loadFavorites()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}