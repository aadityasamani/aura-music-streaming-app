package com.antigravity.aura.player

import android.net.Uri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.TransferListener
import com.antigravity.aura.youtube.YouTubeStreamFetcher
import kotlinx.coroutines.runBlocking

/**
 * A DataSource.Factory that intercepts youtube:// URIs (carrying a video ID),
 * resolves the real audio stream URL via NewPipe at open()-time (not at enqueue-time),
 * and then delegates to a normal HTTP DataSource.
 *
 * This prevents stale stream URLs — the URL is fetched fresh right before each play (Fix #1).
 */
@OptIn(UnstableApi::class)
class NewPipeDataSourceFactory : DataSource.Factory {

    override fun createDataSource(): DataSource = NewPipeDataSource()

    @OptIn(UnstableApi::class)
    private inner class NewPipeDataSource : DataSource {
        private var delegate: DataSource? = null

        override fun open(dataSpec: DataSpec): Long {
            val uri = dataSpec.uri
            val resolvedUri = if (uri.scheme == "youtube") {
                // URI is "youtube://VIDEO_ID" — host contains the video ID
                val videoId = uri.host
                    ?: uri.schemeSpecificPart.removePrefix("//").trimStart('/')
                // Fetch a fresh stream URL right before playback — never stale
                val streamUrl = runBlocking { YouTubeStreamFetcher.getStreamUrl(videoId) }
                    ?: throw java.io.IOException("Failed to resolve stream for video ID: $videoId")
                Uri.parse(streamUrl)
            } else {
                uri
            }

            val resolvedSpec = dataSpec.buildUpon().setUri(resolvedUri).build()
            val http = DefaultHttpDataSource.Factory().createDataSource()
            delegate = http
            return http.open(resolvedSpec)
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            return delegate?.read(buffer, offset, length)
                ?: throw java.io.IOException("DataSource not opened")
        }

        override fun getUri(): Uri? = delegate?.uri

        override fun close() {
            delegate?.close()
            delegate = null
        }

        override fun addTransferListener(transferListener: TransferListener) {
            // no-op — not needed for stream playback
        }
    }
}
