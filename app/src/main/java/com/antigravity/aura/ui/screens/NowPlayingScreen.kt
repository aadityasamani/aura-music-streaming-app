package com.antigravity.aura.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.antigravity.aura.player.AuraPlayerController
import com.antigravity.aura.ui.theme.VermillionRed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.media3.common.Player

@Composable
fun NowPlayingScreen(
    controller: AuraPlayerController,
    trackTitle: String,
    artistName: String,
    isLiked: Boolean,
    onToggleLike: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPlaying by controller.isPlaying.collectAsState()
    val shuffleModeEnabled by controller.shuffleModeEnabled.collectAsState()
    val repeatMode by controller.repeatMode.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Thumbnail placeholder with rounded corners
        Surface(
            modifier = Modifier.size(300.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            androidx.compose.material3.Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.padding(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trackTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = artistName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            
            IconButton(onClick = onToggleLike) {
                androidx.compose.material3.Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) VermillionRed else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Transport Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle
            IconButton(onClick = { controller.toggleShuffle() }) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (shuffleModeEnabled) VermillionRed else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Previous
            IconButton(onClick = { controller.skipToPrevious() }) {
                androidx.compose.material3.Icon(
                    Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    modifier = Modifier.size(32.dp)
                )
            }

            // Play/Pause
            Surface(
                modifier = Modifier.size(72.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = VermillionRed,
                onClick = { controller.togglePlayPause() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(40.dp),
                        tint = Color.Black
                    )
                }
            }

            // Next
            IconButton(onClick = { controller.skipToNext() }) {
                androidx.compose.material3.Icon(
                    Icons.Default.SkipNext,
                    contentDescription = "Next",
                    modifier = Modifier.size(32.dp)
                )
            }

            // Repeat
            IconButton(onClick = { controller.toggleRepeat() }) {
                val icon = when (repeatMode) {
                    Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                    else -> Icons.Default.Repeat
                }
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = "Repeat",
                    tint = if (repeatMode != Player.REPEAT_MODE_OFF) VermillionRed else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
