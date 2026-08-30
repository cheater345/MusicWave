package com.musicwave.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicwave.data.model.*
import com.musicwave.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LibraryUiState>(LibraryUiState.Loading)
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadLibrary()
    }

    fun loadLibrary() {
        viewModelScope.launch {
            _uiState.value = LibraryUiState.Loading
            libraryRepository.getLibrary()
                .onSuccess { page ->
                    _uiState.value = LibraryUiState.Success(page)
                }
                .onFailure { error ->
                    _uiState.value = LibraryUiState.Error(error.message ?: "Failed to load library")
                }
        }
    }
}

sealed class LibraryUiState {
    data object Loading : LibraryUiState()
    data class Success(val items: List<YTItem>, val continuation: String? = null) : LibraryUiState() {
        constructor(page: LibraryPage) : this(items = page.items, continuation = page.continuation)
    }
    data class Error(val message: String) : LibraryUiState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onSongClick: (SongItem) -> Unit,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library") }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is LibraryUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is LibraryUiState.Success -> {
                if (state.items.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LibraryMusic,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Your library is empty",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Add songs, albums, or playlists",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = true,
                                    onClick = { },
                                    label = { Text("All") }
                                )
                                FilterChip(
                                    selected = false,
                                    onClick = { },
                                    label = { Text("Songs") }
                                )
                                FilterChip(
                                    selected = false,
                                    onClick = { },
                                    label = { Text("Albums") }
                                )
                                FilterChip(
                                    selected = false,
                                    onClick = { },
                                    label = { Text("Artists") }
                                )
                                FilterChip(
                                    selected = false,
                                    onClick = { },
                                    label = { Text("Playlists") }
                                )
                            }
                        }

                        items(state.items) { item ->
                            when (item) {
                                is SongItem -> {
                                    com.musicwave.ui.component.SongListItem(
                                        song = item,
                                        onClick = { onSongClick(item) }
                                    )
                                }
                                is AlbumItem -> {
                                    com.musicwave.ui.component.AlbumListItem(
                                        album = item,
                                        onClick = { onAlbumClick(item.browseId) }
                                    )
                                }
                                is ArtistItem -> {
                                    com.musicwave.ui.component.ArtistListItem(
                                        artist = item,
                                        onClick = { onArtistClick(item.browseId) }
                                    )
                                }
                                is PlaylistItem -> {
                                    com.musicwave.ui.component.PlaylistListItem(
                                        playlist = item,
                                        onClick = { onPlaylistClick(item.playlistId) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            is LibraryUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadLibrary() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}