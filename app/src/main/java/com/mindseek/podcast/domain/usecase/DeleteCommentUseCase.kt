package com.mindseek.podcast.domain.usecase

import com.mindseek.podcast.domain.model.Resource
import com.mindseek.podcast.domain.repository.CommentRepository
import javax.inject.Inject

/**
 * 删除评论用例
 */
class DeleteCommentUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {
    /**
     * 删除评论
     */
    suspend operator fun invoke(commentId: String): Resource<Unit> {
        return commentRepository.deleteComment(commentId)
    }
}