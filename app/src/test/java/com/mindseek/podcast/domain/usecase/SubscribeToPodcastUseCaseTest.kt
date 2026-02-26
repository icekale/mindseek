package com.mindseek.podcast.domain.usecase

import com.mindseek.podcast.domain.repository.PodcastRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SubscribeToPodcastUseCaseTest {

    private lateinit var podcastRepository: PodcastRepository
    private lateinit var subscribeToPodcastUseCase: SubscribeToPodcastUseCase

    @Before
    fun setUp() {
        podcastRepository = mockk(relaxed = true)
        subscribeToPodcastUseCase = SubscribeToPodcastUseCase(podcastRepository)
    }

    @Test
    fun `invoke should call repository subscribeToPodcast`() = runTest {
        // Given
        val podcastId = "test-podcast-id"

        // When
        subscribeToPodcastUseCase(podcastId)

        // Then
        coVerify { podcastRepository.subscribeToPodcast(podcastId) }
    }

    @Test(expected = Exception::class)
    fun `invoke should throw exception when repository throws exception`() = runTest {
        // Given
        val podcastId = "test-podcast-id"
        coEvery { podcastRepository.subscribeToPodcast(podcastId) } throws Exception("Network error")

        // When
        subscribeToPodcastUseCase(podcastId)

        // Then - exception should be thrown
    }
}