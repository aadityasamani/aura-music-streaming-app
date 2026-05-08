package com.antigravity.aura.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.antigravity.aura.data.entity.PlaylistEntity
import com.antigravity.aura.data.entity.PlaylistTrackCrossRef
import com.antigravity.aura.data.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuraDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTrackCrossRef(crossRef: PlaylistTrackCrossRef)

    @Query("SELECT * FROM playlists")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT playlists.* FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: String): PlaylistEntity?

    @Query("""
        SELECT tracks.* FROM tracks 
        INNER JOIN playlist_tracks ON tracks.id = playlist_tracks.trackId 
        WHERE playlist_tracks.playlistId = :playlistId 
        ORDER BY playlist_tracks.position ASC
    """)
    fun getTracksForPlaylist(playlistId: String): Flow<List<TrackEntity>>
    
    @Query("SELECT * FROM tracks")
    fun getAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :trackId")
    suspend fun getTrackById(trackId: String): TrackEntity?
    
    @Query("UPDATE tracks SET youtubeVideoId = :videoId WHERE id = :trackId")
    suspend fun updateTrackYoutubeVideoId(trackId: String, videoId: String)

    @Query("UPDATE tracks SET isLiked = :isLiked WHERE id = :trackId")
    suspend fun updateTrackLikedStatus(trackId: String, isLiked: Boolean)

    @Query("SELECT * FROM tracks WHERE isLiked = 1")
    fun getLikedTracks(): Flow<List<TrackEntity>>
}
