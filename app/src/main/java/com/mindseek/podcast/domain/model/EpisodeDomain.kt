package com.mindseek.podcast.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EpisodeDomain(
    val id: String,
    val podcastId: String,
    val title: String,
    val description: String,
    val audioUrl: String,
    val imageUrl: String? = null,
    val duration: Long,
    val publishDate: Long,
    val source: String = "",
    val author: String = "",
    val fileSize: Long? = null,
    val playCount: Int = 0,
    val averageRating: Float = 0f,
    val isDownloaded: Boolean = false,
    val localPath: String? = null,
    val playPosition: Long = 0L,
    val isFavorite: Boolean = false,
    val podcastTitle: String = ""
) : Parcelable