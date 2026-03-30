package com.antigravity.aura.youtube

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.search.SearchExtractor
import com.antigravity.aura.ui.viewmodels.TrackSearchResult

object YouTubeSearcher {
    
    suspend fun search(query: String): List<TrackSearchResult> = withContext(Dispatchers.IO) {
        try {
            val searchExtractor = ServiceList.YouTube.getSearchExtractor(query)
            searchExtractor.fetchPage()
            
            val items = searchExtractor.initialPage.items
            return@withContext items.filterIsInstance<StreamInfoItem>().map { item ->
                TrackSearchResult(
                    videoId = item.url.substringAfter("v="),
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
                    // Item URL looks like https://www.youtube.com/watch?v=VIDEO_ID
                    return@withContext item.url.substringAfter("v=")
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
