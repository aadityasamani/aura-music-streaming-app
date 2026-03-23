package com.antigravity.aura.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.aura.data.entity.PlaylistEntity
import com.antigravity.aura.data.entity.TrackEntity
import com.antigravity.aura.data.repository.AuraRepository
import com.antigravity.aura.network.SpotifyRepository
import com.antigravity.aura.youtube.YouTubeSearcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val spotifyRepository: SpotifyRepository,
    private val auraRepository: AuraRepository
) : ViewModel() {

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    fun importSpotifyPlaylist(url: String) {
        viewModelScope.launch {
            try {
                _importState.value = ImportState.Loading("Extracting Playlist ID...")
                
                // Extract playlist ID from URL
                val playlistId = extractPlaylistId(url)
                if (playlistId == null) {
                    _importState.value = ImportState.Error("Invalid Spotify Playlist URL")
                    return@launch
                }
                
                _importState.value = ImportState.Loading("Fetching tracks from Spotify...")
                val response = spotifyRepository.getPlaylistAndTracks(playlistId)
                
                val dbPlaylistId = UUID.randomUUID().toString()
                val playlistEntity = PlaylistEntity(
                    id = dbPlaylistId,
                    name = response.name,
                    coverUrl = response.images?.firstOrNull()?.url
                )
                auraRepository.createPlaylist(playlistEntity)
                
                val tracks = response.tracks.items
                val totalTracks = tracks.size
                
                for ((index, item) in tracks.withIndex()) {
                    val track = item.track
                    val artistName = track.artists.firstOrNull()?.name ?: "Unknown Artist"
                    
                    _importState.value = ImportState.Loading(
                        "Mapping ${index + 1}/$totalTracks: ${track.name} by $artistName"
                    )
                    
                    val youtubeVideoId = YouTubeSearcher.getBestMatch(track.name, artistName)
                    
                    val trackEntity = TrackEntity(
                        id = track.id,
                        title = track.name,
                        artist = artistName,
                        albumArtUrl = track.album.images?.firstOrNull()?.url,
                        youtubeVideoId = youtubeVideoId,
                        durationMs = track.durationMs
                    )
                    
                    auraRepository.addTrackToPlaylist(trackEntity, dbPlaylistId, index)
                }
                
                _importState.value = ImportState.Success("Successfully imported $totalTracks tracks.")
            } catch (e: Exception) {
                e.printStackTrace()
                _importState.value = ImportState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    private fun extractPlaylistId(url: String): String? {
        val regex = Regex("playlist/([a-zA-Z0-9]+)")
        val match = regex.find(url)
        return match?.groupValues?.get(1)
    }

    sealed class ImportState {
        object Idle : ImportState()
        data class Loading(val message: String) : ImportState()
        data class Success(val message: String) : ImportState()
        data class Error(val message: String) : ImportState()
    }
}
