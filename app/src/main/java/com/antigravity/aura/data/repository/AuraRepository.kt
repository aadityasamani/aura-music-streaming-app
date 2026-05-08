package com.antigravity.aura.data.repository

import com.antigravity.aura.data.dao.AuraDao
import com.antigravity.aura.data.entity.PlaylistEntity
import com.antigravity.aura.data.entity.PlaylistTrackCrossRef
import com.antigravity.aura.data.entity.TrackEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuraRepository @Inject constructor(
    private val auraDao: AuraDao,
    private val apiKeyDao: com.antigravity.aura.data.dao.ApiKeyDao
) {

    // API Key Management
    fun getAllApiKeys() = apiKeyDao.getAllApiKeys()
    suspend fun getActiveApiKey() = apiKeyDao.getActiveApiKey()
    suspend fun insertApiKey(apiKey: com.antigravity.aura.data.entity.ApiKeyEntity) = apiKeyDao.insertApiKey(apiKey)
    suspend fun deleteApiKey(apiKey: com.antigravity.aura.data.entity.ApiKeyEntity) = apiKeyDao.deleteApiKey(apiKey)
    suspend fun setActiveApiKey(keyId: Int) = apiKeyDao.setActiveKey(keyId)
    suspend fun updateApiKey(apiKey: com.antigravity.aura.data.entity.ApiKeyEntity) = apiKeyDao.updateApiKey(apiKey)

    suspend fun createPlaylist(playlist: PlaylistEntity) {
        auraDao.insertPlaylist(playlist)
    }

    suspend fun addTrackToPlaylist(track: TrackEntity, playlistId: String, position: Int) {
        // Insert or update track metadata
        auraDao.insertTrack(track)
        
        // Link track to playlist
        auraDao.insertPlaylistTrackCrossRef(
            PlaylistTrackCrossRef(
                playlistId = playlistId,
                trackId = track.id,
                position = position
            )
        )
    }

    fun getAllPlaylists(): Flow<List<PlaylistEntity>> = auraDao.getAllPlaylists()

    suspend fun getPlaylistById(id: String): PlaylistEntity? = auraDao.getPlaylistById(id)

    fun getTracksForPlaylist(playlistId: String): Flow<List<TrackEntity>> = 
        auraDao.getTracksForPlaylist(playlistId)
        
    suspend fun updateYoutubeVideoId(trackId: String, videoId: String) {
        auraDao.updateTrackYoutubeVideoId(trackId, videoId)
    }

    suspend fun updateTrackLikedStatus(trackId: String, isLiked: Boolean) {
        auraDao.updateTrackLikedStatus(trackId, isLiked)
    }

    fun getLikedTracks(): Flow<List<TrackEntity>> = auraDao.getLikedTracks()

    suspend fun getTrackById(id: String): TrackEntity? = auraDao.getTrackById(id)
}
