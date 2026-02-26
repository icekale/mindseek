package com.mindseek.podcast.domain.usecase

import com.mindseek.podcast.domain.model.Resource
import com.mindseek.podcast.domain.repository.CommentRepository
import javax.inject.Inject

/**
 * 刷新评论用例
 */
class RefreshCommentsUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {
    /**
     * 刷新指定节目的评�?
     */
    suspend operator fun invoke(episodeId: String): Resource<Unit> {
        return commentRepository.refreshComments(episodeId)
    }
}