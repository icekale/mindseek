package com.mindseek.podcast.presentation.screens

import com.mindseek.podcast.domain.model.DownloadInfo
import com.mindseek.podcast.domain.usecase.download.DeleteDownloadUseCase
import com.mindseek.podcast.domain.usecase.download.GetDownloadsUseCase
import com.mindseek.podcast.domain.usecase.offline.CleanupStorageUseCase
import com.mindseek.podcast.domain.usecase.offline.GetStorageInfoUseCase
import com.mindseek.podcast.domain.usecase.offline.StorageInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class StorageManagementViewModelTest {

    @Mock
    private lateinit var getStorageInfoUseCase: GetStorageInfoUseCase

    @Mock
    private lateinit var getDownloadsUseCase: GetDownloadsUseCase

    @Mock
    private lateinit var deleteDownloadUseCase: DeleteDownloadUseCase

    @Mock
    private lateinit var cleanupStorageUseCase: CleanupStorageUseCase

    private lateinit var viewModel: StorageManagementViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val sampleStorageInfo = StorageInfo(
        totalSpace = 1000000000L, // 1GB
        usedSpace = 500000000L,   // 500MB
        availableSpace = 500000000L, // 500MB
        downloadedEpisodesCount = 10,
        downloadedSize = 200000000L // 200MB
    )

    private val sampleDownloads = listOf(
        DownloadInfo(
            episodeId = "episode1",
            episodeTitle = "Episode 1",
            podcastTitle = "Podcast 1",
            downloadDate = System.currentTimeMillis(),
            fileSize = 50000000L, // 50MB
            filePath = "/storage/episode1.mp3"
        ),
        DownloadInfo(
            episodeId = "episode2",
            episodeTitle = "Episode 2",
            podcastTitle = "Podcast 2",
            downloadDate = System.currentTimeMillis() - 86400000L,
            fileSize = 75000000L, // 75MB
            filePath = "/storage/episode2.mp3"
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = StorageManagementViewModel(
            getStorageInfoUseCase,
            getDownloadsUseCase,
            deleteDownloadUseCase,
            cleanupStorageUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be correct`() {
        // Given - ViewModel is created
        val initialState = viewModel.uiState.value

        // Then
        assertNull(initialState.storageInfo)
        assertEquals(emptyList<DownloadInfo>(), initialState.downloads)
        assertFalse(initialState.isLoadingStorage)
        assertFalse(initialState.isLoadingDownloads)
        assertNull(initialState.cleanupResult)
        assertNull(initialState.error)
    }

    @Test
    fun `should load storage info successfully`() = runTest {
        // Given
        whenever(getStorageInfoUseCase()).thenReturn(sampleStorageInfo)

        // When
        viewModel.loadStorageInfo()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(sampleStorageInfo, state.storageInfo)
        assertFalse(state.isLoadingStorage)
        assertNull(state.error)
        verify(getStorageInfoUseCase).invoke()
    }

    @Test
    fun `should handle error when loading storage info fails`() = runTest {
        // Given
        val errorMessage = "Storage access denied"
        whenever(getStorageInfoUseCase()).thenThrow(RuntimeException(errorMessage))

        // When
        viewModel.loadStorageInfo()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertNull(state.storageInfo)
        assertFalse(state.isLoadingStorage)
        assertTrue(state.error?.contains("加载存储信息失败") == true)
    }

    @Test
    fun `should load downloads successfully`() = runTest {
        // Given
        whenever(getDownloadsUseCase()).thenReturn(flowOf(sampleDownloads))

        // When
        viewModel.loadDownloads()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(sampleDownloads, state.downloads)
        assertFalse(state.isLoadingDownloads)
        assertNull(state.error)
        verify(getDownloadsUseCase).invoke()
    }

    @Test
    fun `should handle error when loading downloads fails`() = runTest {
        // Given
        val errorMessage = "Database error"
        whenever(getDownloadsUseCase()).thenThrow(RuntimeException(errorMessage))

        // When
        viewModel.loadDownloads()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(emptyList<DownloadInfo>(), state.downloads)
        assertFalse(state.isLoadingDownloads)
        assertTrue(state.error?.contains("加载下载列表失败") == true)
    }

    @Test
    fun `should delete download successfully`() = runTest {
        // Given
        val episodeId = "episode1"
        whenever(deleteDownloadUseCase(episodeId)).thenReturn(Result.success(Unit))
        whenever(getDownloadsUseCase()).thenReturn(flowOf(sampleDownloads.drop(1)))
        whenever(getStorageInfoUseCase()).thenReturn(sampleStorageInfo)

        // When
        viewModel.deleteDownload(episodeId)
        advanceUntilIdle()

        // Then
        verify(deleteDownloadUseCase).invoke(episodeId)
        verify(getDownloadsUseCase).invoke()
        verify(getStorageInfoUseCase).invoke()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `should handle error when deleting download fails`() = runTest {
        // Given
        val episodeId = "episode1"
        val errorMessage = "File not found"
        whenever(deleteDownloadUseCase(episodeId)).thenReturn(Result.failure(RuntimeException(errorMessage)))

        // When
        viewModel.deleteDownload(episodeId)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.error?.contains("删除下载失败") == true)
        verify(deleteDownloadUseCase).invoke(episodeId)
    }

    @Test
    fun `should cleanup old downloads successfully`() = runTest {
        // Given
        val cleanedCount = 5
        whenever(cleanupStorageUseCase()).thenReturn(Result.success(cleanedCount))
        whenever(getDownloadsUseCase()).thenReturn(flowOf(emptyList()))
        whenever(getStorageInfoUseCase()).thenReturn(sampleStorageInfo)

        // When
        viewModel.cleanupOldDownloads()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals("已清理$cleanedCount个旧下载文件", state.cleanupResult)
        assertNull(state.error)
        verify(cleanupStorageUseCase).invoke()
        verify(getDownloadsUseCase).invoke()
        verify(getStorageInfoUseCase).invoke()
    }

    @Test
    fun `should handle error when cleanup fails`() = runTest {
        // Given
        val errorMessage = "Cleanup failed"
        whenever(cleanupStorageUseCase()).thenReturn(Result.failure(RuntimeException(errorMessage)))

        // When
        viewModel.cleanupOldDownloads()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.error?.contains("清理失败") == true)
        assertNull(state.cleanupResult)
        verify(cleanupStorageUseCase).invoke()
    }

    @Test
    fun `should clear error message`() {
        // Given
        viewModel.loadStorageInfo() // This will set loading state

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `should clear cleanup result`() = runTest {
        // Given
        whenever(cleanupStorageUseCase()).thenReturn(Result.success(3))
        whenever(getDownloadsUseCase()).thenReturn(flowOf(emptyList()))
        whenever(getStorageInfoUseCase()).thenReturn(sampleStorageInfo)
        viewModel.cleanupOldDownloads()
        advanceUntilIdle()

        // When
        viewModel.clearCleanupResult()

        // Then
        assertNull(viewModel.uiState.value.cleanupResult)
    }

    @Test
    fun `should set loading state when loading storage info`() {
        // When
        viewModel.loadStorageInfo()

        // Then
        assertTrue(viewModel.uiState.value.isLoadingStorage)
    }

    @Test
    fun `should set loading state when loading downloads`() {
        // When
        viewModel.loadDownloads()

        // Then
        assertTrue(viewModel.uiState.value.isLoadingDownloads)
    }

    @Test
    fun `should handle exception when deleting download`() = runTest {
        // Given
        val episodeId = "episode1"
        val errorMessage = "Unexpected error"
        whenever(deleteDownloadUseCase(episodeId)).thenThrow(RuntimeException(errorMessage))

        // When
        viewModel.deleteDownload(episodeId)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.error?.contains("删除下载失败") == true)
    }

    @Test
    fun `should handle exception when cleaning up storage`() = runTest {
        // Given
        val errorMessage = "Cleanup exception"
        whenever(cleanupStorageUseCase()).thenThrow(RuntimeException(errorMessage))

        // When
        viewModel.cleanupOldDownloads()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.error?.contains("清理失败") == true)
    }
}