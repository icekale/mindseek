package com.mindseek.podcast.data.remote.api

import com.mindseek.podcast.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface PodcastApiService {
    
    // Podcast discovery endpoints
    @GET("podcasts/recommended")
    suspend fun getRecommendedPodcasts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("category") category: String? = null
    ): Response<PaginatedResponse<PodcastDto>>

    @GET("podcasts/trending")
    suspend fun getTrendingPodcasts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("timeframe") timeframe: String = "week" // day, week, month
    ): Response<PaginatedResponse<PodcastDto>>

    @GET("podcasts/categories")
    suspend fun getCategories(): Response<List<String>>

    @GET("podcasts/category/{category}")
    suspend fun getPodcastsByCategory(
        @Path("category") category: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("sort") sort: String = "popular" // popular, recent, rating
    ): Response<PaginatedResponse<PodcastDto>>

    // Search endpoints
    @GET("podcasts/search")
    suspend fun searchPodcasts(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("category") category: String? = null,
        @Query("sort") sort: String = "relevance" // relevance, popular, recent
    ): Response<SearchResponse<PodcastDto>>

    @GET("episodes/search")
    suspend fun searchEpisodes(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("podcast_id") podcastId: String? = null
    ): Response<SearchResponse<EpisodeDto>>

    // Podcast details endpoints
    @GET("podcasts/{id}")
    suspend fun getPodcastById(@Path("id") id: String): Response<ApiResponse<PodcastDto>>

    @GET("podcasts/{id}/episodes")
    suspend fun getEpisodesByPodcastId(
        @Path("id") podcastId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("sort") sort: String = "recent" // recent, oldest, popular
    ): Response<PaginatedResponse<EpisodeDto>>

    @GET("episodes/{id}")
    suspend fun getEpisodeById(@Path("id") id: String): Response<ApiResponse<EpisodeDto>>

    // Subscription endpoints
    @POST("podcasts/{id}/subscribe")
    suspend fun subscribeToPodcast(@Path("id") podcastId: String): Response<ApiResponse<Unit>>

    @DELETE("podcasts/{id}/subscribe")
    suspend fun unsubscribeFromPodcast(@Path("id") podcastId: String): Response<ApiResponse<Unit>>

    @GET("user/subscriptions")
    suspend fun getUserSubscriptions(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<PaginatedResponse<PodcastDto>>

    // User activity endpoints
    @GET("user/history")
    suspend fun getPlayHistory(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<PaginatedResponse<EpisodeDto>>

    @POST("episodes/{id}/play")
    suspend fun recordPlayHistory(
        @Path("id") episodeId: String,
        @Body playData: Map<String, Any> // position, duration, etc.
    ): Response<ApiResponse<Unit>>

    @GET("user/favorites")
    suspend fun getFavoriteEpisodes(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<PaginatedResponse<EpisodeDto>>

    @POST("episodes/{id}/favorite")
    suspend fun addToFavorites(@Path("id") episodeId: String): Response<ApiResponse<Unit>>

    @DELETE("episodes/{id}/favorite")
    suspend fun removeFromFavorites(@Path("id") episodeId: String): Response<ApiResponse<Unit>>
}