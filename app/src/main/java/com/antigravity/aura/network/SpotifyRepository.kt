package com.antigravity.aura.network

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpotifyRepository @Inject constructor(
    private val authApi: SpotifyAuthApi,
    private val spotifyApi: SpotifyApi
) {
    // These should ideally not be hardcoded, but for MVP we use placeholders
    private val clientId = "PLACEHOLDER_CLIENT_ID"
    private val clientSecret = "PLACEHOLDER_CLIENT_SECRET"
    
    private var accessToken: String? = null
    private var tokenExpiry: Long = 0

    private suspend fun ensureToken(): String = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        if (accessToken != null && now < tokenExpiry) {
            return@withContext accessToken!!
        }

        val authString = "$clientId:$clientSecret"
        val base64Auth = Base64.encodeToString(authString.toByteArray(), Base64.NO_WRAP)
        
        try {
            val response = authApi.getAccessToken("Basic $base64Auth")
            accessToken = response.accessToken
            tokenExpiry = now + (response.expiresIn * 1000) - 60000 // Buffer of 1 minute
            return@withContext accessToken!!
        } catch (e: Exception) {
            e.printStackTrace()
            throw IllegalStateException("Failed to authenticate with Spotify", e)
        }
    }

    suspend fun getPlaylistAndTracks(playlistId: String): SpotifyPlaylistResponse = withContext(Dispatchers.IO) {
        val token = ensureToken()
        val response = spotifyApi.getPlaylist("Bearer $token", playlistId)
        
        var currentResponse = response
        val allTrackItems = currentResponse.tracks.items.toMutableList()
        
        var nextUrl = currentResponse.tracks.next
        while (nextUrl != null) {
            try {
                val nextLogMessage = "Fetching next page from: $nextUrl"
                val pager = spotifyApi.getPlaylistTracksNextPage("Bearer $token", nextUrl)
                allTrackItems.addAll(pager.items)
                nextUrl = pager.next
            } catch (e: Exception) {
                e.printStackTrace()
                break
            }
        }
        
        return@withContext response.copy(tracks = response.tracks.copy(items = allTrackItems, next = null))
    }
}
