package com.antigravity.aura.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.antigravity.aura.ui.screens.*
import com.antigravity.aura.ui.theme.VermillionRed
import com.antigravity.aura.ui.viewmodels.PlayerViewModel

private val PauseIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Pause",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(6f, 19f)
            lineTo(10f, 19f)
            lineTo(10f, 5f)
            lineTo(6f, 5f)
            close()
            moveTo(14f, 5f)
            lineTo(14f, 19f)
            lineTo(18f, 19f)
            lineTo(18f, 5f)
            close()
        }
    }.build()


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
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentTrack!!.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                Text(
                                    text = currentTrack!!.artist,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                            IconButton(onClick = { playerViewModel.playerController.togglePlayPause() }) {
                                androidx.compose.material3.Icon(
                                    imageVector = if (isPlaying) PauseIcon else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
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
                            icon = { androidx.compose.material3.Icon(icon, contentDescription = route) },
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
                HomeScreen(
                    onNavigateToSettings = { navController.navigate("api_key_manager") }
                )
            }
            composable("now_playing") {
                if (currentTrack != null) {
                    val isLiked by playerViewModel.isLiked.collectAsState()
                    NowPlayingScreen(
                        controller = playerViewModel.playerController,
                        trackTitle = currentTrack!!.title,
                        artistName = currentTrack!!.artist,
                        isLiked = isLiked,
                        onToggleLike = { playerViewModel.toggleLikeCurrentTrack() }
                    )
                }
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
                    onNavigateToImport = { navController.navigate("import") },
                    onNavigateToLikedSongs = { navController.navigate("liked_songs") }
                )
            }
            composable("liked_songs") {
                LikedSongsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onPlayTracks = { tracks, index ->
                        playerViewModel.playPlaylist(tracks, index)
                    }
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
                    onPlayPlaylist = { tracks, index ->
                        playerViewModel.playPlaylist(tracks, index)
                    }
                )
            }
            composable("api_key_manager") {
                ApiKeyManagerScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
