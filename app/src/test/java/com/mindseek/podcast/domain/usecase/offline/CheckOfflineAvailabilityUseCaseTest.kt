package com.mindseek.podcast.domain.usecase.offline

import com.mindseek.podcast.domain.model.EpisodeDomain
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.File

class CheckOfflineAvailabilityUseCaseTest {

    private lateinit var useCase: CheckOfflineAvailabilityUseCase

    @Before
    fun setup() {
        useCase = CheckOfflineAvailabilityUseCase()
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should return true when episode is downloaded and file exists`() {
        // Given
        val tempFile = createTempFile("test_audio", ".mp3")
        tempFile.writeText("test audio content")
        
        val episode = EpisodeDomain(
            id = "episode1",
            podcastId = "podcast1",
            title = "Test Episode",
            description = "Test Description",
            audioUrl = "http://example.com/audio.mp3",
            duration = 3600000L,
            publishDate = System.currentTimeMillis(),
            isDownloaded = true,
            localPath = tempFile.absolutePath
        )

        // When
        val result = useCase(episode)

        // Then
        assertTrue(result)
        
        // Cleanup
        tempFile.delete()
    }

    @Test
    fun `should return false when episode is not downloaded`() {
        // Given
        val episode = EpisodeDomain(
            id = "episode1",
            podcastId = "podcast1",
            title = "Test Episode",
            description = "Test Description",
            audioUrl = "http://example.com/audio.mp3",
            duration = 3600000L,
            publishDate = System.currentTimeMillis(),
            isDownloaded = false,
            localPath = null
        )

        // When
        val result = useCase(episode)

        // Then
        assertFalse(result)
    }

    @Test
    fun `should return false when episode is downloaded but local path is null`() {
        // Given
        val episode = EpisodeDomain(
            id = "episode1",
            podcastId = "podcast1",
            title = "Test Episode",
            description = "Test Description",
            audioUrl = "http://example.com/audio.mp3",
            duration = 3600000L,
            publishDate = System.currentTimeMillis(),
            isDownloaded = true,
            localPath = null
        )

        // When
        val result = useCase(episode)

        // Then
        assertFalse(result)
    }

    @Test
    fun `should return false when episode is downloaded but file does not exist`() {
        // Given
        val nonExistentPath = "/path/to/nonexistent/file.mp3"
        val episode = EpisodeDomain(
            id = "episode1",
            podcastId = "podcast1",
            title = "Test Episode",
            description = "Test Description",
            audioUrl = "http://example.com/audio.mp3",
            duration = 3600000L,
            publishDate = System.currentTimeMillis(),
            isDownloaded = true,
            localPath = nonExistentPath
        )

        // When
        val result = useCase(episode)

        // Then
        assertFalse(result)
    }

    @Test
    fun `should return false when episode is downloaded but file is empty`() {
        // Given
        val tempFile = createTempFile("test_audio_empty", ".mp3")
        // File is created but empty (length = 0)
        
        val episode = EpisodeDomain(
            id = "episode1",
            podcastId = "podcast1",
            title = "Test Episode",
            description = "Test Description",
            audioUrl = "http://example.com/audio.mp3",
            duration = 3600000L,
            publishDate = System.currentTimeMillis(),
            isDownloaded = true,
            localPath = tempFile.absolutePath
        )

        // When
        val result = useCase(episode)

        // Then
        assertFalse(result)
        
        // Cleanup
        tempFile.delete()
    }
}