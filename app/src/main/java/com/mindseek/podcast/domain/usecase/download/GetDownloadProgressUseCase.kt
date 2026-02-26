package com.mindseek.podcast.domain.usecase.download

import com.mindseek.podcast.service.DownloadManager
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetDownloadProgressUseCase @Inject constructor(
    private val downloadManager: DownloadManager
) {
    operator fun invoke(): StateFlow<Map<String, DownloadManager.DownloadProgress>> {
        return downloadManager.downloadStates
    }
}