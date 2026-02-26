package com.mindseek.podcast.domain.usecase.player

import com.mindseek.podcast.domain.model.EpisodeDomain
import com.mindseek.podcast.domain.model.PlayHistoryDomain
import com.mindseek.podcast.domain.repository.PlayHistoryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetPlayHistoryUseCaseTest {

    private lateinit var playHistoryRepository: PlayHistoryRepository
    private lateinit var getPlayHistoryUseCase: GetPlayHistoryUseCase

    @Before
    fun setUp() {
        playHistoryRepository = mockk(relaxed = true)
        getPlayHistoryUseCase = GetPlayHistoryUseCase(playHistoryRepository)
    }

    @Test
    fun `getByEpisodeId should return play history from repository`() = runTest {
        // Given
        val episodeId = "episode123"
        val episode = EpisodeDomain(
            id = episodeId,
            podcastId = "podcast123",
            title = "Test Episode",
            description = "Test Description",
            audioUrl = "https://example.com/audio.mp3",
            duration = 60000L,
            publishDate = System.currentTimeMillis()
        )
        val expectedHistory = PlayHistoryDomain(
            id = "history123",
            episode = episode,
            playPosition = 30000L,
            playDate = System.currentTimeMillis(),
            completionPercentage = 50f
        )
        coEvery { playHistoryRepository.getPlayHistoryByEpisodeId(episodeId) } returns expectedHistory

        // When
        val result = getPlayHistoryUseCase.getByEpisodeId(episodeId)

        // Then
        assertEquals(expectedHistory, result)
        coVerify { playHistoryRepository.getPlayHistoryByEpisodeId(episodeId) }
    }

    @Test
    fun `updatePlayPosition should call repository updatePlayPosition`() = runTest {
        // Given
        val episodeId = "episode123"
        val position = 45000L

        // When
        getPlayHistoryUseCase.updatePlayPosition(episodeId, position)

        // Then
        coVerify { playHistoryRepository.updatePlayPosition(episodeId, position) }
    }
}