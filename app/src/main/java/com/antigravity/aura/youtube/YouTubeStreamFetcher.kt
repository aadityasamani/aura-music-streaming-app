package com.antigravity.aura.youtube

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor

object YouTubeStreamFetcher {
    
    fun init() {
        NewPipe.init(AuraDownloader())
    }
    
    suspend fun getStreamUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = "https://www.youtube.com/watch?v=$videoId"
            val extractor = ServiceList.YouTube.getStreamExtractor(url) as YoutubeStreamExtractor
            extractor.fetchPage()
            
            // Get audio-only streams
            val audioStreams = extractor.audioStreams
            if (audioStreams.isNotEmpty()) {
                // Return the URL of the highest bitrate audio stream
                return@withContext audioStreams.maxByOrNull { it.averageBitrate }?.content
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }
}
