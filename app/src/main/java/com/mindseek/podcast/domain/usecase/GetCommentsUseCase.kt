package com.mindseek.podcast.domain.usecase

import com.mindseek.podcast.domain.model.CommentDomain
import com.mindseek.podcast.domain.repository.CommentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取评论列表用例
 */
class GetCommentsUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {
    /**
     * 获取指定节目的所有评论（包含回复�?
     */
    operator fun invoke(episodeId: String): Flow<List<CommentDomain>> {
        return commentRepository.getTopLevelCommentsByEpisodeId(episodeId)
    }
    
    /**
     * 获取指定评论的回�?
     */
    fun getReplies(parentCommentId: String): Flow<List<CommentDomain>> {
        return commentRepository.getRepliesByParentId(parentCommentId)
    }
}