package com.mindseek.podcast.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object for Comment from API
 */
data class CommentDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("episode_id")
    val episodeId: String,
    
    @SerializedName("user_id")
    val userId: String,
    
    @SerializedName("user_name")
    val userName: String,
    
    @SerializedName("user_avatar")
    val userAvatar: String? = null,
    
    @SerializedName("content")
    val content: String,
    
    @SerializedName("timestamp")
    val timestamp: Long,
    
    @SerializedName("like_count")
    val likeCount: Int = 0,
    
    @SerializedName("parent_comment_id")
    val parentCommentId: String? = null,
    
    @SerializedName("reply_count")
    val replyCount: Int = 0,
    
    @SerializedName("is_liked")
    val isLiked: Boolean = false,
    
    @SerializedName("is_author")
    val isAuthor: Boolean = false,
    
    @SerializedName("replies")
    val replies: List<CommentDto>? = null
)