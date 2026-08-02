package com.mindseek.podcast.di

import com.mindseek.podcast.data.repository.DownloadRepositoryImpl
import com.mindseek.podcast.data.repository.FavoriteRepositoryImpl
import com.mindseek.podcast.data.repository.PlayHistoryRepositoryImpl
import com.mindseek.podcast.data.repository.PodcastRepositoryImpl
import com.mindseek.podcast.data.repository.SearchRepositoryImpl
import com.mindseek.podcast.domain.repository.DownloadRepository
import com.mindseek.podcast.domain.repository.FavoriteRepository
import com.mindseek.podcast.domain.repository.PlayHistoryRepository
import com.mindseek.podcast.domain.repository.PodcastRepository
import com.mindseek.podcast.domain.repository.SearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPodcastRepository(
        podcastRepositoryImpl: PodcastRepositoryImpl
    ): PodcastRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(
        favoriteRepositoryImpl: FavoriteRepositoryImpl
    ): FavoriteRepository

    @Binds
    @Singleton
    abstract fun bindPlayHistoryRepository(
        playHistoryRepositoryImpl: PlayHistoryRepositoryImpl
    ): PlayHistoryRepository

    @Binds
    @Singleton
    abstract fun bindSearchRepository(
        searchRepositoryImpl: SearchRepositoryImpl
    ): SearchRepository

    @Binds
    @Singleton
    abstract fun bindDownloadRepository(
        downloadRepositoryImpl: DownloadRepositoryImpl
    ): DownloadRepository
}