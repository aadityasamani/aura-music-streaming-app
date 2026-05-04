package com.antigravity.aura.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.antigravity.aura.data.entity.PlaylistEntity
import com.antigravity.aura.data.entity.TrackEntity
import com.antigravity.aura.data.repository.AuraRepository
import com.antigravity.aura.ui.theme.VermillionRed
import com.antigravity.aura.ui.viewmodels.PlayerViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val repository: AuraRepository
) : ViewModel() {

    private val _playlist = MutableStateFlow<PlaylistEntity?>(null)
    val playlist: StateFlow<PlaylistEntity?> = _playlist.asStateFlow()

    private val _tracks = MutableStateFlow<List<TrackEntity>>(emptyList())
    val tracks: StateFlow<List<TrackEntity>> = _tracks.asStateFlow()

    fun loadPlaylist(id: String) {
        viewModelScope.launch {
            _playlist.value = repository.getPlaylistById(id)
            repository.getTracksForPlaylist(id).collect {
                _tracks.value = it
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: String,
    onNavigateBack: () -> Unit,
    onTrackClick: (TrackEntity) -> Unit,
    viewModel: PlaylistDetailViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    androidx.compose.runtime.LaunchedEffect(playlistId) {
        viewModel.loadPlaylist(playlistId)
    }

    val playlist by viewModel.playlist.collectAsState()
    val tracks by viewModel.tracks.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(playlist?.name ?: "Loading...", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            if (tracks.isNotEmpty()) {
                // Fix #3: Actually shuffle and start playback
                FloatingActionButton(
                    onClick = { playerViewModel.shufflePlaylist(tracks) },
                    containerColor = VermillionRed
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = "Shuffle Play")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            item {
                // Fix #6 companion: show playlist cover art here too
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    val coverUrl = playlist?.coverUrl
                    if (coverUrl != null) {
                        AsyncImage(
                            model = coverUrl,
                            contentDescription = playlist?.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text("No Cover Art", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            items(tracks) { track ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTrackClick(track) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Track album art thumbnail
                    val artUrl = track.albumArtUrl
                    if (artUrl != null) {
                        AsyncImage(
                            model = artUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(MaterialTheme.shapes.small)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1
                        )
                        Text(
                            text = track.artist,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
}
