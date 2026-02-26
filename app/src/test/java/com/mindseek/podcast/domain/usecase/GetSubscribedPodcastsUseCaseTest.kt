package com.mindseek.podcast.domain.usecase

import com.mindseek.podcast.data.local.entity.Podcast
import com.mindseek.podcast.domain.repository.PodcastRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetSubscribedPodcastsUseCaseTest {

    private lateinit var podcastRepository: PodcastRepository
    private lateinit var getSubscribedPodcastsUseCase: GetSubscribedPodcastsUseCase

    @Before
    fun setUp() {
        podcastRepository = mockk()
        getSubscribedPodcastsUseCase = GetSubscribedPodcastsUseCase(podcastRepository)
    }

    @Test
    fun `invoke should return subscribed podcasts from repository`() = runTest {
        // Given
        val subscribedPodcasts = listOf(
            Podcast(
                id = "1",
                title = "Test Podcast 1",
                description = "Description 1",
                imageUrl = "image1.jpg",
                author = "Author 1",
                category = "Technology",
                isSubscribed = true,
                lastUpdated = System.currentTimeMillis()
            ),
            Podcast(
                id = "2",
                title = "Test Podcast 2",
                description = "Description 2",
                imageUrl = "image2.jpg",
                author = "Author 2",
                category = "Science",
                isSubscribed = true,
                lastUpdated = System.currentTimeMillis()
            )
        )
        
        every { podcastRepository.getSubscribedPodcasts() } returns flowOf(subscribedPodcasts)

        // When
        val result = getSubscribedPodcastsUseCase().toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(subscribedPodcasts, result[0])
        verify { podcastRepository.getSubscribedPodcasts() }
    }

    @Test
    fun `invoke should return empty list when no subscriptions`() = runTest {
        // Given
        every { podcastRepository.getSubscribedPodcasts() } returns flowOf(emptyList())

        // When
        val result = getSubscribedPodcastsUseCase().toList()

        // Then
        assertEquals(1, result.size)
        assertEquals(emptyList<Podcast>(), result[0])
        verify { podcastRepository.getSubscribedPodcasts() }
    }
}