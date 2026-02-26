package com.mindseek.podcast.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindseek.podcast.domain.model.DownloadInfo
import com.mindseek.podcast.domain.usecase.download.DeleteDownloadUseCase
import com.mindseek.podcast.domain.usecase.download.GetDownloadsUseCase
import com.mindseek.podcast.domain.usecase.offline.CleanupStorageUseCase
import com.mindseek.podcast.domain.usecase.offline.GetStorageInfoUseCase
import com.mindseek.podcast.domain.usecase.offline.StorageInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StorageManagementUiState(
    val storageInfo: StorageInfo? = null,
    val downloads: List<DownloadInfo> = emptyList(),
    val isLoadingStorage: Boolean = false,
    val isLoadingDownloads: Boolean = false,
    val cleanupResult: String? = null,
    val error: String? = null
)

@HiltViewModel
class StorageManagementViewModel @Inject constructor(
    private val getStorageInfoUseCase: GetStorageInfoUseCase,
    private val getDownloadsUseCase: GetDownloadsUseCase,
    private val deleteDownloadUseCase: DeleteDownloadUseCase,
    private val cleanupStorageUseCase: CleanupStorageUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StorageManagementUiState())
    val uiState: StateFlow<StorageManagementUiState> = _uiState.asStateFlow()
    
    fun loadStorageInfo() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingStorage = true, error = null)
            
            try {
                val storageInfo = getStorageInfoUseCase()
                _uiState.value = _uiState.value.copy(
                    storageInfo = storageInfo,
                    isLoadingStorage = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingStorage = false,
                    error = "加载存储信息失败: ${e.message}"
                )
            }
        }
    }
    
    fun loadDownloads() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingDownloads = true, error = null)
            
            getDownloadsUseCase()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingDownloads = false,
                        error = "加载下载列表失败: ${e.message}"
                    )
                }
                .collect { downloads ->
                    _uiState.value = _uiState.value.copy(
                        downloads = downloads,
                        isLoadingDownloads = false
                    )
                }
        }
    }
    
    fun deleteDownload(episodeId: String) {
        viewModelScope.launch {
            try {
                val result = deleteDownloadUseCase(episodeId)
                if (result.isSuccess) {
                    // Refresh downloads list and storage info
                    loadDownloads()
                    loadStorageInfo()
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "删除下载失败: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "删除下载失败: ${e.message}"
                )
            }
        }
    }
    
    fun cleanupOldDownloads() {
        viewModelScope.launch {
            try {
                val result = cleanupStorageUseCase()
                if (result.isSuccess) {
                    val cleanedCount = result.getOrNull() ?: 0
                    _uiState.value = _uiState.value.copy(
                        cleanupResult = "已清�?$cleanedCount 个旧下载文件"
                    )
                    
                    // Refresh data
                    loadDownloads()
                    loadStorageInfo()
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "清理失败: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "清理失败: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearCleanupResult() {
        _uiState.value = _uiState.value.copy(cleanupResult = null)
    }
}