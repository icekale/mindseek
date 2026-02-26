package com.mindseek.podcast.domain.usecase.download

import com.mindseek.podcast.service.DownloadManager
import javax.inject.Inject

class DeleteDownloadUseCase @Inject constructor(
    private val downloadManager: DownloadManager
) {
    suspend operator fun invoke(episodeId: String): Result<Unit> {
        return downloadManager.deleteDownload(episodeId)
    }
}