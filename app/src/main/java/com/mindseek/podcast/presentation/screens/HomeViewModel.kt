package com.mindseek.podcast.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindseek.podcast.core.error.ErrorHandler
import com.mindseek.podcast.core.error.RetryManager
import com.mindseek.podcast.core.error.executeWithErrorHandling
import com.mindseek.podcast.core.error.executeWithRetry
import com.mindseek.podcast.domain.usecase.GetRecommendedPodcastsUseCase
import com.mindseek.podcast.presentation.ui.state.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRecommendedPodcastsUseCase: GetRecommendedPodcastsUseCase,
    private val errorHandler: ErrorHandler,
    private val retryManager: RetryManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var currentPage = 1
    private var isLoadingMore = false

    init {
        loadRecommendedPodcasts()
    }

    fun loadRecommendedPodcasts(refresh: Boolean = false) {
        if (refresh) {
            currentPage = 1
            _uiState.value = _uiState.value.copy(
                isRefreshing = true,
                errorMessage = null
            )
        } else if (_uiState.value.isLoading || isLoadingMore) {
            return
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        executeWithRetry(
            errorHandler = errorHandler,
            retryManager = retryManager,
            context = "加载推荐播客",
            maxRetries = 3,
            onError = { throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = throwable.message ?: "加载推荐播客失败"
                )
                isLoadingMore = false
            }
        ) {
            val domainPodcasts = getRecommendedPodcastsUseCase(currentPage)

            _uiState.value = if (refresh || currentPage == 1) {
                _uiState.value.copy(
                    recommendedPodcasts = domainPodcasts,
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = null
                )
            } else {
                _uiState.value.copy(
                    recommendedPodcasts = _uiState.value.recommendedPodcasts + domainPodcasts,
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = null
                )
            }

            if (domainPodcasts.isNotEmpty()) {
                currentPage++
            }
            isLoadingMore = false
        }
    }

    fun loadMorePodcasts() {
        if (isLoadingMore || _uiState.value.isLoading || _uiState.value.isRefreshing) {
            return
        }

        isLoadingMore = true
        loadRecommendedPodcasts()
    }

    fun refresh() {
        loadRecommendedPodcasts(refresh = true)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun retryLastOperation() {
        loadRecommendedPodcasts(refresh = true)
    }
}