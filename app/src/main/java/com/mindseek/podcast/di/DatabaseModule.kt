package com.mindseek.podcast.di

import android.content.Context
import androidx.room.Room
import com.mindseek.podcast.data.local.PodcastDatabase
import com.mindseek.podcast.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePodcastDatabase(@ApplicationContext context: Context): PodcastDatabase {
        return Room.databaseBuilder(
            context,
            PodcastDatabase::class.java,
            PodcastDatabase.DATABASE_NAME
        )
            .addMigrations(*PodcastDatabase.getAllMigrations())
            .fallbackToDestructiveMigration() // Only for development, remove in production
            .build()
    }

    @Provides
    fun providePodcastDao(database: PodcastDatabase): PodcastDao = database.podcastDao()

    @Provides
    fun provideEpisodeDao(database: PodcastDatabase): EpisodeDao = database.episodeDao()

    @Provides
    fun providePlayHistoryDao(database: PodcastDatabase): PlayHistoryDao = database.playHistoryDao()

    @Provides
    fun provideFavoriteDao(database: PodcastDatabase): FavoriteDao = database.favoriteDao()

    @Provides
    fun provideSearchHistoryDao(database: PodcastDatabase): SearchHistoryDao = database.searchHistoryDao()

    @Provides
    fun provideDownloadTaskDao(database: PodcastDatabase): DownloadTaskDao = database.downloadTaskDao()
}