package com.mindseek.podcast.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object for Episode from API
 */
data class EpisodeDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("podcast_id")
    val podcastId: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("audio_url")
    val audioUrl: String,
    
    @SerializedName("duration")
    val duration: Long,
    
    @SerializedName("publish_date")
    val publishDate: Long,
    
    @SerializedName("episode_number")
    val episodeNumber: Int? = null,
    
    @SerializedName("season_number")
    val seasonNumber: Int? = null,
    
    @SerializedName("file_size")
    val fileSize: Long? = null,
    
    @SerializedName("mime_type")
    val mimeType: String = "audio/mpeg",
    
    @SerializedName("image_url")
    val imageUrl: String? = null,
    
    @SerializedName("transcript_url")
    val transcriptUrl: String? = null,
    
    @SerializedName("play_count")
    val playCount: Int = 0,
    
    @SerializedName("like_count")
    val likeCount: Int = 0
)