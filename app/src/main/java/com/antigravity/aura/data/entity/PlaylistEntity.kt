package com.antigravity.aura.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String, // Spotify ID or generated UUID
    val name: String,
    val coverUrl: String?,
    val isLikedSongs: Boolean = false // Special flag for Liked Songs playlist
)
