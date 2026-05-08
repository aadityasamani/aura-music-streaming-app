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

import kotlinx.coroutines.flow.*
import com.antigravity.aura.data.entity.TrackEntity

@HiltViewModel
class PlayerViewModel @Inject constructor(
    val playerController: AuraPlayerController,
    private val youtubeStreamFetcher: YouTubeStreamFetcher,
    private val repository: com.antigravity.aura.data.repository.AuraRepository
) : ViewModel() {

    data class TrackInfo(val title: String, val artist: String)

    val currentTrack: StateFlow<TrackInfo?> = playerController.currentMetadata
        .map { metadata ->
            metadata?.let { TrackInfo(it.title?.toString() ?: "Unknown", it.artist?.toString() ?: "Unknown") }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val isLiked: StateFlow<Boolean> = playerController.currentMediaId
        .flatMapLatest { id ->
            if (id == null) flowOf(false)
            else repository.getLikedTracks().map { likedTracks ->
                likedTracks.any { it.id == id }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleLikeCurrentTrack() {
        val currentId = playerController.currentMediaId.value ?: return
        val currentLiked = isLiked.value
        viewModelScope.launch {
            repository.updateTrackLikedStatus(currentId, !currentLiked)
        }
    }

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
            return
        }

        playerController.playYouTubeVideo(normalizedVideoId, title, artist, mediaId = normalizedVideoId)
    }

    fun playPlaylist(tracks: List<TrackEntity>, startIndex: Int = 0) {
        val validTracks = tracks.filter { it.youtubeVideoId != null }
        if (validTracks.isEmpty()) return

        val videoIds = validTracks.map { it.youtubeVideoId!! }
        val titles = validTracks.map { it.title }
        val artists = validTracks.map { it.artist }
        val mediaIds = validTracks.map { it.id }

        playerController.playPlaylist(videoIds, titles, artists, mediaIds, startIndex)
    }
}
