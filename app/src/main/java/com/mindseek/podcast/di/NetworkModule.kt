package com.mindseek.podcast.di

import com.mindseek.podcast.data.remote.ApiServiceWrapper
import com.mindseek.podcast.data.remote.api.NioRadioApi
import com.mindseek.podcast.data.remote.api.PodcastApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val NIO_BASE_URL = "https://gateway-front-external.nio.com/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(NIO_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideNioRadioApi(retrofit: Retrofit): NioRadioApi {
        return retrofit.create(NioRadioApi::class.java)
    }

    @Provides
    @Singleton
    fun providePodcastApiService(retrofit: Retrofit): PodcastApiService {
        return retrofit.create(PodcastApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideApiServiceWrapper(
        podcastApiService: PodcastApiService
    ): ApiServiceWrapper {
        return ApiServiceWrapper(podcastApiService)
    }
}