package com.mindseek.podcast.data.mapper

import com.mindseek.podcast.data.local.entity.Episode
import com.mindseek.podcast.data.local.entity.Podcast
import com.mindseek.podcast.data.remote.dto.NioEpisodeDto

/**
 * Converts a NioEpisodeDto to a local Episode entity.
 * Mapping:
 *   audioId             → id (String)
 *   albumId             → podcastId (String)
 *   audioName           → title
 *   albumDesc           → description
 *   aacPlayUrl192       → audioUrl
 *   albumPic            → imageUrl
 *   duration            → duration
 *   onlineTime          → publishDate
 *   albumName           → source
 *   host / singer       → author
 *   aacFileSize192      → fileSize
 */
fun NioEpisodeDto.toEpisode(): Episode {
    return Episode(
        id = audioId.toString(),
        podcastId = albumId.toString(),
        title = audioName,
        description = albumDesc ?: "",
        audioUrl = aacPlayUrl192 ?: mp3PlayUrl64 ?: "",
        imageUrl = albumPic,
        duration = duration,
        publishDate = onlineTime,
        source = albumName,
        author = host?.joinToString(", ") ?: singer ?: "",
        fileSize = aacFileSize192,
        isDownloaded = false,
        localPath = null
    )
}

/**
 * Converts a NioEpisodeDto to a Podcast entity for album listing.
 * The first episode from an album is used to create the podcast shell.
 */
fun NioEpisodeDto.toPodcast(): Podcast {
    return Podcast(
        id = albumId.toString(),
        title = albumName,
        description = albumDesc ?: "",
        imageUrl = albumPic ?: "",
        author = host?.joinToString(", ") ?: singer ?: "",
        category = "NioRadio",
        isSubscribed = false,
        lastUpdated = System.currentTimeMillis()
    )
}
