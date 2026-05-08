package com.antigravity.aura.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.ResolvingDataSource
import com.antigravity.aura.youtube.YouTubeStreamFetcher
import kotlinx.coroutines.runBlocking

class YouTubeResolvingDataSource(
    private val context: Context,
    private val streamFetcher: YouTubeStreamFetcher
) : ResolvingDataSource.Resolver {

    override fun resolveDataSpec(dataSpec: DataSpec): DataSpec {
        val uri = dataSpec.uri
        if (uri.scheme == "youtube") {
            val videoId = uri.host ?: return dataSpec
            
            // Resolve the stream URL synchronously (blocking)
            // ResolvingDataSource runs on a background thread, so this is safe for Media3
            val resolvedUrl = runBlocking {
                streamFetcher.getStreamUrl(videoId)
            }
            
            if (resolvedUrl != null) {
                return dataSpec.buildUpon()
                    .setUri(android.net.Uri.parse(resolvedUrl))
                    .build()
            }
        }
        return dataSpec
    }

    class Factory(
        private val context: Context,
        private val streamFetcher: YouTubeStreamFetcher
    ) : DataSource.Factory {
        override fun createDataSource(): DataSource {
            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            return ResolvingDataSource(httpDataSourceFactory.createDataSource(), YouTubeResolvingDataSource(context, streamFetcher))
        }
    }
}
