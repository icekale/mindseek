package com.mindseek.podcast.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object for Podcast from API
 */
data class PodcastDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("image_url")
    val imageUrl: String,
    
    @SerializedName("author")
    val author: String,
    
    @SerializedName("category")
    val category: String,
    
    @SerializedName("episode_count")
    val episodeCount: Int = 0,
    
    @SerializedName("last_updated")
    val lastUpdated: Long,
    
    @SerializedName("rating")
    val rating: Float = 0f,
    
    @SerializedName("subscriber_count")
    val subscriberCount: Int = 0,
    
    @SerializedName("language")
    val language: String = "zh-CN",
    
    @SerializedName("website_url")
    val websiteUrl: String? = null,
    
    @SerializedName("rss_url")
    val rssUrl: String? = null
)