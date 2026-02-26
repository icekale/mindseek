package com.mindseek.podcast.domain.usecase

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

class DeleteCommentUseCaseTest {

    private lateinit var commentRepository: CommentRepository
    private lateinit var deleteCommentUseCase: DeleteCommentUseCase

    @Before
    fun setUp() {
        commentRepository = mockk()
        deleteCommentUseCase = DeleteCommentUseCase(commentRepository)
    }

    @Test
    fun `invoke should call repository deleteComment with correct parameters`() = runTest {
        // Given
        val commentId = "comment123"

        coEvery { 
            commentRepository.deleteComment(commentId) 
        } returns Resource.Success(Unit)

        // When
        val result = deleteCommentUseCase(commentId)

        // Then
        coVerify { commentRepository.deleteComment(commentId) }
        assertTrue(result is Resource.Success)
    }

    @Test
    fun `invoke should return error when repository fails`() = runTest {
        // Given
        val commentId = "comment123"
        val errorMessage = "Failed to delete comment"

        coEvery { 
            commentRepository.deleteComment(commentId) 
        } returns Resource.Error(errorMessage)

        // When
        val result = deleteCommentUseCase(commentId)

        // Then
        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, result.message)
    }

    @Test
    fun `invoke should return loading when repository is loading`() = runTest {
        // Given
        val commentId = "comment123"

        coEvery { 
            commentRepository.deleteComment(commentId) 
        } returns Resource.Loading()

        // When
        val result = deleteCommentUseCase(commentId)

        // Then
        assertTrue(result is Resource.Loading)
    }
}