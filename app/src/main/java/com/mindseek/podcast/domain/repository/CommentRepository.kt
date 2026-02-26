package com.mindseek.podcast.domain.repository

import com.mindseek.podcast.domain.model.CommentDomain
import com.mindseek.podcast.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    fun getCommentsByEpisodeId(episodeId: String): Flow<List<CommentDomain>>
    fun getTopLevelCommentsByEpisodeId(episodeId: String): Flow<List<CommentDomain>>
    fun getRepliesByParentId(parentCommentId: String): Flow<List<CommentDomain>>
    suspend fun getCommentById(id: String): CommentDomain?
    suspend fun postComment(episodeId: String, content: String, parentCommentId: String? = null): Resource<CommentDomain>
    suspend fun likeComment(commentId: String): Resource<CommentDomain>
    suspend fun deleteComment(commentId: String): Resource<Unit>
    suspend fun reportComment(commentId: String, reason: String, description: String? = null): Resource<Unit>
    suspend fun refreshComments(episodeId: String): Resource<Unit>
    suspend fun getCommentCount(episodeId: String): Int
}