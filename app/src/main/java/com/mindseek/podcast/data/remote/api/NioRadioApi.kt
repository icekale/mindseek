package com.mindseek.podcast.data.remote.api

import com.mindseek.podcast.data.remote.dto.NioRadioResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Nio Radio 公开 API — 无需认证
 * Gateway: https://gateway-front-external.nio.com/moat/100914/v2/audio/list
 */
interface NioRadioApi {

    @FormUrlEncoded
    @POST("moat/100914/v2/audio/list")
    suspend fun getEpisodeList(
        @Field("albumId") albumId: Long,
        @Field("sorttype") sortType: Int = 2,     // 2 = 最新
        @Field("pagenum") pageNum: Int = 1,
        @Field("pagesize") pageSize: Int = 20
    ): NioRadioResponse
}
