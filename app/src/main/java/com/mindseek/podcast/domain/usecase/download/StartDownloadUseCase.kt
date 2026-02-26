package com.mindseek.podcast.domain.usecase.download

import com.mindseek.podcast.service.DownloadManager
import javax.inject.Inject

class StartDownloadUseCase @Inject constructor(
    private val downloadManager: DownloadManager
) {
    suspend operator fun invoke(episodeId: String, audioUrl: String, title: String): Result<String> {
        return downloadManager.startDownload(episodeId, audioUrl, title)
    }
}