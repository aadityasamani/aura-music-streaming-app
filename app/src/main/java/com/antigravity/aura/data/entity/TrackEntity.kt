package com.antigravity.aura.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String, // Spotify ID or generated ID
    val title: String,
    val artist: String,
    val albumArtUrl: String?,
    val youtubeVideoId: String?, // Can be null until matched
    val durationMs: Long,
    val isLiked: Boolean = false
)
