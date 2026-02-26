package com.mindseek.podcast.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindseek.podcast.data.local.entity.Podcast
import com.mindseek.podcast.domain.usecase.GetSubscribedPodcastsUseCase
import com.mindseek.podcast.domain.usecase.UnsubscribeFromPodcastUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubscriptionUiState(
    val subscribedPodcasts: List<Podcast> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasNewUpdates: Map<String, Boolean> = emptyMap()
)

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val getSubscribedPodcastsUseCase: GetSubscribedPodcastsUseCase,
    private val unsubscribeFromPodcastUseCase: UnsubscribeFromPodcastUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        loadSubscribedPodcasts()
    }

    private fun loadSubscribedPodcasts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            getSubscribedPodcastsUseCase()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "加载订阅列表失败"
                    )
                }
                .collect { podcasts ->
                    _uiState.value = _uiState.value.copy(
                        subscribedPodcasts = podcasts,
                        isLoading = false,
                        errorMessage = null,
                        hasNewUpdates = checkForNewUpdates(podcasts)
                    )
                }
        }
    }

    fun unsubscribeFromPodcast(podcastId: String) {
        viewModelScope.launch {
            try {
                unsubscribeFromPodcastUseCase(podcastId)
                // The UI will automatically update through the Flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "取消订阅失败: ${e.message}"
                )
            }
        }
    }

    fun refreshSubscriptions() {
        loadSubscribedPodcasts()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun checkForNewUpdates(podcasts: List<Podcast>): Map<String, Boolean> {
        // Check if podcasts have been updated recently (within last 24 hours)
        val currentTime = System.currentTimeMillis()
        val oneDayInMillis = 24 * 60 * 60 * 1000L
        
        return podcasts.associate { podcast ->
            podcast.id to (currentTime - podcast.lastUpdated < oneDayInMillis)
        }
    }
}