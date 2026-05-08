package com.antigravity.aura.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.antigravity.aura.data.entity.TrackEntity
import com.antigravity.aura.data.repository.AuraRepository
import com.antigravity.aura.ui.theme.VermillionRed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class LikedSongsViewModel @Inject constructor(
    repository: AuraRepository
) : ViewModel() {
    val likedTracks: Flow<List<TrackEntity>> = repository.getLikedTracks()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LikedSongsScreen(
    onNavigateBack: () -> Unit,
    onPlayTracks: (List<TrackEntity>, Int) -> Unit,
    viewModel: LikedSongsViewModel = hiltViewModel()
) {
    val tracks by viewModel.likedTracks.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Liked Songs", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        androidx.compose.material3.Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                FloatingActionButton(
                    onClick = { onPlayTracks(tracks, 0) },
                    containerColor = VermillionRed
                ) {
                    androidx.compose.material3.Icon(Icons.Default.PlayArrow, contentDescription = "Play All")
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = VermillionRed.copy(alpha = 0.2f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            items(tracks.indices.toList()) { index ->
                val track = tracks[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlayTracks(tracks, index) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = track.artist,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            if (tracks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No liked songs yet.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
