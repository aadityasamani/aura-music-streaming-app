package com.antigravity.aura.player

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuraPlayerController @Inject constructor(@ApplicationContext context: Context) {
    private var mediaController: MediaController? = null
    private var pendingUrl: String? = null
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentMetadata = MutableStateFlow<androidx.media3.common.MediaMetadata?>(null)
    val currentMetadata: StateFlow<androidx.media3.common.MediaMetadata?> = _currentMetadata.asStateFlow()

    private val _currentMediaId = MutableStateFlow<String?>(null)
    val currentMediaId: StateFlow<String?> = _currentMediaId.asStateFlow()

    private val _shuffleModeEnabled = MutableStateFlow(false)
    val shuffleModeEnabled: StateFlow<Boolean> = _shuffleModeEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()
    
    init {
        val sessionToken = SessionToken(context, ComponentName(context, AuraMediaService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        
        controllerFuture.addListener({
            mediaController = controllerFuture.get()
            
            pendingUrl?.let { url ->
                val mediaItem = MediaItem.fromUri(url)
                mediaController?.setMediaItem(mediaItem)
                mediaController?.prepare()
                mediaController?.play()
                pendingUrl = null
            }

            mediaController?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }

                override fun onMediaMetadataChanged(mediaMetadata: androidx.media3.common.MediaMetadata) {
                    _currentMetadata.value = mediaMetadata
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    _currentMediaId.value = mediaItem?.mediaId
                }

                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                    _shuffleModeEnabled.value = shuffleModeEnabled
                }

                override fun onRepeatModeChanged(repeatMode: Int) {
                    _repeatMode.value = repeatMode
                }
            })
        }, ContextCompat.getMainExecutor(context))
    }

    fun playYouTubeVideo(videoId: String, title: String, artist: String, mediaId: String? = null) {
        if (mediaController == null) {
            pendingUrl = "youtube://$videoId"
            return
        }

        val mediaItem = MediaItem.Builder()
            .setUri("youtube://$videoId")
            .setMediaId(mediaId ?: videoId)
            .setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .build()
            )
            .build()
            
        mediaController?.setMediaItem(mediaItem)
        mediaController?.prepare()
        mediaController?.play()
    }

    fun playPlaylist(videoIds: List<String>, titles: List<String>, artists: List<String>, mediaIds: List<String>, startIndex: Int = 0) {
        if (mediaController == null) return

        val mediaItems = videoIds.indices.map { i ->
            MediaItem.Builder()
                .setUri("youtube://${videoIds[i]}")
                .setMediaId(mediaIds[i])
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(titles[i])
                        .setArtist(artists[i])
                        .build()
                )
                .build()
        }

        mediaController?.setMediaItems(mediaItems, startIndex, 0)
        mediaController?.prepare()
        mediaController?.play()
    }

    fun togglePlayPause() {
        if (mediaController?.isPlaying == true) {
            mediaController?.pause()
        } else {
            mediaController?.play()
        }
    }

    fun toggleShuffle() {
        mediaController?.shuffleModeEnabled = !(mediaController?.shuffleModeEnabled ?: false)
    }

    fun toggleRepeat() {
        val nextMode = when (mediaController?.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        mediaController?.repeatMode = nextMode
    }

    fun skipToNext() {
        mediaController?.seekToNext()
    }

    fun skipToPrevious() {
        mediaController?.seekToPrevious()
    }
}
