package com.mindseek.podcast.domain.usecase.offline

import android.content.Context
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.File

class GetStorageInfoUseCaseTest {

    private lateinit var context: Context
    private lateinit var useCase: GetStorageInfoUseCase

    @Before
    fun setup() {
        context = mockk()
        useCase = GetStorageInfoUseCase(context)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should return storage info when external files dir exists`() = runTest {
        // Given
        val mockExternalDir = mockk<File>()
        val mockDownloadDir = mockk<File>()
        
        every { context.getExternalFilesDir(null) } returns mockExternalDir
        every { mockExternalDir.absolutePath } returns "/storage/emulated/0/Android/data/com.test/files"
        every { File(mockExternalDir, "downloads") } returns mockDownloadDir
        every { mockDownloadDir.exists() } returns true
        
        // Mock StatFs - this is tricky to mock, so we'll test the basic flow
        mockkConstructor(android.os.StatFs::class)
        every { anyConstructed<android.os.StatFs>().blockSizeLong } returns 4096L
        every { anyConstructed<android.os.StatFs>().blockCountLong } returns 1000000L
        every { anyConstructed<android.os.StatFs>().availableBlocksLong } returns 500000L
        
        // Mock file walking
        every { mockExternalDir.walkTopDown() } returns sequenceOf(mockExternalDir)
        every { mockExternalDir.isFile } returns false
        every { mockDownloadDir.walkTopDown() } returns sequenceOf(mockDownloadDir)
        every { mockDownloadDir.isFile } returns false

        // When
        val result = useCase()

        // Then
        assertNotNull(result)
        assertEquals(4096L * 1000000L, result.totalSpace)
        assertEquals(4096L * 500000L, result.availableSpace)
        assertEquals(0L, result.usedByApp)
        assertEquals(0L, result.usedByDownloads)
    }

    @Test
    fun `should handle null external files dir gracefully`() = runTest {
        // Given
        every { context.getExternalFilesDir(null) } returns null
        every { context.filesDir } returns mockk<File> {
            every { absolutePath } returns "/data/data/com.test/files"
        }
        
        // Mock StatFs
        mockkConstructor(android.os.StatFs::class)
        every { anyConstructed<android.os.StatFs>().blockSizeLong } returns 4096L
        every { anyConstructed<android.os.StatFs>().blockCountLong } returns 1000000L
        every { anyConstructed<android.os.StatFs>().availableBlocksLong } returns 500000L

        // When
        val result = useCase()

        // Then
        assertNotNull(result)
        assertEquals(4096L * 1000000L, result.totalSpace)
        assertEquals(4096L * 500000L, result.availableSpace)
    }

    @Test
    fun `should calculate directory size correctly`() = runTest {
        // Given
        val mockExternalDir = mockk<File>()
        val mockFile1 = mockk<File>()
        val mockFile2 = mockk<File>()
        
        every { context.getExternalFilesDir(null) } returns mockExternalDir
        every { mockExternalDir.absolutePath } returns "/storage/test"
        every { File(mockExternalDir, "downloads") } returns mockk<File> {
            every { exists() } returns false
        }
        
        // Mock StatFs
        mockkConstructor(android.os.StatFs::class)
        every { anyConstructed<android.os.StatFs>().blockSizeLong } returns 4096L
        every { anyConstructed<android.os.StatFs>().blockCountLong } returns 1000000L
        every { anyConstructed<android.os.StatFs>().availableBlocksLong } returns 500000L
        
        // Mock file walking with actual files
        every { mockExternalDir.walkTopDown() } returns sequenceOf(mockExternalDir, mockFile1, mockFile2)
        every { mockExternalDir.isFile } returns false
        every { mockFile1.isFile } returns true
        every { mockFile1.length() } returns 1024L
        every { mockFile2.isFile } returns true
        every { mockFile2.length() } returns 2048L

        // When
        val result = useCase()

        // Then
        assertEquals(3072L, result.usedByApp) // 1024 + 2048
        assertEquals(0L, result.usedByDownloads) // downloads dir doesn't exist
    }
}