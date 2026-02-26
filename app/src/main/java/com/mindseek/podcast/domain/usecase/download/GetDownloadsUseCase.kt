package com.mindseek.podcast.domain.usecase.download

import com.mindseek.podcast.domain.model.DownloadInfo
import com.mindseek.podcast.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDownloadsUseCase @Inject constructor(
    private val downloadRepository: DownloadRepository
) {
    operator fun invoke(): Flow<List<DownloadInfo>> {
        return downloadRepository.getAllDownloads()
    }
}