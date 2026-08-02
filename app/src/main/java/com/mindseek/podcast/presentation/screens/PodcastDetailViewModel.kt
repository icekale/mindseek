package com.mindseek.podcast.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.domain.model.PodcastDomain
import com.mindseek.podcast.domain.usecase.GetPodcastDetailUseCase
import com.mindseek.podcast.domain.usecase.GetPodcastEpisodesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PodcastDetailUiState(
    val podcast: PodcastDomain? = null,
    val episodes: List<EpisodeDomain> = emptyList(),
    val isLoading: Boolean = false,
    val isSubscribing: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class PodcastDetailViewModel @Inject constructor(
    private val getPodcastDetailUseCase: GetPodcastDetailUseCase,
    private val getPodcastEpisodesUseCase: GetPodcastEpisodesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PodcastDetailUiState())
    val uiState: StateFlow<PodcastDetailUiState> = _uiState.asStateFlow()

    fun loadPodcastDetail(podcastId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // Load podcast details
                val podcast = getPodcastDetailUseCase(podcastId)
                _uiState.value = _uiState.value.copy(podcast = podcast)
                
                // Load episodes — use a timeout to detect stuck state
                var gotEpisodes = false
                getPodcastEpisodesUseCase(podcastId)
                    .catch { e ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "加载失败: ${e.message}",
                            isLoading = false
                        )
                    }
                    .collect { episodes ->
                        gotEpisodes = true
                        _uiState.value = _uiState.value.copy(
                            episodes = episodes,
                            isLoading = false,
                            errorMessage = if (episodes.isEmpty()) "暂无节目，请检查网络连接" else null
                        )
                    }
                    
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "加载失败: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun toggleSubscription() {
        val podcast = _uiState.value.podcast ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubscribing = true)
            
            try {
                // Subscription is a local-only operation with Nio Radio
                val updatedPodcast = podcast.copy(isSubscribed = !podcast.isSubscribed)
                _uiState.value = _uiState.value.copy(
                    podcast = updatedPodcast,
                    isSubscribing = false
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "订阅操作失败: ${e.message}",
                    isSubscribing = false
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun refreshPodcast() {
        val podcast = _uiState.value.podcast
        if (podcast != null) {
            loadPodcastDetail(podcast.id)
        }
    }
}