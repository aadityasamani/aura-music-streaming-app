package com.antigravity.aura.youtube

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import com.antigravity.aura.ui.viewmodels.TrackSearchResult

object YouTubeSearcher {

    private fun extractVideoId(urlOrId: String): String? {
        val trimmed = urlOrId.trim()
        if (trimmed.isBlank()) return null

        // Already a plain YouTube video id
        if (Regex("^[A-Za-z0-9_-]{11}$").matches(trimmed)) return trimmed

        listOf(
            Regex("[?&]v=([A-Za-z0-9_-]{11})"),
            Regex("youtu\\.be/([A-Za-z0-9_-]{11})"),
            Regex("/shorts/([A-Za-z0-9_-]{11})")
        ).forEach { pattern ->
            pattern.find(trimmed)?.groupValues?.get(1)?.let { return it }
        }
        return null
    }

    suspend fun search(query: String): List<TrackSearchResult> = withContext(Dispatchers.IO) {
        try {
            val searchExtractor = ServiceList.YouTube.getSearchExtractor(query)
            searchExtractor.fetchPage()
            searchExtractor.initialPage.items.filterIsInstance<StreamInfoItem>().map { item ->
                val videoId = extractVideoId(item.url)
                    ?: item.url.substringAfter("v=").substringBefore("&")
                TrackSearchResult(videoId = videoId, title = item.name, artist = item.uploaderName)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // ── Fix #7: Single search, top-5 results, scored selection ───────────────

    /**
     * Score a YouTube result title to prefer clean audio versions and
     * penalise covers, remixes, live performances, etc.
     */
    private fun scoreTitle(title: String): Int {
        val lower = title.lowercase()
        var score = 0

        // Positive signals
        if ("full audio" in lower || "audio song" in lower) score += 10
        if ("official audio" in lower) score += 9
        if ("lyric video" in lower || "lyrical" in lower) score += 5

        // Negative signals
        val penaltyTerms = listOf("cover", "remix", "live", "trailer", "reaction", "video song")
        for (term in penaltyTerms) {
            if (term in lower) score -= 5
        }

        return score
    }

    /**
     * Perform a single search, fetch the top 5 stream results, score each by
     * title keywords, and return the video ID of the highest-scoring result.
     * Falls back to the first result if all scores are equal.
     */
    suspend fun getBestMatch(title: String, artist: String): String? = withContext(Dispatchers.IO) {
        try {
            val query = "$title $artist audio"
            val searchExtractor = ServiceList.YouTube.getSearchExtractor(query)
            searchExtractor.fetchPage()

            val candidates = searchExtractor.initialPage.items
                .filterIsInstance<StreamInfoItem>()
                .take(5)

            if (candidates.isEmpty()) return@withContext null

            val best = candidates.maxByOrNull { scoreTitle(it.name) } ?: candidates.first()
            extractVideoId(best.url)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
