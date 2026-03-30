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

    fun playYouTubeVideo(videoId: String, title: String = "Unknown", artist: String = "Unknown") {
        _currentTrack.value = TrackInfo(title, artist)
        viewModelScope.launch {
            try {
                val url = withContext(Dispatchers.IO) {
                    YouTubeStreamFetcher.getStreamUrl(videoId)
                }
                if (url != null) {
                    // Start playback on main thread
                    withContext(Dispatchers.Main) {
                        playerController.playStream(url)
                    }
                } else {
                    println("Failed to fetch audio stream URL for $videoId")
                    _currentTrack.value = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _currentTrack.value = null
            }
        }
    }
}
