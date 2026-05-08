package com.antigravity.aura.youtube

import com.antigravity.aura.BuildConfig
import com.antigravity.aura.network.YouTubeDataApi
import com.antigravity.aura.ui.viewmodels.TrackSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YouTubeSearcher @Inject constructor(
    private val api: YouTubeDataApi,
    private val repository: com.antigravity.aura.data.repository.AuraRepository
) {

    private suspend fun getApiKey(): String? {
        val activeKey = repository.getActiveApiKey()
        if (activeKey != null && !activeKey.isQuotaExceeded) {
            return activeKey.key
        }
        
        // If no active key or quota exceeded, try to find any other available key
        val allKeys = repository.getAllApiKeys().firstOrNull() ?: emptyList()
        val nextKey = allKeys.firstOrNull { !it.isQuotaExceeded }
        
        if (nextKey != null) {
            repository.setActiveApiKey(nextKey.id)
            return nextKey.key
        }

        // Fallback to BuildConfig if no database keys exist
        return BuildConfig.YOUTUBE_API_KEY.ifEmpty { null }
    }

    private suspend fun handleQuotaExceeded() {
        val activeKey = repository.getActiveApiKey() ?: return
        repository.updateApiKey(activeKey.copy(isQuotaExceeded = true, isActive = false))
        
        // Try to activate the next one
        val allKeys = repository.getAllApiKeys().firstOrNull() ?: emptyList()
        val nextAvailable = allKeys.firstOrNull { !it.isQuotaExceeded && it.id != activeKey.id }
        if (nextAvailable != null) {
            repository.setActiveApiKey(nextAvailable.id)
        }
    }

    suspend fun search(query: String): List<TrackSearchResult> = withContext(Dispatchers.IO) {
        val key = getApiKey() ?: return@withContext emptyList()
        try {
            val response = api.search(query = "$query official audio", apiKey = key)
            
            return@withContext response.items.mapNotNull { item ->
                val videoId = item.id.videoId ?: return@mapNotNull null
                TrackSearchResult(
                    videoId = videoId,
                    title = item.snippet.title,
                    artist = item.snippet.channelTitle
                )
            }
        } catch (e: retrofit2.HttpException) {
            if (e.code() == 403) {
                handleQuotaExceeded()
                // Recursive retry with next key (be careful with depth, but here it's limited by number of keys)
                return@withContext search(query)
            }
            e.printStackTrace()
            return@withContext emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }

    suspend fun getBestMatch(title: String, artist: String): String? = withContext(Dispatchers.IO) {
        val key = getApiKey() ?: return@withContext null
        try {
            val query = "$title $artist full audio song"
            val response = api.search(query = query, maxResults = 5, apiKey = key)
            
            if (response.items.isEmpty()) return@withContext null

            var bestVideoId: String? = null
            var bestScore = -999

            for (item in response.items) {
                val videoId = item.id.videoId ?: continue
                val score = scoreResult(item.snippet.title)
                if (score > bestScore) {
                    bestScore = score
                    bestVideoId = videoId
                }
            }

            return@withContext bestVideoId
        } catch (e: retrofit2.HttpException) {
            if (e.code() == 403) {
                handleQuotaExceeded()
                return@withContext getBestMatch(title, artist)
            }
            e.printStackTrace()
            return@withContext null
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    private fun scoreResult(title: String): Int {
        val t = title.lowercase()
        var score = 0

        // Highest priority — explicit full audio indicators
        if (t.contains("full audio"))               score += 10
        if (t.contains("audio song"))               score += 10
        if (t.contains("official audio"))           score += 9
        if (t.contains("audio only"))               score += 9

        // Good — official channel signals
        if (t.contains("official"))                 score += 3
        if (t.contains("original"))                 score += 2

        // Acceptable fallback — lyric videos (usually clean audio, full length)
        if (t.contains("lyric video"))              score += 5
        if (t.contains("lyrics video"))             score += 5
        if (t.contains("lyrical"))                  score += 4
        if (t.contains("lyrics"))                   score += 3

        // Neutral
        if (t.contains("song"))                     score += 1

        // Penalise — likely video songs with dialogue, trimmed, or wrong version
        if (t.contains("video song"))               score -= 3
        if (t.contains("full video"))               score -= 3
        if (t.contains("making"))                   score -= 4
        if (t.contains("behind the scene"))         score -= 5
        if (t.contains("trailer"))                  score -= 8
        if (t.contains("teaser"))                   score -= 8
        if (t.contains("cover"))                    score -= 5
        if (t.contains("remix"))                    score -= 4
        if (t.contains("mashup"))                   score -= 5
        if (t.contains("live"))                     score -= 3
        if (t.contains("unplugged"))                score -= 2
        if (t.contains("karaoke"))                  score -= 6
        if (t.contains("reaction"))                 score -= 8
        if (t.contains("dance"))                    score -= 2

        return score
    }
}
