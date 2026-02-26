package com.mindseek.podcast.presentation.screens

import com.mindseek.podcast.domain.model.CommentDomain
import com.mindseek.podcast.domain.model.Resource
import com.mindseek.podcast.domain.repository.CommentRepository
import com.mindseek.podcast.domain.usecase.CheckUserLoginStatusUseCase
import com.mindseek.podcast.domain.usecase.DeleteCommentUseCase
import com.mindseek.podcast.domain.usecase.GetCommentsUseCase
import com.mindseek.podcast.domain.usecase.LikeCommentUseCase
import com.mindseek.podcast.domain.usecase.PostCommentUseCase
import com.mindseek.podcast.domain.usecase.RefreshCommentsUseCase
import com.mindseek.podcast.domain.usecase.ReplyToCommentUseCase
import com.mindseek.podcast.domain.usecase.ReportCommentUseCase
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import org.mockito.kotlin.any

class CommentViewModelTest {

    @Mock
    private lateinit var getCommentsUseCase: GetCommentsUseCase

    @Mock
    private lateinit var refreshCommentsUseCase: RefreshCommentsUseCase

    @Mock
    private lateinit var likeCommentUseCase: LikeCommentUseCase

    @Mock
    private lateinit var postCommentUseCase: PostCommentUseCase

    @Mock
    private lateinit var replyToCommentUseCase: ReplyToCommentUseCase

    @Mock
    private lateinit var deleteCommentUseCase: DeleteCommentUseCase

    @Mock
    private lateinit var reportCommentUseCase: ReportCommentUseCase

    @Mock
    private lateinit var checkUserLoginStatusUseCase: CheckUserLoginStatusUseCase

    @Mock
    private lateinit var commentRepository: CommentRepository

    private lateinit var viewModel: CommentViewModel

    private val testEpisodeId = "test_episode_id"
    private val testComments = listOf(
        CommentDomain(
            id = "comment1",
            episodeId = testEpisodeId,
            userId = "user1",
            userName = "Test User 1",
            userAvatar = null,
            content = "This is a test comment",
            timestamp = System.currentTimeMillis(),
            likeCount = 5,
            parentCommentId = null,
            replies = emptyList(),
            isLiked = false
        ),
        CommentDomain(
            id = "comment2",
            episodeId = testEpisodeId,
            userId = "user2",
            userName = "Test User 2",
            userAvatar = null,
            content = "This is another test comment",
            timestamp = System.currentTimeMillis() - 1000,
            likeCount = 2,
            parentCommentId = null,
            replies = emptyList(),
            isLiked = true
        )
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        whenever(checkUserLoginStatusUseCase()).thenReturn(true)
        viewModel = CommentViewModel(
            getCommentsUseCase = getCommentsUseCase,
            refreshCommentsUseCase = refreshCommentsUseCase,
            likeCommentUseCase = likeCommentUseCase,
            postCommentUseCase = postCommentUseCase,
            replyToCommentUseCase = replyToCommentUseCase,
            deleteCommentUseCase = deleteCommentUseCase,
            reportCommentUseCase = reportCommentUseCase,
            checkUserLoginStatusUseCase = checkUserLoginStatusUseCase,
            commentRepository = commentRepository
        )
    }

    @Test
    fun `loadComments should update UI state with comments`() = runTest {
        // Given
        whenever(getCommentsUseCase(testEpisodeId)).thenReturn(flowOf(testComments))
        whenever(commentRepository.getCommentCount(testEpisodeId)).thenReturn(testComments.size)

        // When
        viewModel.loadComments(testEpisodeId)

        // Then
        val uiState = viewModel.uiState.value
        assertEquals(testComments, uiState.comments)
        assertEquals(testComments.size, uiState.commentCount)
        assertEquals(testEpisodeId, uiState.currentEpisodeId)
        assertFalse(uiState.isLoading)
        assertNull(uiState.errorMessage)
    }

    @Test
    fun `loadComments should not reload if same episode`() = runTest {
        // Given
        whenever(getCommentsUseCase(testEpisodeId)).thenReturn(flowOf(testComments))
        whenever(commentRepository.getCommentCount(testEpisodeId)).thenReturn(testComments.size)

        // When
        viewModel.loadComments(testEpisodeId)
        val firstState = viewModel.uiState.value
        
        viewModel.loadComments(testEpisodeId) // Load same episode again
        val secondState = viewModel.uiState.value

        // Then
        assertEquals(firstState, secondState)
    }

    @Test
    fun `clearError should clear error message`() = runTest {
        // Given - simulate error state
        whenever(getCommentsUseCase(testEpisodeId)).thenThrow(RuntimeException("Test error"))

        // When
        viewModel.loadComments(testEpisodeId)
        viewModel.clearError()

        // Then
        val uiState = viewModel.uiState.value
        assertNull(uiState.errorMessage)
    }

    @Test
    fun `postComment should post comment successfully when user is logged in`() = runTest {
        // Given
        val content = "This is a new comment"
        val newComment = CommentDomain(
            id = "new_comment",
            episodeId = testEpisodeId,
            userId = "current_user",
            userName = "Current User",
            userAvatar = null,
            content = content,
            timestamp = System.currentTimeMillis(),
            likeCount = 0,
            parentCommentId = null,
            replies = emptyList(),
            isLiked = false
        )

        whenever(postCommentUseCase(testEpisodeId, content, null)).thenReturn(Resource.Success(newComment))
        whenever(refreshCommentsUseCase(testEpisodeId)).thenReturn(Resource.Success(Unit))
        whenever(getCommentsUseCase(testEpisodeId)).thenReturn(flowOf(testComments + newComment))
        whenever(commentRepository.getCommentCount(testEpisodeId)).thenReturn(testComments.size)

        // Load comments first
        viewModel.loadComments(testEpisodeId)

        // When
        viewModel.postComment(content)

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isPosting)
        assertNull(uiState.errorMessage)
        verify(postCommentUseCase).invoke(testEpisodeId, content, null)
    }

    @Test
    fun `postComment should show error when user is not logged in`() = runTest {
        // Given
        whenever(checkUserLoginStatusUseCase()).thenReturn(false)
        viewModel = CommentViewModel(
            getCommentsUseCase = getCommentsUseCase,
            refreshCommentsUseCase = refreshCommentsUseCase,
            likeCommentUseCase = likeCommentUseCase,
            postCommentUseCase = postCommentUseCase,
            replyToCommentUseCase = replyToCommentUseCase,
            deleteCommentUseCase = deleteCommentUseCase,
            reportCommentUseCase = reportCommentUseCase,
            checkUserLoginStatusUseCase = checkUserLoginStatusUseCase,
            commentRepository = commentRepository
        )

        val content = "This is a new comment"
        viewModel.loadComments(testEpisodeId)

        // When
        viewModel.postComment(content)

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isPosting)
        assertEquals("请先登录后发表评论", uiState.errorMessage)
        verify(postCommentUseCase, org.mockito.kotlin.never()).invoke(any(), any(), any())
    }

    @Test
    fun `postComment should handle repository error`() = runTest {
        // Given
        val content = "This is a new comment"
        val errorMessage = "Network error"

        whenever(postCommentUseCase(testEpisodeId, content, null)).thenReturn(Resource.Error(errorMessage))
        whenever(getCommentsUseCase(testEpisodeId)).thenReturn(flowOf(testComments))
        whenever(commentRepository.getCommentCount(testEpisodeId)).thenReturn(testComments.size)

        // Load comments first
        viewModel.loadComments(testEpisodeId)

        // When
        viewModel.postComment(content)

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isPosting)
        assertEquals(errorMessage, uiState.errorMessage)
    }

    @Test
    fun `setReplyToComment should update reply target`() = runTest {
        // Given
        val targetComment = testComments.first()

        // When
        viewModel.setReplyToComment(targetComment)

        // Then
        val uiState = viewModel.uiState.value
        assertEquals(targetComment, uiState.replyToComment)
    }

    @Test
    fun `cancelReply should clear reply target`() = runTest {
        // Given
        val targetComment = testComments.first()
        viewModel.setReplyToComment(targetComment)

        // When
        viewModel.cancelReply()

        // Then
        val uiState = viewModel.uiState.value
        assertNull(uiState.replyToComment)
    }

    @Test
    fun `postComment with reply should post reply comment`() = runTest {
        // Given
        val content = "This is a reply"
        val parentComment = testComments.first()
        val replyComment = CommentDomain(
            id = "reply_comment",
            episodeId = testEpisodeId,
            userId = "current_user",
            userName = "Current User",
            userAvatar = null,
            content = content,
            timestamp = System.currentTimeMillis(),
            likeCount = 0,
            parentCommentId = parentComment.id,
            replies = emptyList(),
            isLiked = false
        )

        whenever(postCommentUseCase(testEpisodeId, content, parentComment.id)).thenReturn(Resource.Success(replyComment))
        whenever(refreshCommentsUseCase(testEpisodeId)).thenReturn(Resource.Success(Unit))
        whenever(getCommentsUseCase(testEpisodeId)).thenReturn(flowOf(testComments))
        whenever(commentRepository.getCommentCount(testEpisodeId)).thenReturn(testComments.size)

        // Load comments and set reply target
        viewModel.loadComments(testEpisodeId)
        viewModel.setReplyToComment(parentComment)

        // When
        viewModel.postComment(content)

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isPosting)
        assertNull(uiState.replyToComment) // Should be cleared after posting
        verify(postCommentUseCase).invoke(testEpisodeId, content, parentComment.id)
    }

    @Test
    fun `handleLoginRequired should set error message`() = runTest {
        // When
        viewModel.handleLoginRequired()

        // Then
        val uiState = viewModel.uiState.value
        assertEquals("请前往登录页面完成登录", uiState.errorMessage)
    }

    @Test
    fun `initial state should check user login status`() = runTest {
        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState.isUserLoggedIn)
        verify(checkUserLoginStatusUseCase).invoke()
    }

    @Test
    fun `handleMoreOptions should show comment options`() = runTest {
        // Given
        val comment = testComments.first()

        // When
        viewModel.handleMoreOptions(comment)

        // Then
        val uiState = viewModel.uiState.value
        assertEquals(comment, uiState.showCommentOptions)
    }

    @Test
    fun `hideCommentOptions should clear comment options`() = runTest {
        // Given
        val comment = testComments.first()
        viewModel.handleMoreOptions(comment)

        // When
        viewModel.hideCommentOptions()

        // Then
        val uiState = viewModel.uiState.value
        assertNull(uiState.showCommentOptions)
    }

    @Test
    fun `showDeleteDialog should show delete dialog and hide options`() = runTest {
        // Given
        val comment = testComments.first()
        viewModel.handleMoreOptions(comment)

        // When
        viewModel.showDeleteDialog(comment)

        // Then
        val uiState = viewModel.uiState.value
        assertEquals(comment, uiState.showDeleteDialog)
        assertNull(uiState.showCommentOptions)
    }

    @Test
    fun `hideDeleteDialog should clear delete dialog`() = runTest {
        // Given
        val comment = testComments.first()
        viewModel.showDeleteDialog(comment)

        // When
        viewModel.hideDeleteDialog()

        // Then
        val uiState = viewModel.uiState.value
        assertNull(uiState.showDeleteDialog)
    }

    @Test
    fun `deleteComment should delete comment successfully`() = runTest {
        // Given
        val comment = testComments.first()
        whenever(deleteCommentUseCase(comment.id)).thenReturn(Resource.Success(Unit))
        whenever(refreshCommentsUseCase(testEpisodeId)).thenReturn(Resource.Success(Unit))
        whenever(getCommentsUseCase(testEpisodeId)).thenReturn(flowOf(testComments))
        whenever(commentRepository.getCommentCount(testEpisodeId)).thenReturn(testComments.size)

        // Load comments first
        viewModel.loadComments(testEpisodeId)

        // When
        viewModel.deleteComment(comment)

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isDeleting)
        assertEquals("评论已删除", uiState.successMessage)
        assertNull(uiState.showDeleteDialog)
        verify(deleteCommentUseCase).invoke(comment.id)
    }

    @Test
    fun `deleteComment should handle error`() = runTest {
        // Given
        val comment = testComments.first()
        val errorMessage = "Failed to delete comment"
        whenever(deleteCommentUseCase(comment.id)).thenReturn(Resource.Error(errorMessage))

        // When
        viewModel.deleteComment(comment)

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isDeleting)
        assertEquals(errorMessage, uiState.errorMessage)
        assertNull(uiState.showDeleteDialog)
    }

    @Test
    fun `showReportDialog should show report dialog and hide options`() = runTest {
        // Given
        val comment = testComments.first()
        viewModel.handleMoreOptions(comment)

        // When
        viewModel.showReportDialog(comment)

        // Then
        val uiState = viewModel.uiState.value
        assertEquals(comment, uiState.showReportDialog)
        assertNull(uiState.showCommentOptions)
    }

    @Test
    fun `hideReportDialog should clear report dialog`() = runTest {
        // Given
        val comment = testComments.first()
        viewModel.showReportDialog(comment)

        // When
        viewModel.hideReportDialog()

        // Then
        val uiState = viewModel.uiState.value
        assertNull(uiState.showReportDialog)
    }

    @Test
    fun `reportComment should report comment successfully`() = runTest {
        // Given
        val comment = testComments.first()
        val reason = "spam"
        val description = "This is spam content"
        whenever(reportCommentUseCase(comment.id, reason, description)).thenReturn(Resource.Success(Unit))

        // When
        viewModel.reportComment(comment, reason, description)

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isReporting)
        assertEquals("举报已提交，感谢您的反馈", uiState.successMessage)
        assertNull(uiState.showReportDialog)
        verify(reportCommentUseCase).invoke(comment.id, reason, description)
    }

    @Test
    fun `reportComment should handle error`() = runTest {
        // Given
        val comment = testComments.first()
        val reason = "spam"
        val description = "This is spam content"
        val errorMessage = "Failed to report comment"
        whenever(reportCommentUseCase(comment.id, reason, description)).thenReturn(Resource.Error(errorMessage))

        // When
        viewModel.reportComment(comment, reason, description)

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isReporting)
        assertEquals(errorMessage, uiState.errorMessage)
        assertNull(uiState.showReportDialog)
    }

    @Test
    fun `shareComment should show success message and hide options`() = runTest {
        // Given
        val comment = testComments.first()
        viewModel.handleMoreOptions(comment)

        // When
        viewModel.shareComment(comment)

        // Then
        val uiState = viewModel.uiState.value
        assertEquals("评论链接已复制到剪贴板", uiState.successMessage)
        assertNull(uiState.showCommentOptions)
    }

    @Test
    fun `replyToComment should use reply use case when user is logged in`() = runTest {
        // Given
        val parentComment = testComments.first()
        val content = "This is a reply"
        val replyComment = CommentDomain(
            id = "reply123",
            episodeId = testEpisodeId,
            userId = "current_user",
            userName = "Current User",
            content = content,
            timestamp = System.currentTimeMillis(),
            parentCommentId = parentComment.id
        )

        whenever(replyToCommentUseCase(testEpisodeId, parentComment.id, content)).thenReturn(Resource.Success(replyComment))
        whenever(refreshCommentsUseCase(testEpisodeId)).thenReturn(Resource.Success(Unit))
        whenever(getCommentsUseCase(testEpisodeId)).thenReturn(flowOf(testComments))
        whenever(commentRepository.getCommentCount(testEpisodeId)).thenReturn(testComments.size)

        // Load comments first
        viewModel.loadComments(testEpisodeId)

        // When
        viewModel.replyToComment(parentComment, content)

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isPosting)
        assertNull(uiState.replyToComment)
        assertEquals("回复发布成功", uiState.successMessage)
        verify(replyToCommentUseCase).invoke(testEpisodeId, parentComment.id, content)
    }

    @Test
    fun `replyToComment should show error when user is not logged in`() = runTest {
        // Given
        whenever(checkUserLoginStatusUseCase()).thenReturn(false)
        viewModel = CommentViewModel(
            getCommentsUseCase = getCommentsUseCase,
            refreshCommentsUseCase = refreshCommentsUseCase,
            likeCommentUseCase = likeCommentUseCase,
            postCommentUseCase = postCommentUseCase,
            replyToCommentUseCase = replyToCommentUseCase,
            deleteCommentUseCase = deleteCommentUseCase,
            reportCommentUseCase = reportCommentUseCase,
            checkUserLoginStatusUseCase = checkUserLoginStatusUseCase,
            commentRepository = commentRepository
        )

        val parentComment = testComments.first()
        val content = "This is a reply"
        viewModel.loadComments(testEpisodeId)

        // When
        viewModel.replyToComment(parentComment, content)

        // Then
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isPosting)
        assertEquals("请先登录后发表评论", uiState.errorMessage)
        verify(replyToCommentUseCase, org.mockito.kotlin.never()).invoke(any(), any(), any())
    }

    @Test
    fun `clearSuccessMessage should clear success message`() = runTest {
        // Given
        val comment = testComments.first()
        viewModel.shareComment(comment) // This sets a success message

        // When
        viewModel.clearSuccessMessage()

        // Then
        val uiState = viewModel.uiState.value
        assertNull(uiState.successMessage)
    }
}