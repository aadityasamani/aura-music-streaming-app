package com.antigravity.aura.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.antigravity.aura.data.entity.PlaylistEntity
import com.antigravity.aura.data.repository.AuraRepository
import com.antigravity.aura.ui.theme.VermillionRed
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    repository: AuraRepository
) : ViewModel() {
    val playlists: Flow<List<PlaylistEntity>> = repository.getAllPlaylists()
    val likedTracksCount: Flow<Int> = repository.getLikedTracks().map { it.size }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onNavigateToPlaylist: (String) -> Unit,
    onNavigateToImport: () -> Unit,
    onNavigateToLikedSongs: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val playlists by viewModel.playlists.collectAsState(initial = emptyList())
    val likedCount by viewModel.likedTracksCount.collectAsState(initial = 0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Library", style = MaterialTheme.typography.headlineLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    IconButton(onClick = onNavigateToImport) {
                        androidx.compose.material3.Icon(Icons.Default.Add, contentDescription = "Import Playlist")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Liked Songs Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToLikedSongs() }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = VermillionRed
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Liked Songs",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "$likedCount songs",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(playlists) { playlist ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToPlaylist(playlist.id) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}
