package com.mindseek.podcast.integration

import android.content.Context
import com.mindseek.podcast.data.local.dao.DownloadTaskDao
import com.mindseek.podcast.data.local.dao.EpisodeDao
import com.mindseek.podcast.data.local.entity.DownloadTask
import com.mindseek.podcast.data.local.entity.Episode
import com.mindseek.podcast.data.repository.DownloadRepositoryImpl
import com.mindseek.podcast.domain.model.DownloadState
import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.domain.usecase.offline.CheckOfflineAvailabilityUseCase
import com.mindseek.podcast.service.DownloadManager
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.File

class OfflinePlaybackIntegrationTest {

    private lateinit var downloadTaskDao: DownloadTaskDao
    private lateinit var episodeDao: EpisodeDao
    private lateinit var context: Context
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var downloadRepository: DownloadRepositoryImpl
    private lateinit var downloadManager: DownloadManager
    private lateinit var checkOfflineAvailabilityUseCase: CheckOfflineAvailabilityUseCase

    @Before
    fun setup() {
        downloadTaskDao = mockk()
        episodeDao = mockk()
        context = mockk()
        okHttpClient = mockk()
        
        // Mock context.getExternalFilesDir
        val mockFile = mockk<File>()
        every { context.getExternalFilesDir(null) } returns mockFile
        every { mockFile.exists() } returns true
        every { mockFile.mkdirs() } returns true
        
        downloadRepository = DownloadRepositoryImpl(downloadTaskDao, episodeDao, context)
        downloadManager = DownloadManager(downloadRepository, okHttpClient, context)
        checkOfflineAvailabilityUseCase = CheckOfflineAvailabilityUseCase()
    }

    @After
    fun tearDown() {
        downloadManager.cleanup()
        clearAllMocks()
    }

    @Test
    fun `complete offline download and playback workflow`() = runTest {
        // Given - Episode to download
        val episodeId = "episode1"
        val audioUrl = "http://example.com/audio.mp3"
        val title = "Test Episode"
        val taskId = "task1"
        
        // Create a temporary file to simulate downloaded content
        val tempFile = createTempFile("test_audio", ".mp3")
        tempFile.writeText("test audio content")
        val localPath = tempFile.absolutePath
        
        // Mock repository methods for download workflow
        coEvery { downloadRepository.startDownload(episodeId, audioUrl, title) } returns Result.success(taskId)
        coEvery { downloadTaskDao.insertDownloadTask(any()) } just Runs
        coEvery { downloadTaskDao.getDownloadTaskById(taskId) } returns DownloadTask(
            id = taskId,
            episodeId = episodeId,
            audioUrl = audioUrl,
            status = "downloading",
            progress = 0f,
            createdAt = System.currentTimeMillis()
        )
        coEvery { downloadTaskDao.markDownloadCompleted(any(), any(), any(), any()) } just Runs
        coEvery { episodeDao.updateDownloadStatus(episodeId, true, localPath) } just Runs
        
        // Mock download info retrieval
        val downloadInfo = com.mindseek.podcast.domain.model.DownloadInfo(
            episodeId = episodeId,
            title = title,
            audioUrl = audioUrl,
            localPath = localPath,
            downloadState = DownloadState.Downloaded,
            downloadedBytes = tempFile.length(),
            totalBytes = tempFile.length()
        )
        coEvery { downloadRepository.getDownloadInfo(episodeId) } returns downloadInfo
        
        // When - Start download
        val downloadResult = downloadManager.startDownload(episodeId, audioUrl, title)
        
        // Then - Download should start successfully
        assertTrue(downloadResult.isSuccess)
        assertEquals(taskId, downloadResult.getOrNull())
        
        // When - Mark download as completed
        downloadRepository.markDownloadCompleted(taskId, localPath)
        
        // Then - Verify download completion
        coVerify { downloadTaskDao.markDownloadCompleted(taskId, "completed", localPath, any()) }
        coVerify { episodeDao.updateDownloadStatus(episodeId, true, localPath) }
        
        // When - Check offline availability
        val episode = EpisodeDomain(
            id = episodeId,
            podcastId = "podcast1",
            title = title,
            description = "Test Description",
            audioUrl = audioUrl,
            duration = 3600000L,
            publishDate = System.currentTimeMillis(),
            isDownloaded = true,
            localPath = localPath
        )
        
        val isOfflineAvailable = checkOfflineAvailabilityUseCase(episode)
        
        // Then - Episode should be available offline
        assertTrue(isOfflineAvailable)
        
        // Cleanup
        tempFile.delete()
    }

    @Test
    fun `download failure workflow`() = runTest {
        // Given
        val episodeId = "episode1"
        val audioUrl = "http://example.com/audio.mp3"
        val title = "Test Episode"
        val taskId = "task1"
        val errorMessage = "Network error"
        
        // Mock download failure
        coEvery { downloadRepository.startDownload(episodeId, audioUrl, title) } returns Result.success(taskId)
        coEvery { downloadRepository.markDownloadFailed(taskId, errorMessage) } just Runs
        coEvery { downloadTaskDao.updateDownloadStatus(taskId, "failed", errorMessage) } just Runs
        
        // When - Start download and simulate failure
        val downloadResult = downloadManager.startDownload(episodeId, audioUrl, title)
        assertTrue(downloadResult.isSuccess)
        
        // Simulate download failure
        downloadRepository.markDownloadFailed(taskId, errorMessage)
        
        // Then - Verify failure handling
        coVerify { downloadTaskDao.updateDownloadStatus(taskId, "failed", errorMessage) }
        
        // When - Check offline availability for failed download
        val episode = EpisodeDomain(
            id = episodeId,
            podcastId = "podcast1",
            title = title,
            description = "Test Description",
            audioUrl = audioUrl,
            duration = 3600000L,
            publishDate = System.currentTimeMillis(),
            isDownloaded = false,
            localPath = null
        )
        
        val isOfflineAvailable = checkOfflineAvailabilityUseCase(episode)
        
        // Then - Episode should not be available offline
        assertFalse(isOfflineAvailable)
    }

    @Test
    fun `cancel download workflow`() = runTest {
        // Given
        val episodeId = "episode1"
        val audioUrl = "http://example.com/audio.mp3"
        val title = "Test Episode"
        val taskId = "task1"
        
        // Mock download cancellation
        coEvery { downloadRepository.startDownload(episodeId, audioUrl, title) } returns Result.success(taskId)
        coEvery { downloadRepository.cancelDownload(episodeId) } returns Result.success(Unit)
        coEvery { downloadTaskDao.getDownloadTaskByEpisodeId(episodeId) } returns DownloadTask(
            id = taskId,
            episodeId = episodeId,
            audioUrl = audioUrl,
            localPath = "/path/to/partial/file.mp3",
            status = "downloading",
            progress = 0.5f,
            createdAt = System.currentTimeMillis()
        )
        coEvery { downloadTaskDao.deleteDownloadTaskByEpisodeId(episodeId) } just Runs
        
        // Mock file operations
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true
        every { anyConstructed<File>().delete() } returns true
        
        // When - Start and then cancel download
        val downloadResult = downloadManager.startDownload(episodeId, audioUrl, title)
        assertTrue(downloadResult.isSuccess)
        
        val cancelResult = downloadManager.cancelDownload(episodeId)
        
        // Then - Cancellation should succeed
        assertTrue(cancelResult.isSuccess)
        coVerify { downloadTaskDao.deleteDownloadTaskByEpisodeId(episodeId) }
    }
}