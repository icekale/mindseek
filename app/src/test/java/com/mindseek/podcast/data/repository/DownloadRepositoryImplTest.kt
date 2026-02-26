package com.mindseek.podcast.data.repository

import android.content.Context
import com.mindseek.podcast.data.local.dao.DownloadTaskDao
import com.mindseek.podcast.data.local.dao.EpisodeDao
import com.mindseek.podcast.data.local.entity.DownloadTask
import com.mindseek.podcast.domain.model.DownloadState
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.File

class DownloadRepositoryImplTest {

    private lateinit var downloadTaskDao: DownloadTaskDao
    private lateinit var episodeDao: EpisodeDao
    private lateinit var context: Context
    private lateinit var repository: DownloadRepositoryImpl

    @Before
    fun setup() {
        downloadTaskDao = mockk()
        episodeDao = mockk()
        context = mockk()
        
        // Mock context.getExternalFilesDir
        val mockFile = mockk<File>()
        every { context.getExternalFilesDir(null) } returns mockFile
        every { mockFile.exists() } returns true
        every { mockFile.mkdirs() } returns true
        
        repository = DownloadRepositoryImpl(downloadTaskDao, episodeDao, context)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getAllDownloads should return mapped download info list`() = runTest {
        // Given
        val downloadTasks = listOf(
            DownloadTask(
                id = "task1",
                episodeId = "episode1",
                audioUrl = "http://example.com/audio1.mp3",
                status = "completed",
                progress = 1.0f,
                createdAt = System.currentTimeMillis()
            )
        )
        every { downloadTaskDao.getAllDownloadTasks() } returns flowOf(downloadTasks)

        // When
        val result = repository.getAllDownloads()

        // Then
        result.collect { downloadInfoList ->
            assertEquals(1, downloadInfoList.size)
            assertEquals("episode1", downloadInfoList[0].episodeId)
            assertEquals(DownloadState.Downloaded::class, downloadInfoList[0].downloadState::class)
        }
    }

    @Test
    fun `startDownload should create download task successfully`() = runTest {
        // Given
        val episodeId = "episode1"
        val audioUrl = "http://example.com/audio1.mp3"
        val title = "Test Episode"
        
        coEvery { downloadTaskDao.insertDownloadTask(any()) } just Runs

        // When
        val result = repository.startDownload(episodeId, audioUrl, title)

        // Then
        assertTrue(result.isSuccess)
        coVerify { downloadTaskDao.insertDownloadTask(any()) }
    }

    @Test
    fun `pauseDownload should update task status to paused`() = runTest {
        // Given
        val episodeId = "episode1"
        val downloadTask = DownloadTask(
            id = "task1",
            episodeId = episodeId,
            audioUrl = "http://example.com/audio1.mp3",
            status = "downloading",
            progress = 0.5f,
            createdAt = System.currentTimeMillis()
        )
        
        coEvery { downloadTaskDao.getDownloadTaskByEpisodeId(episodeId) } returns downloadTask
        coEvery { downloadTaskDao.updateDownloadStatus(any(), any()) } just Runs

        // When
        val result = repository.pauseDownload(episodeId)

        // Then
        assertTrue(result.isSuccess)
        coVerify { downloadTaskDao.updateDownloadStatus("task1", "paused") }
    }

    @Test
    fun `cancelDownload should delete task and file`() = runTest {
        // Given
        val episodeId = "episode1"
        val mockFile = mockk<File>()
        val downloadTask = DownloadTask(
            id = "task1",
            episodeId = episodeId,
            audioUrl = "http://example.com/audio1.mp3",
            localPath = "/path/to/file.mp3",
            status = "downloading",
            progress = 0.5f,
            createdAt = System.currentTimeMillis()
        )
        
        coEvery { downloadTaskDao.getDownloadTaskByEpisodeId(episodeId) } returns downloadTask
        coEvery { downloadTaskDao.deleteDownloadTaskByEpisodeId(episodeId) } just Runs
        
        // Mock file operations
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true
        every { anyConstructed<File>().delete() } returns true

        // When
        val result = repository.cancelDownload(episodeId)

        // Then
        assertTrue(result.isSuccess)
        coVerify { downloadTaskDao.deleteDownloadTaskByEpisodeId(episodeId) }
    }

    @Test
    fun `deleteDownload should remove completed download`() = runTest {
        // Given
        val episodeId = "episode1"
        val downloadTask = DownloadTask(
            id = "task1",
            episodeId = episodeId,
            audioUrl = "http://example.com/audio1.mp3",
            localPath = "/path/to/file.mp3",
            status = "completed",
            progress = 1.0f,
            createdAt = System.currentTimeMillis()
        )
        
        coEvery { downloadTaskDao.getDownloadTaskByEpisodeId(episodeId) } returns downloadTask
        coEvery { downloadTaskDao.deleteDownloadTaskByEpisodeId(episodeId) } just Runs
        coEvery { episodeDao.updateDownloadStatus(episodeId, false, null) } just Runs
        
        // Mock file operations
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true
        every { anyConstructed<File>().delete() } returns true

        // When
        val result = repository.deleteDownload(episodeId)

        // Then
        assertTrue(result.isSuccess)
        coVerify { episodeDao.updateDownloadStatus(episodeId, false, null) }
        coVerify { downloadTaskDao.deleteDownloadTaskByEpisodeId(episodeId) }
    }

    @Test
    fun `markDownloadCompleted should update task and episode`() = runTest {
        // Given
        val taskId = "task1"
        val episodeId = "episode1"
        val localPath = "/path/to/file.mp3"
        val downloadTask = DownloadTask(
            id = taskId,
            episodeId = episodeId,
            audioUrl = "http://example.com/audio1.mp3",
            status = "downloading",
            progress = 0.9f,
            createdAt = System.currentTimeMillis()
        )
        
        coEvery { downloadTaskDao.getDownloadTaskById(taskId) } returns downloadTask
        coEvery { downloadTaskDao.markDownloadCompleted(any(), any(), any(), any()) } just Runs
        coEvery { episodeDao.updateDownloadStatus(episodeId, true, localPath) } just Runs

        // When
        repository.markDownloadCompleted(taskId, localPath)

        // Then
        coVerify { downloadTaskDao.markDownloadCompleted(taskId, "completed", localPath, any()) }
        coVerify { episodeDao.updateDownloadStatus(episodeId, true, localPath) }
    }
}