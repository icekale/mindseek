package com.mindseek.podcast.data.remote.api

import com.mindseek.podcast.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface CommentApiService {
    
    // Get comments for an episode
    @GET("episodes/{episodeId}/comments")
    suspend fun getCommentsByEpisodeId(
        @Path("episodeId") episodeId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("sort") sort: String = "recent" // recent, popular, oldest
    ): Response<PaginatedResponse<CommentDto>>

    // Get replies for a specific comment
    @GET("comments/{commentId}/replies")
    suspend fun getCommentReplies(
        @Path("commentId") commentId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<PaginatedResponse<CommentDto>>

    // Post a new comment
    @POST("episodes/{episodeId}/comments")
    suspend fun postComment(
        @Path("episodeId") episodeId: String,
        @Body request: PostCommentRequest
    ): Response<ApiResponse<CommentDto>>

    // Reply to a comment
    @POST("comments/{commentId}/reply")
    suspend fun replyToComment(
        @Path("commentId") commentId: String,
        @Body request: PostCommentRequest
    ): Response<ApiResponse<CommentDto>>

    // Like/unlike a comment
    @POST("comments/{id}/like")
    suspend fun likeComment(@Path("id") commentId: String): Response<ApiResponse<CommentDto>>

    @DELETE("comments/{id}/like")
    suspend fun unlikeComment(@Path("id") commentId: String): Response<ApiResponse<CommentDto>>

    // Update a comment
    @PUT("comments/{id}")
    suspend fun updateComment(
        @Path("id") commentId: String,
        @Body request: UpdateCommentRequest
    ): Response<ApiResponse<CommentDto>>

    // Delete a comment
    @DELETE("comments/{id}")
    suspend fun deleteComment(@Path("id") commentId: String): Response<ApiResponse<Unit>>

    // Report a comment
    @POST("comments/{id}/report")
    suspend fun reportComment(
        @Path("id") commentId: String,
        @Body request: ReportCommentRequest
    ): Response<ApiResponse<Unit>>

    // Get user's comments
    @GET("user/comments")
    suspend fun getUserComments(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PaginatedResponse<CommentDto>>
}

// Request DTOs for comment operations
data class PostCommentRequest(
    val content: String,
    val timestamp: Long? = null
)

data class UpdateCommentRequest(
    val content: String
)

data class ReportCommentRequest(
    val reason: String,
    val description: String? = null
)