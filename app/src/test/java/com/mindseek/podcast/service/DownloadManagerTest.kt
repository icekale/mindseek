package com.mindseek.podcast.service

import android.content.Context
import com.mindseek.podcast.domain.model.DownloadInfo
import com.mindseek.podcast.domain.model.DownloadState
import com.mindseek.podcast.domain.repository.DownloadRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.File

class DownloadManagerTest {

    private lateinit var downloadRepository: DownloadRepository
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var context: Context
    private lateinit var downloadManager: DownloadManager

    @Before
    fun setup() {
        downloadRepository = mockk()
        okHttpClient = mockk()
        context = mockk()
        
        // Mock context.getExternalFilesDir
        val mockFile = mockk<File>()
        every { context.getExternalFilesDir(null) } returns mockFile
        every { mockFile.exists() } returns true
        every { mockFile.mkdirs() } returns true
        
        downloadManager = DownloadManager(downloadRepository, okHttpClient, context)
    }

    @After
    fun tearDown() {
        downloadManager.cleanup()
        clearAllMocks()
    }

    @Test
    fun `startDownload should create download task when not already downloading`() = runTest {
        // Given
        val episodeId = "episode1"
        val audioUrl = "http://example.com/audio1.mp3"
        val title = "Test Episode"
        val taskId = "task1"
        
        coEvery { downloadRepository.startDownload(episodeId, audioUrl, title) } returns Result.success(taskId)

        // When
        val result = downloadManager.startDownload(episodeId, audioUrl, title)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(taskId, result.getOrNull())
        coVerify { downloadRepository.startDownload(episodeId, audioUrl, title) }
    }

    @Test
    fun `startDownload should fail when episode is already downloading`() = runTest {
        // Given
        val episodeId = "episode1"
        val audioUrl = "http://example.com/audio1.mp3"
        val title = "Test Episode"
        val taskId = "task1"
        
        coEvery { downloadRepository.startDownload(episodeId, audioUrl, title) } returns Result.success(taskId)
        
        // Start first download
        downloadManager.startDownload(episodeId, audioUrl, title)

        // When - try to start same download again
        val result = downloadManager.startDownload(episodeId, audioUrl, title)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `pauseDownload should pause active download`() = runTest {
        // Given
        val episodeId = "episode1"
        
        coEvery { downloadRepository.pauseDownload(episodeId) } returns Result.success(Unit)

        // When
        val result = downloadManager.pauseDownload(episodeId)

        // Then
        assertTrue(result.isSuccess)
        coVerify { downloadRepository.pauseDownload(episodeId) }
    }

    @Test
    fun `resumeDownload should resume paused download`() = runTest {
        // Given
        val episodeId = "episode1"
        val downloadInfo = DownloadInfo(
            episodeId = episodeId,
            title = "Test Episode",
            audioUrl = "http://example.com/audio1.mp3",
            downloadState = DownloadState.Paused
        )
        
        coEvery { downloadRepository.getDownloadInfo(episodeId) } returns downloadInfo
        coEvery { downloadRepository.resumeDownload(episodeId) } returns Result.success(Unit)

        // When
        val result = downloadManager.resumeDownload(episodeId)

        // Then
        assertTrue(result.isSuccess)
        coVerify { downloadRepository.resumeDownload(episodeId) }
    }

    @Test
    fun `cancelDownload should cancel active download and remove from queue`() = runTest {
        // Given
        val episodeId = "episode1"
        
        coEvery { downloadRepository.cancelDownload(episodeId) } returns Result.success(Unit)

        // When
        val result = downloadManager.cancelDownload(episodeId)

        // Then
        assertTrue(result.isSuccess)
        coVerify { downloadRepository.cancelDownload(episodeId) }
    }

    @Test
    fun `deleteDownload should delete completed download`() = runTest {
        // Given
        val episodeId = "episode1"
        
        coEvery { downloadRepository.deleteDownload(episodeId) } returns Result.success(Unit)

        // When
        val result = downloadManager.deleteDownload(episodeId)

        // Then
        assertTrue(result.isSuccess)
        coVerify { downloadRepository.deleteDownload(episodeId) }
    }

    @Test
    fun `getTotalDownloadedSize should return total size from repository`() = runTest {
        // Given
        val expectedSize = 1024L * 1024L * 100L // 100MB
        
        coEvery { downloadRepository.getTotalDownloadedSize() } returns expectedSize

        // When
        val result = downloadManager.getTotalDownloadedSize()

        // Then
        assertEquals(expectedSize, result)
        coVerify { downloadRepository.getTotalDownloadedSize() }
    }
}