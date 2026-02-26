package com.mindseek.podcast.domain.usecase

import com.mindseek.podcast.data.local.entity.Podcast
import com.mindseek.podcast.domain.repository.PodcastRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetPodcastDetailUseCaseTest {

    private lateinit var podcastRepository: PodcastRepository
    private lateinit var getPodcastDetailUseCase: GetPodcastDetailUseCase

    @Before
    fun setUp() {
        podcastRepository = mockk()
        getPodcastDetailUseCase = GetPodcastDetailUseCase(podcastRepository)
    }

    @Test
    fun `invoke should return podcast domain when repository returns podcast`() = runTest {
        // Given
        val podcastId = "test-podcast-id"
        val podcast = Podcast(
            id = podcastId,
            title = "Test Podcast",
            description = "Test Description",
            imageUrl = "https://example.com/image.jpg",
            author = "Test Author",
            category = "Technology",
            isSubscribed = false,
            lastUpdated = System.currentTimeMillis()
        )

        coEvery { podcastRepository.getPodcastById(podcastId) } returns podcast

        // When
        val result = getPodcastDetailUseCase(podcastId)

        // Then
        assertEquals(podcastId, result?.id)
        assertEquals("Test Podcast", result?.title)
        assertEquals("Test Description", result?.description)
        assertEquals("Test Author", result?.author)
        assertEquals("Technology", result?.category)
        assertEquals(false, result?.isSubscribed)
    }

    @Test
    fun `invoke should return null when repository returns null`() = runTest {
        // Given
        val podcastId = "non-existent-podcast-id"
        coEvery { podcastRepository.getPodcastById(podcastId) } returns null

        // When
        val result = getPodcastDetailUseCase(podcastId)

        // Then
        assertNull(result)
    }

    @Test(expected = Exception::class)
    fun `invoke should throw exception when repository throws exception`() = runTest {
        // Given
        val podcastId = "test-podcast-id"
        coEvery { podcastRepository.getPodcastById(podcastId) } throws Exception("Database error")

        // When
        getPodcastDetailUseCase(podcastId)

        // Then - exception should be thrown
    }
}