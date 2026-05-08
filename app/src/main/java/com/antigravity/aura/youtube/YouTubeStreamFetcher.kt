package com.antigravity.aura.youtube

import com.antigravity.aura.network.YouTubeDataApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YouTubeStreamFetcher @Inject constructor() {
    
    companion object {
        fun init() {
            NewPipe.init(AuraDownloader())
        }
    }
    
    suspend fun getStreamUrl(videoId: String): String? = withContext(Dispatchers.IO) {
        // Try NewPipe first
        val newPipeUrl = tryNewPipe(videoId)
        if (newPipeUrl != null) return@withContext newPipeUrl
        
        // If NewPipe fails, try a public Piped API as a fallback (the "backend approach")
        return@withContext tryPipedApi(videoId)
    }
    
    private suspend fun tryNewPipe(videoId: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = "https://www.youtube.com/watch?v=$videoId"
            val extractor = ServiceList.YouTube.getStreamExtractor(url) as YoutubeStreamExtractor
            extractor.fetchPage()
            
            val audioStreams = extractor.audioStreams
            if (audioStreams.isNotEmpty()) {
                // Return the highest bitrate audio stream
                return@withContext audioStreams.maxByOrNull { it.averageBitrate }?.content
            }
        } catch (e: Exception) {
            println("NewPipe failed for $videoId: ${e.message}")
        }
        return@withContext null
    }
    
    /**
     * Fallback to a Piped instance API if NewPipe fails.
     * This is a "backend approach" that is often more stable.
     */
    private suspend fun tryPipedApi(videoId: String): String? = withContext(Dispatchers.IO) {
        val instances = listOf(
            "https://pipedapi.kavin.rocks",
            "https://pipedapi.ducks.party",
            "https://api-piped.mha.fi"
        )
        
        for (baseUrl in instances) {
            try {
                val apiUrl = "$baseUrl/streams/$videoId"
                val connection = URL(apiUrl).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                
                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    // Simple regex to find the first M4A or WebM audio stream in the Piped response
                    // Piped returns a JSON with "audioStreams" array
                    val m4aUrlPattern = Regex("\"url\":\"(https://[^\"]+)\",\"format\":\"M4A\"")
                    val match = m4aUrlPattern.find(response)
                    if (match != null) {
                        return@withContext match.groupValues[1].replace("\\/", "/")
                    }
                }
            } catch (e: Exception) {
                println("Piped fallback failed for $baseUrl: ${e.message}")
            }
        }
        return@withContext null
    }
}
