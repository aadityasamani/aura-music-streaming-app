package com.antigravity.aura.network

import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeDataApi {
    @GET("search")
    suspend fun search(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("maxResults") maxResults: Int = 20,
        @Query("key") apiKey: String
    ): YouTubeSearchResponse

    @GET("videos")
    suspend fun getVideoDetails(
        @Query("part") part: String = "contentDetails",
        @Query("id") ids: String,
        @Query("key") apiKey: String
    ): YouTubeVideoResponse
}
