package com.antigravity.aura.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.antigravity.aura.ui.screens.*
import com.antigravity.aura.ui.theme.VermillionRed
import com.antigravity.aura.ui.viewmodels.PlayerViewModel

@Composable
fun AuraNavigation() {
    val navController = rememberNavController()

    val playerViewModel: PlayerViewModel = hiltViewModel()
    val currentTrack by playerViewModel.currentTrack.collectAsState()
    val isPlaying by playerViewModel.playerController.isPlaying.collectAsState()

    val items = listOf(
        Pair("home", Icons.Default.Home),
        Pair("search", Icons.Default.Search),
        Pair("library", Icons.Default.List)
    )

    Scaffold(
        bottomBar = {
            Column {
                // Mini-player bar — tap to open NowPlayingScreen
                if (currentTrack != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("now_playing") },
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Album art thumbnail
                            val artUrl = currentTrack!!.albumArtUrl
                            if (artUrl != null) {
                                AsyncImage(
                                    model = artUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentTrack!!.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = currentTrack!!.artist,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(onClick = { playerViewModel.togglePlayPause() }) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = VermillionRed
                                )
                            }
                        }
                    }
                }

                NavigationBar {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    items.forEach { (route, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = route) },
                            label = { Text(route.replaceFirstChar { it.uppercase() }) },
                            selected = currentDestination?.hierarchy?.any { it.route == route } == true,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(playerViewModel = playerViewModel)
            }
            // Fix #4: NowPlayingScreen now manages its own state via hiltViewModel()
            composable("now_playing") {
                NowPlayingScreen(playerViewModel = playerViewModel)
            }
            composable("search") {
                SearchScreen(
                    onTrackClick = { result ->
                        playerViewModel.playYouTubeVideo(result.videoId, result.title, result.artist)
                    }
                )
            }
            composable("library") {
                LibraryScreen(
                    onNavigateToPlaylist = { id -> navController.navigate("playlist/$id") },
                    onNavigateToImport = { navController.navigate("import") }
                )
            }
            composable("import") {
                ImportScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("playlist/{playlistId}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("playlistId") ?: return@composable
                PlaylistDetailScreen(
                    playlistId = id,
                    onNavigateBack = { navController.popBackStack() },
                    // Fix #3 wiring: playTrack handles the queue so skip/next work
                    onTrackClick = { track -> playerViewModel.playTrack(track) },
                    playerViewModel = playerViewModel
                )
            }
        }
    }
}
