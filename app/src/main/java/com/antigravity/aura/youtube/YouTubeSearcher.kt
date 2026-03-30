package com.antigravity.aura.youtube

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.search.SearchExtractor
import com.antigravity.aura.ui.viewmodels.TrackSearchResult

object YouTubeSearcher {

    private fun extractVideoId(urlOrId: String): String? {
        val trimmed = urlOrId.trim()
        if (trimmed.isBlank()) return null

        // Already a plain YouTube video id.
        val plainIdRegex = Regex("^[A-Za-z0-9_-]{11}$")
        if (plainIdRegex.matches(trimmed)) return trimmed

        // Common URL formats: youtube.com/watch?v=..., youtu.be/..., shorts/...
        val patterns = listOf(
            Regex("[?&]v=([A-Za-z0-9_-]{11})"),
            Regex("youtu\\.be/([A-Za-z0-9_-]{11})"),
            Regex("/shorts/([A-Za-z0-9_-]{11})")
        )

        for (pattern in patterns) {
            val match = pattern.find(trimmed)
            if (match != null) return match.groupValues[1]
        }

        return null
    }
    
    suspend fun search(query: String): List<TrackSearchResult> = withContext(Dispatchers.IO) {
        try {
            val searchExtractor = ServiceList.YouTube.getSearchExtractor(query)
            searchExtractor.fetchPage()
            
            val items = searchExtractor.initialPage.items
            return@withContext items.filterIsInstance<StreamInfoItem>().map { item ->
                val normalizedVideoId = extractVideoId(item.url)
                    ?: item.url.substringAfter("v=").substringBefore('&')

                TrackSearchResult(
                    videoId = normalizedVideoId,
                    title = item.name,
                    artist = item.uploaderName
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }
    
    suspend fun searchVideoId(query: String): String? = withContext(Dispatchers.IO) {
        try {
            val searchExtractor = ServiceList.YouTube.getSearchExtractor(query)
            searchExtractor.fetchPage()
            
            // Get the first video result
            val items = searchExtractor.initialPage.items
            for (item in items) {
                if (item is StreamInfoItem) {
                    return@withContext extractVideoId(item.url)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }

    suspend fun getBestMatch(title: String, artist: String): String? {
        // Fallback 1: `{song_title} {artist_name} official audio`
        var videoId = searchVideoId("$title $artist official audio")
        if (videoId != null) return videoId

        // Fallback 2: `{song_title} {artist_name}`
        videoId = searchVideoId("$title $artist")
        if (videoId != null) return videoId

        // Fallback 3: `{song_title} {artist_name} lyrics`
        videoId = searchVideoId("$title $artist lyrics")
        return videoId
    }
}
