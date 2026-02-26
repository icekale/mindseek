package com.mindseek.podcast.data.mapper

import com.mindseek.podcast.data.local.entity.Comment
import com.mindseek.podcast.data.remote.dto.CommentDto
import com.mindseek.podcast.domain.model.CommentDomain

fun Comment.toDomain(
    userName: String? = null,
    userAvatar: String? = null,
    replies: List<CommentDomain> = emptyList(),
    isLiked: Boolean = false,
    isAuthor: Boolean = false,
    canDelete: Boolean = false,
    canReport: Boolean = true
): CommentDomain {
    return CommentDomain(
        id = id,
        episodeId = episodeId,
        userId = userId,
        userName = userName,
        userAvatar = userAvatar,
        content = content,
        timestamp = timestamp,
        likeCount = likeCount,
        parentCommentId = parentCommentId,
        replies = replies,
        isLiked = isLiked,
        isAuthor = isAuthor,
        canDelete = canDelete,
        canReport = canReport
    )
}

fun CommentDomain.toEntity(): Comment {
    return Comment(
        id = id,
        episodeId = episodeId,
        userId = userId,
        content = content,
        timestamp = timestamp,
        likeCount = likeCount,
        parentCommentId = parentCommentId
    )
}

fun CommentDto.toDomain(
    replies: List<CommentDomain> = emptyList()
): CommentDomain {
    return CommentDomain(
        id = id,
        episodeId = episodeId,
        userId = userId,
        userName = userName,
        userAvatar = userAvatar,
        content = content,
        timestamp = timestamp,
        likeCount = likeCount,
        parentCommentId = parentCommentId,
        replies = replies,
        isLiked = isLiked,
        isAuthor = isAuthor,
        canDelete = isAuthor, // User can delete their own comments
        canReport = !isAuthor // User can't report their own comments
    )
}

fun CommentDto.toEntity(): Comment {
    return Comment(
        id = id,
        episodeId = episodeId,
        userId = userId,
        content = content,
        timestamp = timestamp,
        likeCount = likeCount,
        parentCommentId = parentCommentId
    )
}