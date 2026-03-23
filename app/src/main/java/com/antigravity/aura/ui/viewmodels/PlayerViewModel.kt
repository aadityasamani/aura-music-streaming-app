package com.antigravity.aura.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.aura.player.AuraPlayerController
import com.antigravity.aura.youtube.YouTubeStreamFetcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    val playerController: AuraPlayerController
) : ViewModel() {

    fun playYouTubeVideo(videoId: String) {
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
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
