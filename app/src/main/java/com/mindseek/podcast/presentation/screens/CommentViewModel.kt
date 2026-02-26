package com.mindseek.podcast.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 评论UI状�?
 */
data class CommentUiState(
    val comments: List<CommentDomain> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isPosting: Boolean = false,
    val isDeleting: Boolean = false,
    val isReporting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val commentCount: Int = 0,
    val currentEpisodeId: String? = null,
    val isUserLoggedIn: Boolean = false,
    val replyToComment: CommentDomain? = null,
    val showCommentOptions: CommentDomain? = null,
    val showDeleteDialog: CommentDomain? = null,
    val showReportDialog: CommentDomain? = null
)

/**
 * 评论ViewModel
 */
@HiltViewModel
class CommentViewModel @Inject constructor(
    private val getCommentsUseCase: GetCommentsUseCase,
    private val refreshCommentsUseCase: RefreshCommentsUseCase,
    private val likeCommentUseCase: LikeCommentUseCase,
    private val postCommentUseCase: PostCommentUseCase,
    private val replyToCommentUseCase: ReplyToCommentUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val reportCommentUseCase: ReportCommentUseCase,
    private val checkUserLoginStatusUseCase: CheckUserLoginStatusUseCase,
    private val commentRepository: CommentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommentUiState())
    val uiState: StateFlow<CommentUiState> = _uiState.asStateFlow()

    init {
        // 初始化时检查用户登录状�?
        checkUserLoginStatus()
    }

    /**
     * 加载指定节目的评�?
     */
    fun loadComments(episodeId: String) {
        if (_uiState.value.currentEpisodeId == episodeId) {
            return // Already loaded for this episode
        }

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            currentEpisodeId = episodeId,
            errorMessage = null
        )

        // Load comment count
        viewModelScope.launch {
            try {
                val count = commentRepository.getCommentCount(episodeId)
                _uiState.value = _uiState.value.copy(commentCount = count)
            } catch (e: Exception) {
                // Ignore error for comment count
            }
        }

        // Load comments
        getCommentsUseCase(episodeId)
            .catch { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "加载评论失败"
                )
            }
            .onEach { comments ->
                _uiState.value = _uiState.value.copy(
                    comments = comments,
                    isLoading = false,
                    errorMessage = null,
                    commentCount = comments.size
                )
            }
            .launchIn(viewModelScope)
    }

    /**
     * 刷新评论
     */
    fun refreshComments() {
        val episodeId = _uiState.value.currentEpisodeId ?: return

        _uiState.value = _uiState.value.copy(isRefreshing = true)

        viewModelScope.launch {
            when (val result = refreshCommentsUseCase(episodeId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        errorMessage = null
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        errorMessage = result.message
                    )
                }
                is Resource.Loading -> {
                    // Keep refreshing state
                }
            }
        }
    }

    /**
     * 点赞评论
     */
    fun likeComment(comment: CommentDomain) {
        viewModelScope.launch {
            when (val result = likeCommentUseCase(comment.id)) {
                is Resource.Success -> {
                    // Update the comment in the list
                    val updatedComments = _uiState.value.comments.map { existingComment ->
                        if (existingComment.id == comment.id) {
                            result.data ?: existingComment.copy(
                                isLiked = !existingComment.isLiked,
                                likeCount = if (existingComment.isLiked) 
                                    existingComment.likeCount - 1 
                                else 
                                    existingComment.likeCount + 1
                            )
                        } else {
                            // Also check replies
                            existingComment.copy(
                                replies = existingComment.replies.map { reply ->
                                    if (reply.id == comment.id) {
                                        result.data ?: reply.copy(
                                            isLiked = !reply.isLiked,
                                            likeCount = if (reply.isLiked) 
                                                reply.likeCount - 1 
                                            else 
                                                reply.likeCount + 1
                                        )
                                    } else {
                                        reply
                                    }
                                }
                            )
                        }
                    }
                    
                    _uiState.value = _uiState.value.copy(comments = updatedComments)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.message
                    )
                }
                is Resource.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    /**
     * 加载更多评论（分页）
     */
    fun loadMoreComments() {
        // TODO: Implement pagination logic
        // For now, this is a placeholder for future pagination implementation
    }

    /**
     * 清除错误消息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * 检查用户登录状�?
     */
    private fun checkUserLoginStatus() {
        val isLoggedIn = checkUserLoginStatusUseCase()
        _uiState.value = _uiState.value.copy(isUserLoggedIn = isLoggedIn)
    }

    /**
     * 发布评论
     */
    fun postComment(content: String) {
        val episodeId = _uiState.value.currentEpisodeId ?: return
        val parentCommentId = _uiState.value.replyToComment?.id

        if (!_uiState.value.isUserLoggedIn) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "请先登录后发表评论"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isPosting = true, errorMessage = null)

        viewModelScope.launch {
            when (val result = postCommentUseCase(episodeId, content, parentCommentId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isPosting = false,
                        replyToComment = null
                    )
                    // 刷新评论列表以显示新评论
                    refreshComments()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isPosting = false,
                        errorMessage = result.message
                    )
                }
                is Resource.Loading -> {
                    // Keep posting state
                }
            }
        }
    }

    /**
     * 设置回复目标评论
     */
    fun setReplyToComment(comment: CommentDomain?) {
        _uiState.value = _uiState.value.copy(replyToComment = comment)
    }

    /**
     * 取消回复
     */
    fun cancelReply() {
        _uiState.value = _uiState.value.copy(replyToComment = null)
    }

    /**
     * 处理评论回复
     */
    fun handleReplyToComment(comment: CommentDomain) {
        setReplyToComment(comment)
    }

    /**
     * 处理更多选项
     */
    fun handleMoreOptions(comment: CommentDomain) {
        _uiState.value = _uiState.value.copy(showCommentOptions = comment)
    }

    /**
     * 隐藏评论选项
     */
    fun hideCommentOptions() {
        _uiState.value = _uiState.value.copy(showCommentOptions = null)
    }

    /**
     * 显示删除确认对话�?
     */
    fun showDeleteDialog(comment: CommentDomain) {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = comment,
            showCommentOptions = null
        )
    }

    /**
     * 隐藏删除确认对话�?
     */
    fun hideDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = null)
    }

    /**
     * 删除评论
     */
    fun deleteComment(comment: CommentDomain) {
        _uiState.value = _uiState.value.copy(
            isDeleting = true,
            showDeleteDialog = null,
            errorMessage = null
        )

        viewModelScope.launch {
            when (val result = deleteCommentUseCase(comment.id)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        successMessage = "评论已删除"
                    )
                    // 刷新评论列表
                    refreshComments()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        errorMessage = result.message ?: "删除评论失败"
                    )
                }
                is Resource.Loading -> {
                    // Keep deleting state
                }
            }
        }
    }

    /**
     * 显示举报对话�?
     */
    fun showReportDialog(comment: CommentDomain) {
        _uiState.value = _uiState.value.copy(
            showReportDialog = comment,
            showCommentOptions = null
        )
    }

    /**
     * 隐藏举报对话�?
     */
    fun hideReportDialog() {
        _uiState.value = _uiState.value.copy(showReportDialog = null)
    }

    /**
     * 举报评论
     */
    fun reportComment(comment: CommentDomain, reason: String, description: String) {
        _uiState.value = _uiState.value.copy(
            isReporting = true,
            showReportDialog = null,
            errorMessage = null
        )

        viewModelScope.launch {
            when (val result = reportCommentUseCase(comment.id, reason, description)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isReporting = false,
                        successMessage = "举报已提交，感谢您的反馈馈"
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isReporting = false,
                        errorMessage = result.message ?: "举报提交失败"
                    )
                }
                is Resource.Loading -> {
                    // Keep reporting state
                }
            }
        }
    }

    /**
     * 分享评论
     */
    fun shareComment(comment: CommentDomain) {
        // TODO: Implement share functionality
        // This could trigger a share intent or copy to clipboard
        _uiState.value = _uiState.value.copy(
            successMessage = "评论链接已复制到剪贴板",
            showCommentOptions = null
        )
    }

    /**
     * 回复评论（使用专门的回复用例�?
     */
    fun replyToComment(parentComment: CommentDomain, content: String) {
        val episodeId = _uiState.value.currentEpisodeId ?: return

        if (!_uiState.value.isUserLoggedIn) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "请先登录后发表评论"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isPosting = true, errorMessage = null)

        viewModelScope.launch {
            when (val result = replyToCommentUseCase(episodeId, parentComment.id, content)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isPosting = false,
                        replyToComment = null,
                        successMessage = "回复发布成功功"
                    )
                    // 刷新评论列表以显示新回复
                    refreshComments()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isPosting = false,
                        errorMessage = result.message ?: "回复发布成功失败"
                    )
                }
                is Resource.Loading -> {
                    // Keep posting state
                }
            }
        }
    }

    /**
     * 清除成功消息
     */
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    /**
     * 处理用户点击
     */
    fun handleUserClick(userId: String) {
        // TODO: Navigate to user profile or show user info
        // For now, this is a placeholder
    }

    /**
     * 处理登录要求
     */
    fun handleLoginRequired() {
        // TODO: Navigate to login screen
        // For now, this is a placeholder that could trigger navigation
        _uiState.value = _uiState.value.copy(
            errorMessage = "请前往登录页面完成登录"
        )
    }
}