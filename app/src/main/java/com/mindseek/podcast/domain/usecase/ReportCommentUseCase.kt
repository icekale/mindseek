package com.mindseek.podcast.domain.usecase

import com.mindseek.podcast.domain.model.Resource
import com.mindseek.podcast.domain.repository.CommentRepository
import javax.inject.Inject

/**
 * 举报评论用例
 */
class ReportCommentUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {
    /**
     * 举报评论
     */
    suspend operator fun invoke(
        commentId: String,
        reason: String,
        description: String? = null
    ): Resource<Unit> {
        return commentRepository.reportComment(commentId, reason, description)
    }
}