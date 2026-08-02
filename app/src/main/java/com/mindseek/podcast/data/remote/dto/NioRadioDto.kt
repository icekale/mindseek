package com.mindseek.podcast.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Nio Radio API 响应格式
 * POST /moat/100914/v2/audio/list
 */
data class NioRadioResponse(
    @SerializedName("requestId") val requestId: String?,
    @SerializedName("serverTime") val serverTime: String?,
    @SerializedName("result") val result: NioRadioResult
)

data class NioRadioResult(
    @SerializedName("totalCount") val totalCount: Int,
    @SerializedName("haveNext") val haveNext: Int,
    @SerializedName("nextPage") val nextPage: Int,
    @SerializedName("dataList") val dataList: List<NioEpisodeDto>,
    @SerializedName("album") val album: NioAlbumInfo?
)

data class NioEpisodeDto(
    @SerializedName("audioId") val audioId: Long,
    @SerializedName("audioName") val audioName: String,
    @SerializedName("albumId") val albumId: Long,
    @SerializedName("albumName") val albumName: String,
    @SerializedName("albumPic") val albumPic: String?,
    @SerializedName("albumDesc") val albumDesc: String?,
    @SerializedName("singer") val singer: String?,
    @SerializedName("host") val host: List<String>?,
    @SerializedName("audioType") val audioType: Int,
    @SerializedName("duration") val duration: Long,
    @SerializedName("onlineTime") val onlineTime: Long,
    @SerializedName("aacPlayUrl192") val aacPlayUrl192: String?,
    @SerializedName("aacPlayUrl128") val aacPlayUrl128: String?,
    @SerializedName("mp3PlayUrl64") val mp3PlayUrl64: String?,
    @SerializedName("mp3PlayUrl32") val mp3PlayUrl32: String?,
    @SerializedName("aacFileSize192") val aacFileSize192: Long?,
    @SerializedName("isFavorites") val isFavorites: Boolean,
    @SerializedName("updateTime") val updateTime: Long?,
    @SerializedName("originalDuration") val originalDuration: Long?
)

data class NioAlbumInfo(
    @SerializedName("subscribed") val subscribed: Int,
    @SerializedName("subscribeCount") val subscribeCount: Int,
    @SerializedName("subscribeCountStr") val subscribeCountStr: String
)
