package com.musicwave

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.musicwave.data.model.*
import com.musicwave.ui.component.MiniPlayer
import com.musicwave.ui.screen.*
import com.musicwave.ui.theme.MusicWaveTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicWaveTheme {
                MusicWaveAppContent()
            }
        }
    }
}

@Composable
fun MusicWaveAppContent() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf("home", "search", "library")

    var currentSong by remember { mutableStateOf<SongItem?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var shuffleMode by remember { mutableStateOf(false) }
    var repeatMode by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentRoute == "home",
                        onClick = {
                            navController.navigate("home") {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                        label = { Text("Search") },
                        selected = currentRoute == "search",
                        onClick = {
                            navController.navigate("search") {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.LibraryMusic, contentDescription = "Library") },
                        label = { Text("Library") },
                        selected = currentRoute == "library",
                        onClick = {
                            navController.navigate("library") {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                composable("home") {
                    HomeScreen(
                        onSongClick = { song ->
                            currentSong = song
                            isPlaying = true
                            navController.navigate("now_playing")
                        },
                        onAlbumClick = { albumId ->
                            navController.navigate("album/$albumId")
                        },
                        onArtistClick = { artistId ->
                            navController.navigate("artist/$artistId")
                        },
                        onPlaylistClick = { playlistId ->
                            navController.navigate("playlist/$playlistId")
                        }
                    )
                }
                composable("search") {
                    SearchScreen(
                        onSongClick = { song ->
                            currentSong = song
                            isPlaying = true
                            navController.navigate("now_playing")
                        },
                        onAlbumClick = { albumId ->
                            navController.navigate("album/$albumId")
                        },
                        onArtistClick = { artistId ->
                            navController.navigate("artist/$artistId")
                        },
                        onPlaylistClick = { playlistId ->
                            navController.navigate("playlist/$playlistId")
                        }
                    )
                }
                composable("library") {
                    LibraryScreen(
                        onSongClick = { song ->
                            currentSong = song
                            isPlaying = true
                            navController.navigate("now_playing")
                        },
                        onAlbumClick = { albumId ->
                            navController.navigate("album/$albumId")
                        },
                        onArtistClick = { artistId ->
                            navController.navigate("artist/$artistId")
                        },
                        onPlaylistClick = { playlistId ->
                            navController.navigate("playlist/$playlistId")
                        }
                    )
                }
                composable("now_playing") {
                    NowPlayingScreen(
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        currentPosition = currentPosition,
                        duration = duration,
                        shuffleMode = shuffleMode,
                        repeatMode = repeatMode,
                        onPlayPauseClick = { isPlaying = !isPlaying },
                        onNextClick = { },
                        onPreviousClick = { },
                        onShuffleClick = { shuffleMode = !shuffleMode },
                        onRepeatClick = { repeatMode = (repeatMode + 1) % 3 },
                        onSeekTo = { newPosition -> currentPosition = newPosition },
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable(
                    "album/{albumId}",
                    arguments = listOf(navArgument("albumId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val albumId = backStackEntry.arguments?.getString("albumId") ?: return@composable
                    AlbumScreen(
                        albumId = albumId,
                        onSongClick = { song ->
                            currentSong = song
                            isPlaying = true
                            navController.navigate("now_playing")
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable(
                    "artist/{artistId}",
                    arguments = listOf(navArgument("artistId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val artistId = backStackEntry.arguments?.getString("artistId") ?: return@composable
                    ArtistScreen(
                        artistId = artistId,
                        onSongClick = { song ->
                            currentSong = song
                            isPlaying = true
                            navController.navigate("now_playing")
                        },
                        onAlbumClick = { albumId ->
                            navController.navigate("album/$albumId")
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable(
                    "playlist/{playlistId}",
                    arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val playlistId = backStackEntry.arguments?.getString("playlistId") ?: return@composable
                    PlaylistScreen(
                        playlistId = playlistId,
                        onSongClick = { song ->
                            currentSong = song
                            isPlaying = true
                            navController.navigate("now_playing")
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }

            MiniPlayer(
                currentSong = currentSong,
                isPlaying = isPlaying,
                onPlayPauseClick = { isPlaying = !isPlaying },
                onNextClick = { },
                onClick = {
                    navController.navigate("now_playing")
                }
            )
        }
    }
}

// Placeholder screens for Album, Artist, Playlist detail
@Composable
fun AlbumScreen(
    albumId: String,
    onSongClick: (SongItem) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Album") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("Album: $albumId")
        }
    }
}

@Composable
fun ArtistScreen(
    artistId: String,
    onSongClick: (SongItem) -> Unit,
    onAlbumClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Artist") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("Artist: $artistId")
        }
    }
}

@Composable
fun PlaylistScreen(
    playlistId: String,
    onSongClick: (SongItem) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Playlist") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("Playlist: $playlistId")
        }
    }
}