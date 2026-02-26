package com.mindseek.podcast.data.repository

import com.mindseek.podcast.data.local.dao.CommentDao
import com.mindseek.podcast.data.local.entity.Comment
import com.mindseek.podcast.data.mapper.toDomain
import com.mindseek.podcast.data.mapper.toEntity
import com.mindseek.podcast.data.remote.ApiServiceWrapper
import com.mindseek.podcast.data.remote.NetworkResult
import com.mindseek.podcast.data.remote.dto.CommentDto
import com.mindseek.podcast.domain.model.CommentDomain
import com.mindseek.podcast.domain.model.Resource
import com.mindseek.podcast.domain.repository.CommentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val commentDao: CommentDao,
    private val apiServiceWrapper: ApiServiceWrapper
) : CommentRepository {

    override fun getCommentsByEpisodeId(episodeId: String): Flow<List<CommentDomain>> {
        return commentDao.getCommentsByEpisodeId(episodeId).map { comments ->
            comments.map { comment ->
                val replies = commentDao.getRepliesByParentId(comment.id).first()
                    .map { it.toDomain() }
                comment.toDomain(replies = replies)
            }
        }
    }

    override fun getTopLevelCommentsByEpisodeId(episodeId: String): Flow<List<CommentDomain>> {
        return commentDao.getTopLevelCommentsByEpisodeId(episodeId).map { comments ->
            comments.map { comment ->
                val replies = commentDao.getRepliesByParentId(comment.id).first()
                    .map { it.toDomain() }
                comment.toDomain(replies = replies)
            }
        }
    }

    override fun getRepliesByParentId(parentCommentId: String): Flow<List<CommentDomain>> {
        return commentDao.getRepliesByParentId(parentCommentId).map { comments ->
            comments.map { it.toDomain() }
        }
    }

    override suspend fun getCommentById(id: String): CommentDomain? {
        val comment = commentDao.getCommentById(id) ?: return null
        val replies = commentDao.getRepliesByParentId(id).first()
            .map { it.toDomain() }
        return comment.toDomain(replies = replies)
    }

    override suspend fun postComment(
        episodeId: String,
        content: String,
        parentCommentId: String?
    ): Resource<CommentDomain> {
        return try {
            // Post to remote API using ApiServiceWrapper
            val result = apiServiceWrapper.postComment(episodeId, content)
            
            when (result) {
                is NetworkResult.Success -> {
                    // Save to local database
                    val localComment = result.data.toEntity()
                    commentDao.insertComment(localComment)
                    Resource.Success(localComment.toDomain())
                }
                is NetworkResult.Error -> {
                    // If remote fails, save locally only
                    val localComment = Comment(
                        id = UUID.randomUUID().toString(),
                        episodeId = episodeId,
                        userId = "current_user",
                        content = content,
                        timestamp = System.currentTimeMillis(),
                        likeCount = 0,
                        parentCommentId = parentCommentId
                    )
                    commentDao.insertComment(localComment)
                    Resource.Success(localComment.toDomain())
                }
                is NetworkResult.Loading -> {
                    Resource.Loading()
                }
            }
        } catch (e: Exception) {
            // If everything fails, return error
            Resource.Error(e.message ?: "Failed to post comment")
        }
    }

    override suspend fun likeComment(commentId: String): Resource<CommentDomain> {
        return try {
            // Update remote first
            val result = apiServiceWrapper.likeComment(commentId)
            
            when (result) {
                is NetworkResult.Success -> {
                    // Update local database
                    val localComment = result.data.toEntity()
                    commentDao.insertComment(localComment)
                    Resource.Success(localComment.toDomain())
                }
                is NetworkResult.Error -> {
                    // If remote fails, update locally
                    val localComment = commentDao.getCommentById(commentId)
                    if (localComment != null) {
                        commentDao.updateLikeCount(commentId, localComment.likeCount + 1)
                        Resource.Success(localComment.copy(likeCount = localComment.likeCount + 1).toDomain())
                    } else {
                        Resource.Error("Comment not found")
                    }
                }
                is NetworkResult.Loading -> {
                    Resource.Loading()
                }
            }
        } catch (e: Exception) {
            Resource.Error("Failed to like comment: ${e.message}")
        }
    }

    override suspend fun deleteComment(commentId: String): Resource<Unit> {
        return try {
            // Delete from remote first
            val result = apiServiceWrapper.deleteComment(commentId)
            
            when (result) {
                is NetworkResult.Success -> {
                    // Delete from local database
                    val comment = commentDao.getCommentById(commentId)
                    if (comment != null) {
                        commentDao.deleteComment(comment)
                    }
                    Resource.Success(Unit)
                }
                is NetworkResult.Error -> {
                    Resource.Error("Failed to delete comment: ${result.message}")
                }
                is NetworkResult.Loading -> {
                    Resource.Loading()
                }
            }
        } catch (e: Exception) {
            Resource.Error("Failed to delete comment: ${e.message}")
        }
    }

    override suspend fun reportComment(
        commentId: String,
        reason: String,
        description: String?
    ): Resource<Unit> {
        return try {
            // Report to remote API
            val result = apiServiceWrapper.reportComment(commentId, reason, description)
            
            when (result) {
                is NetworkResult.Success -> {
                    Resource.Success(Unit)
                }
                is NetworkResult.Error -> {
                    Resource.Error("Failed to report comment: ${result.message}")
                }
                is NetworkResult.Loading -> {
                    Resource.Loading()
                }
            }
        } catch (e: Exception) {
            Resource.Error("Failed to report comment: ${e.message}")
        }
    }

    override suspend fun refreshComments(episodeId: String): Resource<Unit> {
        return try {
            val result = apiServiceWrapper.getCommentsByEpisodeId(episodeId)
            
            when (result) {
                is NetworkResult.Success -> {
                    val localComments = result.data.items.map { it.toEntity() }
                    
                    // Clear existing comments for this episode and insert new ones
                    commentDao.deleteCommentsByEpisodeId(episodeId)
                    commentDao.insertComments(localComments)
                    
                    Resource.Success(Unit)
                }
                is NetworkResult.Error -> {
                    Resource.Error("Failed to refresh comments: ${result.message}")
                }
                is NetworkResult.Loading -> {
                    Resource.Loading()
                }
            }
        } catch (e: Exception) {
            Resource.Error("Failed to refresh comments: ${e.message}")
        }
    }

    override suspend fun getCommentCount(episodeId: String): Int {
        return commentDao.getCommentCountByEpisodeId(episodeId)
    }
}