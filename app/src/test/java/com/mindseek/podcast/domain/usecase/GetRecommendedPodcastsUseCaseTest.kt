package com.mindseek.podcast.domain.usecase

import com.mindseek.podcast.data.local.entity.Podcast
import com.mindseek.podcast.domain.repository.PodcastRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class GetRecommendedPodcastsUseCaseTest {

    @Mock
    private lateinit var podcastRepository: PodcastRepository

    private lateinit var useCase: GetRecommendedPodcastsUseCase

    private val samplePodcasts = listOf(
        Podcast(
            id = "1",
            title = "Tech Podcast",
            description = "A podcast about technology",
            imageUrl = "https://example.com/image1.jpg",
            author = "Tech Author",
            category = "Technology",
            isSubscribed = false,
            lastUpdated = System.currentTimeMillis()
        ),
        Podcast(
            id = "2",
            title = "Science Podcast",
            description = "A podcast about science",
            imageUrl = "https://example.com/image2.jpg",
            author = "Science Author",
            category = "Science",
            isSubscribed = true,
            lastUpdated = System.currentTimeMillis()
        )
    )

    @Before
    fun setup() {
        useCase = GetRecommendedPodcastsUseCase(podcastRepository)
    }

    @Test
    fun `should return recommended podcasts successfully`() = runTest {
        // Given
        val page = 1
        whenever(podcastRepository.getRecommendedPodcasts(page)).thenReturn(samplePodcasts)

        // When
        val result = useCase(page)

        // Then
        assertEquals(2, result.size)
        assertEquals("Tech Podcast", result[0].title)
        assertEquals("Science Podcast", result[1].title)
        assertEquals("Technology", result[0].category)
        assertEquals("Science", result[1].category)
        verify(podcastRepository).getRecommendedPodcasts(page)
    }

    @Test
    fun `should return empty list when no podcasts available`() = runTest {
        // Given
        val page = 1
        whenever(podcastRepository.getRecommendedPodcasts(page)).thenReturn(emptyList())

        // When
        val result = useCase(page)

        // Then
        assertEquals(0, result.size)
        verify(podcastRepository).getRecommendedPodcasts(page)
    }

    @Test
    fun `should use default page when not specified`() = runTest {
        // Given
        whenever(podcastRepository.getRecommendedPodcasts(1)).thenReturn(samplePodcasts)

        // When
        val result = useCase()

        // Then
        assertEquals(2, result.size)
        verify(podcastRepository).getRecommendedPodcasts(1)
    }

    @Test
    fun `should handle different page numbers`() = runTest {
        // Given
        val page = 3
        whenever(podcastRepository.getRecommendedPodcasts(page)).thenReturn(samplePodcasts)

        // When
        val result = useCase(page)

        // Then
        assertEquals(2, result.size)
        verify(podcastRepository).getRecommendedPodcasts(page)
    }

    @Test(expected = RuntimeException::class)
    fun `should propagate repository exception`() = runTest {
        // Given
        val page = 1
        whenever(podcastRepository.getRecommendedPodcasts(page)).thenThrow(RuntimeException("Network error"))

        // When
        useCase(page)

        // Then - exception should be thrown
    }

    @Test
    fun `should map podcast entities to domain correctly`() = runTest {
        // Given
        val page = 1
        whenever(podcastRepository.getRecommendedPodcasts(page)).thenReturn(samplePodcasts)

        // When
        val result = useCase(page)

        // Then
        assertEquals(samplePodcasts[0].id, result[0].id)
        assertEquals(samplePodcasts[0].title, result[0].title)
        assertEquals(samplePodcasts[0].description, result[0].description)
        assertEquals(samplePodcasts[0].imageUrl, result[0].imageUrl)
        assertEquals(samplePodcasts[0].author, result[0].author)
        assertEquals(samplePodcasts[0].category, result[0].category)
        assertEquals(samplePodcasts[0].isSubscribed, result[0].isSubscribed)
        assertEquals(samplePodcasts[0].lastUpdated, result[0].lastUpdated)
    }
}