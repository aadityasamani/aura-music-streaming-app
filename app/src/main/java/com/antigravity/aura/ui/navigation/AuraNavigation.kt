package com.antigravity.aura.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.antigravity.aura.ui.screens.*

@Composable
fun AuraNavigation() {
    val navController = rememberNavController()
    
    val items = listOf(
        Pair("home", Icons.Default.Home),
        Pair("search", Icons.Default.Search),
        Pair("library", Icons.Default.List)
    )

    Scaffold(
        bottomBar = {
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
                                // Pop up to the start destination of the graph to avoid building up a large stack
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
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
                HomeScreen()
            }
            composable("search") {
                val playerViewModel = androidx.hilt.navigation.compose.hiltViewModel<com.antigravity.aura.ui.viewmodels.PlayerViewModel>()
                SearchScreen(
                    onTrackClick = { videoId ->
                        playerViewModel.playYouTubeVideo(videoId)
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
                val playerViewModel = androidx.hilt.navigation.compose.hiltViewModel<com.antigravity.aura.ui.viewmodels.PlayerViewModel>()
                PlaylistDetailScreen(
                    playlistId = id,
                    onNavigateBack = { navController.popBackStack() },
                    onTrackClick = { track -> 
                        track.youtubeVideoId?.let { playerViewModel.playYouTubeVideo(it) }
                    }
                )
            }
        }
    }
}
