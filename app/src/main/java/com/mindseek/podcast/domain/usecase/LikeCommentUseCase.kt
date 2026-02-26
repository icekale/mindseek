package com.mindseek.podcast.domain.usecase

import com.mindseek.podcast.domain.model.CommentDomain
import com.mindseek.podcast.domain.model.Resource
import com.mindseek.podcast.domain.repository.CommentRepository
import javax.inject.Inject

/**
 * 点赞评论用例
 */
class LikeCommentUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {
    /**
     * 点赞或取消点赞评�?
     */
    suspend operator fun invoke(commentId: String): Resource<CommentDomain> {
        return commentRepository.likeComment(commentId)
    }
}