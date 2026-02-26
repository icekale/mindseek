package com.mindseek.podcast.domain.model

data class CommentDomain(
    val id: String,
    val episodeId: String,
    val userId: String,
    val userName: String? = null,
    val userAvatar: String? = null,
    val content: String,
    val timestamp: Long,
    val likeCount: Int = 0,
    val parentCommentId: String? = null,
    val replies: List<CommentDomain> = emptyList(),
    val isLiked: Boolean = false,
    val isAuthor: Boolean = false, // Whether current user is the author
    val canDelete: Boolean = false, // Whether current user can delete this comment
    val canReport: Boolean = true   // Whether current user can report this comment
)