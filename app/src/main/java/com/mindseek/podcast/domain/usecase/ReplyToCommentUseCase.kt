package com.mindseek.podcast.domain.usecase

import com.mindseek.podcast.domain.model.CommentDomain
import com.mindseek.podcast.domain.model.Resource
import com.mindseek.podcast.domain.repository.CommentRepository
import javax.inject.Inject

/**
 * 回复评论用例
 */
class ReplyToCommentUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {
    /**
     * 回复指定评论
     */
    suspend operator fun invoke(
        episodeId: String,
        parentCommentId: String,
        content: String
    ): Resource<CommentDomain> {
        return commentRepository.postComment(episodeId, content, parentCommentId)
    }
}