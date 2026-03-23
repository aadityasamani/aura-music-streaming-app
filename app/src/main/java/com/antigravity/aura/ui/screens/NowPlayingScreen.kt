package com.antigravity.aura.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.antigravity.aura.player.AuraPlayerController
import com.antigravity.aura.ui.theme.VermillionRed

@Composable
fun NowPlayingScreen(
    controller: AuraPlayerController,
    trackTitle: String,
    artistName: String,
    modifier: Modifier = Modifier
) {
    val isPlaying = controller.isPlaying.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Placeholder for Album Art / Thumbnail
        Box(
            modifier = Modifier
                .size(300.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = trackTitle,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = artistName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { controller.togglePlayPause() },
                colors = ButtonDefaults.buttonColors(containerColor = VermillionRed)
            ) {
                Text(if (isPlaying.value) "Pause" else "Play")
            }
        }
    }
}
