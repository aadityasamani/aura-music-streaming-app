package com.antigravity.aura.network

import com.google.gson.annotations.SerializedName

data class SpotifyTokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int
)

data class SpotifyPlaylistResponse(
    val id: String,
    val name: String,
    val images: List<SpotifyImage>?,
    val tracks: SpotifyTracksPager
)

data class SpotifyTracksPager(
    val items: List<SpotifyTrackItem>,
    val next: String?
)

data class SpotifyTrackItem(
    val track: SpotifyTrack
)

data class SpotifyTrack(
    val id: String,
    val name: String,
    val artists: List<SpotifyArtist>,
    val album: SpotifyAlbum,
    @SerializedName("duration_ms") val durationMs: Long
)

data class SpotifyArtist(
    val id: String,
    val name: String
)

data class SpotifyAlbum(
    val id: String,
    val name: String,
    val images: List<SpotifyImage>?
)

data class SpotifyImage(
    val url: String,
    val height: Int?,
    val width: Int?
)
