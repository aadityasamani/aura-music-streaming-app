package com.antigravity.aura.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.aura.player.AuraPlayerController
import com.antigravity.aura.youtube.YouTubeStreamFetcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    val playerController: AuraPlayerController
) : ViewModel() {

    data class TrackInfo(val title: String, val artist: String)

    private val _currentTrack = MutableStateFlow<TrackInfo?>(null)
    val currentTrack: StateFlow<TrackInfo?> = _currentTrack.asStateFlow()

    private fun normalizeVideoId(videoIdOrUrl: String): String? {
        val trimmed = videoIdOrUrl.trim()
        if (trimmed.isBlank()) return null

        val plainIdRegex = Regex("^[A-Za-z0-9_-]{11}$")
        if (plainIdRegex.matches(trimmed)) return trimmed

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

    fun playYouTubeVideo(videoId: String, title: String = "Unknown", artist: String = "Unknown") {
        val normalizedVideoId = normalizeVideoId(videoId)
        if (normalizedVideoId == null) {
            println("Invalid YouTube video id/url: $videoId")
            _currentTrack.value = null
            return
        }

        viewModelScope.launch {
            try {
                val url = withContext(Dispatchers.IO) {
                    YouTubeStreamFetcher.getStreamUrl(normalizedVideoId)
                }
                if (url != null) {
                    _currentTrack.value = TrackInfo(title, artist)
                    withContext(Dispatchers.Main) {
                        playerController.playStream(url)
                    }
                } else {
                    println("Failed to fetch audio stream URL for $normalizedVideoId")
                    _currentTrack.value = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _currentTrack.value = null
            }
        }
    }
}
