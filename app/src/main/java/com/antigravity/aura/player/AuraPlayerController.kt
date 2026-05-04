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
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Fix #2: The MediaController is now exposed via a suspend function [awaitController]
 * that correctly waits for the async build to complete. All callers (PlayerViewModel)
 * use this instead of the fragile pendingUrl pattern.
 */
@Singleton
class AuraPlayerController @Inject constructor(@ApplicationContext private val context: Context) {

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    // Deferred controller — resolved once asynchronously
    private var controllerDeferred: kotlinx.coroutines.CompletableDeferred<MediaController> =
        kotlinx.coroutines.CompletableDeferred()

    init {
        val sessionToken = SessionToken(context, ComponentName(context, AuraMediaService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        future.addListener({
            val controller = future.get()
            controller.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }
            })
            controllerDeferred.complete(controller)
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Suspends until the MediaController is ready, then returns it.
     * Never races — coroutine callers simply await this.
     */
    suspend fun awaitController(): MediaController = controllerDeferred.await()

    /**
     * Play a URI (including youtube:// scheme URIs) once the controller is ready.
     * Must be called from a coroutine.
     */
    suspend fun playUri(uri: String) {
        val controller = awaitController()
        val mediaItem = MediaItem.fromUri(uri)
        controller.setMediaItem(mediaItem)
        controller.prepare()
        controller.play()
    }

    suspend fun skipToNext() {
        val controller = awaitController()
        if (controller.hasNextMediaItem()) controller.seekToNextMediaItem()
    }

    suspend fun skipToPrevious() {
        val controller = awaitController()
        if (controller.hasPreviousMediaItem()) controller.seekToPreviousMediaItem()
    }

    suspend fun seekTo(positionMs: Long) {
        awaitController().seekTo(positionMs)
    }

    suspend fun togglePlayPause() {
        val controller = awaitController()
        if (controller.isPlaying) controller.pause() else controller.play()
    }

    suspend fun getCurrentPosition(): Long = awaitController().currentPosition
    suspend fun getDuration(): Long = awaitController().duration
}
