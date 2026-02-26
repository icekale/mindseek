package com.mindseek.podcast.domain.usecase.player

import com.mindseek.podcast.data.local.entity.Episode
import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.domain.repository.PodcastRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetPreviousEpisodeUseCaseTest {

    private lateinit var podcastRepository: PodcastRepository
    private lateinit var getPreviousEpisodeUseCase: GetPreviousEpisodeUseCase

    @Before
    fun setup() {
        podcastRepository = mockk()
        getPreviousEpisodeUseCase = GetPreviousEpisodeUseCase(podcastRepository)
    }

    @Test
    fun `should return previous episode when current episode is not first`() = runTest {
        // Given
        val episodes = listOf(
            createTestEpisodeEntity("episode-1", 3000L), // Latest (index 0)
            createTestEpisodeEntity("episode-2", 2000L), // Middle (index 1)
            createTestEpisodeEntity("episode-3", 1000L)  // Oldest (index 2)
        )
        
        val currentEpisode = createTestEpisodeDomain("episode-2", 2000L)
        
        coEvery { 
            podcastRepository.getEpisodesByPodcastId("test-podcast-1") 
        } returns flowOf(episodes)

        // When
        val result = getPreviousEpisodeUseCase(currentEpisode)

        // Then
        assertEquals("episode-1", result?.id)
        assertEquals("Test Episode 1", result?.title)
    }

    @Test
    fun `should return null when current episode is first episode`() = runTest {
        // Given
        val episodes = listOf(
            createTestEpisodeEntity("episode-1", 3000L), // Latest (index 0)
            createTestEpisodeEntity("episode-2", 2000L), // Middle (index 1)
            createTestEpisodeEntity("episode-3", 1000L)  // Oldest (index 2)
        )
        
        val currentEpisode = createTestEpisodeDomain("episode-1", 3000L) // First episode
        
        coEvery { 
            podcastRepository.getEpisodesByPodcastId("test-podcast-1") 
        } returns flowOf(episodes)

        // When
        val result = getPreviousEpisodeUseCase(currentEpisode)

        // Then
        assertNull(result)
    }

    @Test
    fun `should return null when current episode is not found in list`() = runTest {
        // Given
        val episodes = listOf(
            createTestEpisodeEntity("episode-1", 3000L),
            createTestEpisodeEntity("episode-2", 2000L),
            createTestEpisodeEntity("episode-3", 1000L)
        )
        
        val currentEpisode = createTestEpisodeDomain("episode-unknown", 2500L)
        
        coEvery { 
            podcastRepository.getEpisodesByPodcastId("test-podcast-1") 
        } returns flowOf(episodes)

        // When
        val result = getPreviousEpisodeUseCase(currentEpisode)

        // Then
        assertNull(result)
    }

    @Test
    fun `should return null when episodes list is empty`() = runTest {
        // Given
        val currentEpisode = createTestEpisodeDomain("episode-1", 3000L)
        
        coEvery { 
            podcastRepository.getEpisodesByPodcastId("test-podcast-1") 
        } returns flowOf(emptyList())

        // When
        val result = getPreviousEpisodeUseCase(currentEpisode)

        // Then
        assertNull(result)
    }

    @Test
    fun `should return null when repository throws exception`() = runTest {
        // Given
        val currentEpisode = createTestEpisodeDomain("episode-1", 3000L)
        
        coEvery { 
            podcastRepository.getEpisodesByPodcastId("test-podcast-1") 
        } throws RuntimeException("Database error")

        // When
        val result = getPreviousEpisodeUseCase(currentEpisode)

        // Then
        assertNull(result)
    }

    @Test
    fun `should handle single episode list correctly`() = runTest {
        // Given
        val episodes = listOf(
            createTestEpisodeEntity("episode-1", 3000L)
        )
        
        val currentEpisode = createTestEpisodeDomain("episode-1", 3000L)
        
        coEvery { 
            podcastRepository.getEpisodesByPodcastId("test-podcast-1") 
        } returns flowOf(episodes)

        // When
        val result = getPreviousEpisodeUseCase(currentEpisode)

        // Then
        assertNull(result, "Should return null when there's only one episode")
    }

    private fun createTestEpisodeEntity(
        id: String,
        publishDate: Long
    ): Episode {
        return Episode(
            id = id,
            podcastId = "test-podcast-1",
            title = "Test Episode ${id.last()}",
            description = "Test episode description",
            audioUrl = "https://example.com/$id.mp3",
            duration = 3600000L,
            publishDate = publishDate,
            isDownloaded = false,
            localPath = null,
            playPosition = 0L
        )
    }

    private fun createTestEpisodeDomain(
        id: String,
        publishDate: Long
    ): EpisodeDomain {
        return EpisodeDomain(
            id = id,
            podcastId = "test-podcast-1",
            title = "Test Episode ${id.last()}",
            description = "Test episode description",
            audioUrl = "https://example.com/$id.mp3",
            duration = 3600000L,
            publishDate = publishDate,
            isDownloaded = false,
            localPath = null,
            playPosition = 0L,
            isFavorite = false,
            podcastTitle = "Test Podcast"
        )
    }
}