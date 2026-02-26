package com.mindseek.podcast.domain.usecase

import com.mindseek.podcast.domain.model.CommentDomain
import com.mindseek.podcast.domain.model.Resource
import com.mindseek.podcast.domain.repository.CommentRepository
import javax.inject.Inject

/**
 * 发布评论用例
 */
class PostCommentUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {
    /**
     * 发布评论
     * @param episodeId 节目ID
     * @param content 评论内容
     * @param parentCommentId 父评论ID（回复时使用）
     * @return 发布结果
     */
    suspend operator fun invoke(
        episodeId: String,
        content: String,
        parentCommentId: String? = null
    ): Resource<CommentDomain> {
        // 验证评论内容
        val validationResult = validateCommentContent(content)
        if (validationResult != null) {
            return Resource.Error(validationResult)
        }

        return commentRepository.postComment(episodeId, content, parentCommentId)
    }

    /**
     * 验证评论内容
     * @param content 评论内容
     * @return 错误信息，null表示验证通过
     */
    private fun validateCommentContent(content: String): String? {
        return when {
            content.isBlank() -> "评论内容不能为空"
            content.length < 2 -> "评论内容至少需要5个字符"
            content.length > 500 -> "评论内容不能超过500个字符"
            content.contains(Regex("[\\p{Cntrl}&&[^\r\n\t]]")) -> "评论内容包含非法字符"
            else -> null
        }
    }
}