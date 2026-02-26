package com.mindseek.podcast.domain.model

sealed class DownloadState {
    object NotDownloaded : DownloadState()
    data class Downloading(val progress: Float) : DownloadState()
    object Downloaded : DownloadState()
    data class Failed(val error: String) : DownloadState()
    object Paused : DownloadState()
}

data class DownloadInfo(
    val episodeId: String,
    val title: String,
    val audioUrl: String,
    val localPath: String? = null,
    val downloadState: DownloadState = DownloadState.NotDownloaded,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val downloadDate: Long? = null
)