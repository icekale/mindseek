package com.mindseek.podcast.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindseek.podcast.domain.model.PlayHistoryDomain
import com.mindseek.podcast.domain.usecase.ClearPlayHistoryUseCase
import com.mindseek.podcast.domain.usecase.GetAllPlayHistoryUseCase
import com.mindseek.podcast.domain.usecase.SearchPlayHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val playHistory: List<PlayHistoryDomain> = emptyList(),
    val filteredHistory: List<PlayHistoryDomain> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val showClearDialog: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getAllPlayHistoryUseCase: GetAllPlayHistoryUseCase,
    private val searchPlayHistoryUseCase: SearchPlayHistoryUseCase,
    private val clearPlayHistoryUseCase: ClearPlayHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadPlayHistory()
    }

    private fun loadPlayHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            getAllPlayHistoryUseCase()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "加载播放历史失败"
                    )
                }
                .collect { history ->
                    _uiState.value = _uiState.value.copy(
                        playHistory = history,
                        filteredHistory = if (_uiState.value.searchQuery.isBlank()) history else _uiState.value.filteredHistory,
                        isLoading = false,
                        errorMessage = null
                    )
                }
        }
    }

    fun searchHistory(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            isSearching = query.isNotBlank()
        )
        
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                filteredHistory = _uiState.value.playHistory,
                isSearching = false
            )
            return
        }
        
        viewModelScope.launch {
            searchPlayHistoryUseCase(query)
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "搜索失败: ${exception.message}"
                    )
                }
                .collect { filteredHistory ->
                    _uiState.value = _uiState.value.copy(
                        filteredHistory = filteredHistory
                    )
                }
        }
    }

    fun clearSearchQuery() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            filteredHistory = _uiState.value.playHistory,
            isSearching = false
        )
    }

    fun showClearDialog() {
        _uiState.value = _uiState.value.copy(showClearDialog = true)
    }

    fun hideClearDialog() {
        _uiState.value = _uiState.value.copy(showClearDialog = false)
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            try {
                clearPlayHistoryUseCase()
                _uiState.value = _uiState.value.copy(
                    playHistory = emptyList(),
                    filteredHistory = emptyList(),
                    showClearDialog = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "清除历史记录失败: ${e.message}",
                    showClearDialog = false
                )
            }
        }
    }

    fun refreshHistory() {
        loadPlayHistory()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}