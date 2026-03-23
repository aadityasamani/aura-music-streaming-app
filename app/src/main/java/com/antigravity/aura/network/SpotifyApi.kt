package com.antigravity.aura.network

import retrofit2.http.*

interface SpotifyAuthApi {
    @FormUrlEncoded
    @POST("api/token")
    suspend fun getAccessToken(
        @Header("Authorization") authHeader: String,
        @Field("grant_type") grantType: String = "client_credentials"
    ): SpotifyTokenResponse
}

interface SpotifyApi {
    @GET("v1/playlists/{playlist_id}")
    suspend fun getPlaylist(
        @Header("Authorization") authHeader: String,
        @Path("playlist_id") playlistId: String
    ): SpotifyPlaylistResponse
    
    @GET
    suspend fun getPlaylistTracksNextPage(
        @Header("Authorization") authHeader: String,
        @Url url: String
    ): SpotifyTracksPager
}
