package com.mindseek.podcast.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.mindseek.podcast.data.local.dao.DownloadTaskDao
import com.mindseek.podcast.data.local.dao.EpisodeDao
import com.mindseek.podcast.data.local.dao.FavoriteDao
import com.mindseek.podcast.data.local.dao.PlayHistoryDao
import com.mindseek.podcast.data.local.dao.PodcastDao
import com.mindseek.podcast.data.local.dao.SearchHistoryDao
import com.mindseek.podcast.data.local.entity.DownloadTask
import com.mindseek.podcast.data.local.entity.Episode
import com.mindseek.podcast.data.local.entity.Favorite
import com.mindseek.podcast.data.local.entity.PlayHistory
import com.mindseek.podcast.data.local.entity.Podcast
import com.mindseek.podcast.data.local.entity.SearchHistory

@Database(
    entities = [
        Podcast::class,
        Episode::class,
        PlayHistory::class,
        Favorite::class,
        SearchHistory::class,
        DownloadTask::class
    ],
    version = 6,
    exportSchema = true
)
abstract class PodcastDatabase : RoomDatabase() {
    abstract fun podcastDao(): PodcastDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun playHistoryDao(): PlayHistoryDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun downloadTaskDao(): DownloadTaskDao

    companion object {
        const val DATABASE_NAME = "podcast_database"
        
        // Migration from version 1 to 2: Add search history table
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create search history table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS search_history (
                        id TEXT PRIMARY KEY NOT NULL,
                        query TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        resultCount INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
                
                // Add index for better search performance
                database.execSQL("CREATE INDEX IF NOT EXISTS index_search_history_timestamp ON search_history(timestamp)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_search_history_query ON search_history(query)")
            }
        }

        // Migration from version 2 to 3: Add download tasks table
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create download tasks table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS download_tasks (
                        id TEXT PRIMARY KEY NOT NULL,
                        episodeId TEXT NOT NULL,
                        audioUrl TEXT NOT NULL,
                        localPath TEXT,
                        status TEXT NOT NULL,
                        progress REAL NOT NULL DEFAULT 0.0,
                        downloadedBytes INTEGER NOT NULL DEFAULT 0,
                        totalBytes INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        completedAt INTEGER,
                        errorMessage TEXT,
                        FOREIGN KEY(episodeId) REFERENCES episodes(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                
                // Add indices for better performance
                database.execSQL("CREATE INDEX IF NOT EXISTS index_download_tasks_episodeId ON download_tasks(episodeId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_download_tasks_status ON download_tasks(status)")
            }
        }

        // Migration from version 3 to 4: Add notification settings and playback statistics
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add notification settings columns to user_preferences or create dedicated table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS notification_settings (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        podcastId TEXT NOT NULL,
                        enabled INTEGER NOT NULL DEFAULT 1,
                        FOREIGN KEY(podcastId) REFERENCES podcasts(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                
                // Add playback statistics columns to episodes
                database.execSQL("ALTER TABLE episodes ADD COLUMN playCount INTEGER DEFAULT 0 NOT NULL")
                database.execSQL("ALTER TABLE episodes ADD COLUMN averageRating REAL DEFAULT 0.0 NOT NULL")
                
                // Add indices
                database.execSQL("CREATE INDEX IF NOT EXISTS index_notification_settings_podcast ON notification_settings(podcastId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_episodes_play_count ON episodes(playCount)")
            }
        }

        // Migration from version 4 to 5: Add Nio Radio fields to episodes (imageUrl, source, author, fileSize)
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE episodes ADD COLUMN imageUrl TEXT")
                database.execSQL("ALTER TABLE episodes ADD COLUMN source TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE episodes ADD COLUMN author TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE episodes ADD COLUMN fileSize INTEGER")
            }
        }

        // Migration from version 5 to 6: Drop comments table (Nio Radio doesn't support comments)
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS comments")
            }
        }

        // Helper function to get all migrations
        fun getAllMigrations(): Array<Migration> {
            return arrayOf(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6
            )
        }
        
        // Helper function to validate migration chain
        fun validateMigrationChain(): Boolean {
            val migrations = getAllMigrations()
            if (migrations.isEmpty()) return true
            
            val sortedMigrations = migrations.sortedBy { it.startVersion }
            
            // Check that migrations form a continuous chain
            for (i in 0 until sortedMigrations.size - 1) {
                val current = sortedMigrations[i]
                val next = sortedMigrations[i + 1]
                if (current.endVersion != next.startVersion) {
                    return false
                }
            }
            return true
        }
    }
}