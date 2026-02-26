package com.mindseek.podcast.di

import com.mindseek.podcast.core.error.ErrorHandler
import com.mindseek.podcast.core.error.RetryManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for error handling dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object ErrorModule {
    
    @Provides
    @Singleton
    fun provideErrorHandler(): ErrorHandler {
        return ErrorHandler()
    }
    
    @Provides
    @Singleton
    fun provideRetryManager(): RetryManager {
        return RetryManager()
    }
}