package com.antigravity.aura.network

import com.google.gson.annotations.SerializedName

data class YouTubeSearchResponse(
    val items: List<YouTubeSearchItem>
)

data class YouTubeSearchItem(
    val id: YouTubeId,
    val snippet: YouTubeSnippet
)

data class YouTubeId(
    val videoId: String?
)

data class YouTubeSnippet(
    val title: String,
    val channelTitle: String,
    val thumbnails: YouTubeThumbnails
)

data class YouTubeThumbnails(
    val medium: YouTubeThumbnail?,
    val high: YouTubeThumbnail?
)

data class YouTubeThumbnail(
    val url: String
)

data class YouTubeVideoResponse(
    val items: List<YouTubeVideoItem>
)

data class YouTubeVideoItem(
    val id: String,
    val contentDetails: YouTubeContentDetails
)

data class YouTubeContentDetails(
    val duration: String
)
