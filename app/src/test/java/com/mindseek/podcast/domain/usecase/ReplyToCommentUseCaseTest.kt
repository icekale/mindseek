package com.mindseek.podcast.domain.usecase

import com.mindseek.podcast.domain.model.CommentDomain
import com.mindseek.podcast.domain.model.Resource
import com.mindseek.podcast.domain.repository.CommentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReplyToCommentUseCaseTest {

    private lateinit var commentRepository: CommentRepository
    private lateinit var replyToCommentUseCase: ReplyToCommentUseCase

    @Before
    fun setUp() {
        commentRepository = mockk()
        replyToCommentUseCase = ReplyToCommentUseCase(commentRepository)
    }

    @Test
    fun `invoke should call repository postComment with correct parameters`() = runTest {
        // Given
        val episodeId = "episode123"
        val parentCommentId = "parent123"
        val content = "This is a reply"
        val expectedComment = CommentDomain(
            id = "reply123",
            episodeId = episodeId,
            userId = "user123",
            userName = "Test User",
            content = content,
            timestamp = System.currentTimeMillis(),
            parentCommentId = parentCommentId
        )

        coEvery { 
            commentRepository.postComment(episodeId, content, parentCommentId) 
        } returns Resource.Success(expectedComment)

        // When
        val result = replyToCommentUseCase(episodeId, parentCommentId, content)

        // Then
        coVerify { commentRepository.postComment(episodeId, content, parentCommentId) }
        assertTrue(result is Resource.Success)
        assertEquals(expectedComment, result.data)
    }

    @Test
    fun `invoke should return error when repository fails`() = runTest {
        // Given
        val episodeId = "episode123"
        val parentCommentId = "parent123"
        val content = "This is a reply"
        val errorMessage = "Failed to post reply"

        coEvery { 
            commentRepository.postComment(episodeId, content, parentCommentId) 
        } returns Resource.Error(errorMessage)

        // When
        val result = replyToCommentUseCase(episodeId, parentCommentId, content)

        // Then
        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, result.message)
    }

    @Test
    fun `invoke should return loading when repository is loading`() = runTest {
        // Given
        val episodeId = "episode123"
        val parentCommentId = "parent123"
        val content = "This is a reply"

        coEvery { 
            commentRepository.postComment(episodeId, content, parentCommentId) 
        } returns Resource.Loading()

        // When
        val result = replyToCommentUseCase(episodeId, parentCommentId, content)

        // Then
        assertTrue(result is Resource.Loading)
    }
}