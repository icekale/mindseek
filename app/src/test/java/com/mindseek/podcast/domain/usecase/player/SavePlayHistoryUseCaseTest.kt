package com.mindseek.podcast.domain.usecase.player

import com.mindseek.podcast.domain.repository.PlayHistoryRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SavePlayHistoryUseCaseTest {

    private lateinit var playHistoryRepository: PlayHistoryRepository
    private lateinit var savePlayHistoryUseCase: SavePlayHistoryUseCase

    @Before
    fun setUp() {
        playHistoryRepository = mockk(relaxed = true)
        savePlayHistoryUseCase = SavePlayHistoryUseCase(playHistoryRepository)
    }

    @Test
    fun `invoke should call repository savePlayHistory`() = runTest {
        // Given
        val episodeId = "episode123"
        val position = 30000L

        // When
        savePlayHistoryUseCase(episodeId, position)

        // Then
        coVerify { playHistoryRepository.savePlayHistory(episodeId, position) }
    }
}