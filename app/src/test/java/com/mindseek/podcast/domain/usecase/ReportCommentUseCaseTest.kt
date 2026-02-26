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

class ReportCommentUseCaseTest {

    private lateinit var commentRepository: CommentRepository
    private lateinit var reportCommentUseCase: ReportCommentUseCase

    @Before
    fun setUp() {
        commentRepository = mockk()
        reportCommentUseCase = ReportCommentUseCase(commentRepository)
    }

    @Test
    fun `invoke should call repository reportComment with correct parameters`() = runTest {
        // Given
        val commentId = "comment123"
        val reason = "spam"
        val description = "This is spam content"

        coEvery { 
            commentRepository.reportComment(commentId, reason, description) 
        } returns Resource.Success(Unit)

        // When
        val result = reportCommentUseCase(commentId, reason, description)

        // Then
        coVerify { commentRepository.reportComment(commentId, reason, description) }
        assertTrue(result is Resource.Success)
    }

    @Test
    fun `invoke should work with null description`() = runTest {
        // Given
        val commentId = "comment123"
        val reason = "harassment"
        val description: String? = null

        coEvery { 
            commentRepository.reportComment(commentId, reason, description) 
        } returns Resource.Success(Unit)

        // When
        val result = reportCommentUseCase(commentId, reason, description)

        // Then
        coVerify { commentRepository.reportComment(commentId, reason, description) }
        assertTrue(result is Resource.Success)
    }

    @Test
    fun `invoke should return error when repository fails`() = runTest {
        // Given
        val commentId = "comment123"
        val reason = "spam"
        val description = "This is spam content"
        val errorMessage = "Failed to report comment"

        coEvery { 
            commentRepository.reportComment(commentId, reason, description) 
        } returns Resource.Error(errorMessage)

        // When
        val result = reportCommentUseCase(commentId, reason, description)

        // Then
        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, result.message)
    }

    @Test
    fun `invoke should return loading when repository is loading`() = runTest {
        // Given
        val commentId = "comment123"
        val reason = "spam"
        val description = "This is spam content"

        coEvery { 
            commentRepository.reportComment(commentId, reason, description) 
        } returns Resource.Loading()

        // When
        val result = reportCommentUseCase(commentId, reason, description)

        // Then
        assertTrue(result is Resource.Loading)
    }
}