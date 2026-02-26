package com.mindseek.podcast.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mindseek.podcast.data.local.PodcastDatabase
import com.mindseek.podcast.data.local.entity.Comment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CommentDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: PodcastDatabase
    private lateinit var commentDao: CommentDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PodcastDatabase::class.java
        ).allowMainThreadQueries().build()
        
        commentDao = database.commentDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetComment() = runTest {
        // Given
        val comment = createTestComment("1", "episode1", "user1", "Great episode!")

        // When
        commentDao.insertComment(comment)
        val retrieved = commentDao.getCommentById("1")

        // Then
        assertNotNull(retrieved)
        assertEquals(comment.id, retrieved?.id)
        assertEquals(comment.content, retrieved?.content)
        assertEquals(comment.episodeId, retrieved?.episodeId)
        assertEquals(comment.userId, retrieved?.userId)
    }

    @Test
    fun getCommentsByEpisodeId() = runTest {
        // Given
        val comments = listOf(
            createTestComment("1", "episode1", "user1", "First comment"),
            createTestComment("2", "episode1", "user2", "Second comment"),
            createTestComment("3", "episode2", "user1", "Different episode comment")
        )

        // When
        commentDao.insertComments(comments)
        val episode1Comments = commentDao.getCommentsByEpisodeId("episode1").first()

        // Then
        assertEquals(2, episode1Comments.size)
        assertTrue(episode1Comments.all { it.episodeId == "episode1" })
        assertTrue(episode1Comments.any { it.content == "First comment" })
        assertTrue(episode1Comments.any { it.content == "Second comment" })
    }

    @Test
    fun getTopLevelCommentsByEpisodeId() = runTest {
        // Given
        val comments = listOf(
            createTestComment("1", "episode1", "user1", "Top level comment"),
            createTestComment("2", "episode1", "user2", "Reply to comment 1", parentCommentId = "1"),
            createTestComment("3", "episode1", "user3", "Another top level comment"),
            createTestComment("4", "episode1", "user1", "Reply to comment 3", parentCommentId = "3")
        )

        // When
        commentDao.insertComments(comments)
        val topLevelComments = commentDao.getTopLevelCommentsByEpisodeId("episode1").first()

        // Then
        assertEquals(2, topLevelComments.size)
        assertTrue(topLevelComments.all { it.parentCommentId == null })
        assertTrue(topLevelComments.any { it.content == "Top level comment" })
        assertTrue(topLevelComments.any { it.content == "Another top level comment" })
    }

    @Test
    fun getRepliesByParentId() = runTest {
        // Given
        val comments = listOf(
            createTestComment("1", "episode1", "user1", "Parent comment"),
            createTestComment("2", "episode1", "user2", "First reply", parentCommentId = "1"),
            createTestComment("3", "episode1", "user3", "Second reply", parentCommentId = "1"),
            createTestComment("4", "episode1", "user4", "Different parent reply", parentCommentId = "5")
        )

        // When
        commentDao.insertComments(comments)
        val replies = commentDao.getRepliesByParentId("1").first()

        // Then
        assertEquals(2, replies.size)
        assertTrue(replies.all { it.parentCommentId == "1" })
        assertTrue(replies.any { it.content == "First reply" })
        assertTrue(replies.any { it.content == "Second reply" })
    }

    @Test
    fun getCommentsByUserId() = runTest {
        // Given
        val comments = listOf(
            createTestComment("1", "episode1", "user1", "User1 comment 1"),
            createTestComment("2", "episode1", "user2", "User2 comment"),
            createTestComment("3", "episode2", "user1", "User1 comment 2")
        )

        // When
        commentDao.insertComments(comments)
        val user1Comments = commentDao.getCommentsByUserId("user1").first()

        // Then
        assertEquals(2, user1Comments.size)
        assertTrue(user1Comments.all { it.userId == "user1" })
        assertTrue(user1Comments.any { it.content == "User1 comment 1" })
        assertTrue(user1Comments.any { it.content == "User1 comment 2" })
    }

    @Test
    fun getCommentCountByEpisodeId() = runTest {
        // Given
        val comments = listOf(
            createTestComment("1", "episode1", "user1", "Comment 1"),
            createTestComment("2", "episode1", "user2", "Comment 2"),
            createTestComment("3", "episode2", "user1", "Comment 3")
        )

        // When
        commentDao.insertComments(comments)
        val episode1Count = commentDao.getCommentCountByEpisodeId("episode1")

        // Then
        assertEquals(2, episode1Count)
    }

    @Test
    fun getReplyCountByParentId() = runTest {
        // Given
        val comments = listOf(
            createTestComment("1", "episode1", "user1", "Parent comment"),
            createTestComment("2", "episode1", "user2", "Reply 1", parentCommentId = "1"),
            createTestComment("3", "episode1", "user3", "Reply 2", parentCommentId = "1"),
            createTestComment("4", "episode1", "user4", "Reply 3", parentCommentId = "1")
        )

        // When
        commentDao.insertComments(comments)
        val replyCount = commentDao.getReplyCountByParentId("1")

        // Then
        assertEquals(3, replyCount)
    }

    @Test
    fun searchComments() = runTest {
        // Given
        val comments = listOf(
            createTestComment("1", "episode1", "user1", "This is a great episode about technology"),
            createTestComment("2", "episode1", "user2", "I love science podcasts"),
            createTestComment("3", "episode2", "user1", "Technology is fascinating")
        )

        // When
        commentDao.insertComments(comments)
        val searchResults = commentDao.searchComments("technology").first()

        // Then
        assertEquals(2, searchResults.size)
        assertTrue(searchResults.any { it.content.contains("technology", ignoreCase = true) })
    }

    @Test
    fun updateLikeCount() = runTest {
        // Given
        val comment = createTestComment("1", "episode1", "user1", "Great comment", likeCount = 5)

        // When
        commentDao.insertComment(comment)
        commentDao.updateLikeCount("1", 10)
        val updated = commentDao.getCommentById("1")

        // Then
        assertNotNull(updated)
        assertEquals(10, updated?.likeCount)
    }

    @Test
    fun updateComment() = runTest {
        // Given
        val originalComment = createTestComment("1", "episode1", "user1", "Original content")
        val updatedComment = originalComment.copy(content = "Updated content", likeCount = 5)

        // When
        commentDao.insertComment(originalComment)
        commentDao.updateComment(updatedComment)
        val retrieved = commentDao.getCommentById("1")

        // Then
        assertNotNull(retrieved)
        assertEquals("Updated content", retrieved?.content)
        assertEquals(5, retrieved?.likeCount)
    }

    @Test
    fun deleteComment() = runTest {
        // Given
        val comment = createTestComment("1", "episode1", "user1", "Test comment")

        // When
        commentDao.insertComment(comment)
        commentDao.deleteComment(comment)
        val retrieved = commentDao.getCommentById("1")

        // Then
        assertNull(retrieved)
    }

    @Test
    fun deleteCommentsByEpisodeId() = runTest {
        // Given
        val comments = listOf(
            createTestComment("1", "episode1", "user1", "Comment 1"),
            createTestComment("2", "episode1", "user2", "Comment 2"),
            createTestComment("3", "episode2", "user1", "Comment 3")
        )

        // When
        commentDao.insertComments(comments)
        commentDao.deleteCommentsByEpisodeId("episode1")
        val episode1Comments = commentDao.getCommentsByEpisodeId("episode1").first()
        val episode2Comments = commentDao.getCommentsByEpisodeId("episode2").first()

        // Then
        assertEquals(0, episode1Comments.size)
        assertEquals(1, episode2Comments.size)
    }

    @Test
    fun deleteCommentsByUserId() = runTest {
        // Given
        val comments = listOf(
            createTestComment("1", "episode1", "user1", "User1 comment 1"),
            createTestComment("2", "episode1", "user2", "User2 comment"),
            createTestComment("3", "episode2", "user1", "User1 comment 2")
        )

        // When
        commentDao.insertComments(comments)
        commentDao.deleteCommentsByUserId("user1")
        val user1Comments = commentDao.getCommentsByUserId("user1").first()
        val user2Comments = commentDao.getCommentsByUserId("user2").first()

        // Then
        assertEquals(0, user1Comments.size)
        assertEquals(1, user2Comments.size)
    }

    private fun createTestComment(
        id: String,
        episodeId: String,
        userId: String,
        content: String,
        likeCount: Int = 0,
        parentCommentId: String? = null,
        timestamp: Long = System.currentTimeMillis()
    ) = Comment(
        id = id,
        episodeId = episodeId,
        userId = userId,
        content = content,
        timestamp = timestamp,
        likeCount = likeCount,
        parentCommentId = parentCommentId
    )
}