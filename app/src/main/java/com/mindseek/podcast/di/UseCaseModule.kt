package com.mindseek.podcast.di

import com.mindseek.podcast.domain.repository.FavoriteRepository
import com.mindseek.podcast.domain.repository.PodcastRepository
import com.mindseek.podcast.domain.repository.PlayHistoryRepository
import com.mindseek.podcast.domain.usecase.AddToFavoritesUseCase
import com.mindseek.podcast.domain.usecase.ClearPlayHistoryUseCase
import com.mindseek.podcast.domain.usecase.GetAllFavoritesUseCase
import com.mindseek.podcast.domain.usecase.GetAllPlayHistoryUseCase
import com.mindseek.podcast.domain.usecase.GetPodcastDetailUseCase
import com.mindseek.podcast.domain.usecase.GetPodcastEpisodesUseCase
import com.mindseek.podcast.domain.usecase.GetRecommendedPodcastsUseCase
import com.mindseek.podcast.domain.usecase.RemoveFromFavoritesUseCase
import com.mindseek.podcast.domain.usecase.SearchEpisodesUseCase
import com.mindseek.podcast.domain.usecase.SearchFavoritesUseCase
import com.mindseek.podcast.domain.usecase.SearchPlayHistoryUseCase
import com.mindseek.podcast.domain.usecase.SearchPodcastsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetRecommendedPodcastsUseCase(
        podcastRepository: PodcastRepository
    ): GetRecommendedPodcastsUseCase {
        return GetRecommendedPodcastsUseCase(podcastRepository)
    }

    @Provides
    @Singleton
    fun provideSearchPodcastsUseCase(
        podcastRepository: PodcastRepository
    ): SearchPodcastsUseCase {
        return SearchPodcastsUseCase(podcastRepository)
    }

    @Provides
    @Singleton
    fun provideSearchEpisodesUseCase(
        podcastRepository: PodcastRepository
    ): SearchEpisodesUseCase {
        return SearchEpisodesUseCase(podcastRepository)
    }

    @Provides
    @Singleton
    fun provideGetPodcastDetailUseCase(
        podcastRepository: PodcastRepository
    ): GetPodcastDetailUseCase {
        return GetPodcastDetailUseCase(podcastRepository)
    }

    @Provides
    @Singleton
    fun provideGetPodcastEpisodesUseCase(
        podcastRepository: PodcastRepository
    ): GetPodcastEpisodesUseCase {
        return GetPodcastEpisodesUseCase(podcastRepository)
    }

    @Provides
    @Singleton
    fun provideGetAllPlayHistoryUseCase(
        playHistoryRepository: PlayHistoryRepository
    ): GetAllPlayHistoryUseCase {
        return GetAllPlayHistoryUseCase(playHistoryRepository)
    }

    @Provides
    @Singleton
    fun provideSearchPlayHistoryUseCase(
        playHistoryRepository: PlayHistoryRepository
    ): SearchPlayHistoryUseCase {
        return SearchPlayHistoryUseCase(playHistoryRepository)
    }

    @Provides
    @Singleton
    fun provideClearPlayHistoryUseCase(
        playHistoryRepository: PlayHistoryRepository
    ): ClearPlayHistoryUseCase {
        return ClearPlayHistoryUseCase(playHistoryRepository)
    }

    @Provides
    @Singleton
    fun provideGetAllFavoritesUseCase(
        favoriteRepository: FavoriteRepository
    ): GetAllFavoritesUseCase {
        return GetAllFavoritesUseCase(favoriteRepository)
    }

    @Provides
    @Singleton
    fun provideSearchFavoritesUseCase(
        favoriteRepository: FavoriteRepository
    ): SearchFavoritesUseCase {
        return SearchFavoritesUseCase(favoriteRepository)
    }

    @Provides
    @Singleton
    fun provideAddToFavoritesUseCase(
        favoriteRepository: FavoriteRepository
    ): AddToFavoritesUseCase {
        return AddToFavoritesUseCase(favoriteRepository)
    }

    @Provides
    @Singleton
    fun provideRemoveFromFavoritesUseCase(
        favoriteRepository: FavoriteRepository
    ): RemoveFromFavoritesUseCase {
        return RemoveFromFavoritesUseCase(favoriteRepository)
    }
}