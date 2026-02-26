package com.mindseek.podcast.data.local.dao

import androidx.room.*
import com.mindseek.podcast.data.local.entity.Comment
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE episodeId = :episodeId ORDER BY timestamp DESC")
    fun getCommentsByEpisodeId(episodeId: String): Flow<List<Comment>>

    @Query("SELECT * FROM comments WHERE episodeId = :episodeId AND parentCommentId IS NULL ORDER BY timestamp DESC")
    fun getTopLevelCommentsByEpisodeId(episodeId: String): Flow<List<Comment>>

    @Query("SELECT * FROM comments WHERE parentCommentId = :parentCommentId ORDER BY timestamp ASC")
    fun getRepliesByParentId(parentCommentId: String): Flow<List<Comment>>

    @Query("SELECT * FROM comments WHERE id = :id")
    suspend fun getCommentById(id: String): Comment?

    @Query("SELECT * FROM comments WHERE userId = :userId ORDER BY timestamp DESC")
    fun getCommentsByUserId(userId: String): Flow<List<Comment>>

    @Query("SELECT COUNT(*) FROM comments WHERE episodeId = :episodeId")
    suspend fun getCommentCountByEpisodeId(episodeId: String): Int

    @Query("SELECT COUNT(*) FROM comments WHERE parentCommentId = :parentCommentId")
    suspend fun getReplyCountByParentId(parentCommentId: String): Int

    @Query("SELECT * FROM comments WHERE content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchComments(query: String): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<Comment>)

    @Update
    suspend fun updateComment(comment: Comment)

    @Delete
    suspend fun deleteComment(comment: Comment)

    @Query("UPDATE comments SET likeCount = :likeCount WHERE id = :id")
    suspend fun updateLikeCount(id: String, likeCount: Int)

    @Query("DELETE FROM comments WHERE episodeId = :episodeId")
    suspend fun deleteCommentsByEpisodeId(episodeId: String)

    @Query("DELETE FROM comments WHERE userId = :userId")
    suspend fun deleteCommentsByUserId(userId: String)
}