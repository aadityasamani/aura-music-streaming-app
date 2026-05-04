package com.antigravity.aura.ui.viewmodels

// TrackSearchResult is defined in SearchViewModel.kt (same package — no import needed)

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.aura.data.entity.TrackEntity
import com.antigravity.aura.data.repository.AuraRepository
import com.antigravity.aura.player.AuraPlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class PlayerViewModel @Inject constructor(
    val playerController: AuraPlayerController,
    private val auraRepository: AuraRepository
) : ViewModel() {

    data class TrackInfo(
        val title: String,
        val artist: String,
        val albumArtUrl: String? = null,
        val youtubeVideoId: String? = null
    )

    private val _currentTrack = MutableStateFlow<TrackInfo?>(null)
    val currentTrack: StateFlow<TrackInfo?> = _currentTrack.asStateFlow()

    // ── Playlist / queue state ────────────────────────────────────────────────
    private var queue: List<TrackEntity> = emptyList()      // current shuffled/ordered queue
    private var queueIndex: Int = -1                        // pointer into queue

    // ── Fix #8: Fisher-Yates deterministic shuffle ────────────────────────────
    private fun fisherYatesShuffle(tracks: List<TrackEntity>): List<TrackEntity> {
        val list = tracks.toMutableList()
        for (i in list.lastIndex downTo 1) {
            val j = (0..i).random()
            val tmp = list[i]; list[i] = list[j]; list[j] = tmp
        }
        return list
    }

    /**
     * Load a playlist into the queue in normal order and start from [startIndex].
     */
    fun playPlaylist(tracks: List<TrackEntity>, startIndex: Int = 0) {
        queue = tracks
        queueIndex = startIndex.coerceIn(0, tracks.lastIndex)
        playCurrentQueueItem()
    }

    /**
     * Load a playlist shuffled (Fix #3 + #8): build a Fisher-Yates shuffled
     * sequence upfront and walk through it sequentially — no repeats per cycle.
     */
    fun shufflePlaylist(tracks: List<TrackEntity>) {
        queue = fisherYatesShuffle(tracks)
        queueIndex = 0
        playCurrentQueueItem()
    }

    fun skipToNext() {
        if (queue.isEmpty()) return
        queueIndex++
        if (queueIndex >= queue.size) {
            // Re-shuffle for the next round (Fix #8: every track plays once per cycle)
            queue = fisherYatesShuffle(queue)
            queueIndex = 0
        }
        playCurrentQueueItem()
    }

    fun skipToPrevious() {
        if (queue.isEmpty()) return
        queueIndex = (queueIndex - 1).coerceAtLeast(0)
        playCurrentQueueItem()
    }

    private fun playCurrentQueueItem() {
        val track = queue.getOrNull(queueIndex) ?: return
        if (track.youtubeVideoId == null) return
        playTrack(track)
    }

    /**
     * Play a single TrackEntity. The youtube:// URI is passed directly to ExoPlayer;
     * NewPipeDataSourceFactory resolves it fresh at open()-time (Fix #1 — no stale URLs).
     */
    fun playTrack(track: TrackEntity) {
        val videoId = normalizeVideoId(track.youtubeVideoId ?: "") ?: return
        _currentTrack.value = TrackInfo(
            title = track.title,
            artist = track.artist,
            albumArtUrl = track.albumArtUrl,
            youtubeVideoId = videoId
        )
        viewModelScope.launch {
            // youtube:// URI — resolved by NewPipeDataSourceFactory right before play
            playerController.playUri("youtube://$videoId")
        }
        // Record play in recently-played
        recordRecentlyPlayed(track)
    }

    /**
     * Direct YouTube video ID or URL play (used from SearchScreen).
     */
    fun playYouTubeVideo(videoId: String, title: String = "Unknown", artist: String = "Unknown", albumArtUrl: String? = null) {
        val normalized = normalizeVideoId(videoId) ?: run {
            println("Invalid YouTube video id/url: $videoId")
            return
        }
        _currentTrack.value = TrackInfo(title = title, artist = artist, albumArtUrl = albumArtUrl, youtubeVideoId = normalized)
        viewModelScope.launch {
            playerController.playUri("youtube://$normalized")
        }
    }

    // ── Recently Played (Fix #5) ──────────────────────────────────────────────
    private val _recentlyPlayed = MutableStateFlow<List<TrackEntity>>(emptyList())
    val recentlyPlayed: StateFlow<List<TrackEntity>> = _recentlyPlayed.asStateFlow()

    private fun recordRecentlyPlayed(track: TrackEntity) {
        val current = _recentlyPlayed.value.toMutableList()
        current.removeAll { it.id == track.id }
        current.add(0, track)
        if (current.size > 5) current.removeAt(current.lastIndex)
        _recentlyPlayed.value = current
    }

    // ── Non-suspend click-handler wrappers ────────────────────────────────────

    /** Toggle play/pause — safe to call from any Compose click lambda. */
    fun togglePlayPause() {
        viewModelScope.launch { playerController.togglePlayPause() }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private fun normalizeVideoId(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return null
        if (Regex("^[A-Za-z0-9_-]{11}$").matches(trimmed)) return trimmed
        listOf(
            Regex("[?&]v=([A-Za-z0-9_-]{11})"),
            Regex("youtu\\.be/([A-Za-z0-9_-]{11})"),
            Regex("/shorts/([A-Za-z0-9_-]{11})")
        ).forEach { pat ->
            pat.find(trimmed)?.groupValues?.get(1)?.let { return it }
        }
        return null
    }
}
