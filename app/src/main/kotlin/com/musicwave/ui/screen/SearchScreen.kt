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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _query.value = query
        if (query.isBlank()) {
            _uiState.value = SearchUiState.Idle
            return
        }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            search(query)
        }
    }

    private suspend fun search(query: String) {
        _uiState.value = SearchUiState.Loading
        searchRepository.search(query)
            .onSuccess { items ->
                _uiState.value = SearchUiState.Success(items)
            }
            .onFailure { error ->
                _uiState.value = SearchUiState.Error(error.message ?: "Search failed")
            }
    }

    fun loadHome() {
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            searchRepository.home()
                .onSuccess { home ->
                    _uiState.value = SearchUiState.Home(home)
                }
                .onFailure { error ->
                    _uiState.value = SearchUiState.Error(error.message ?: "Failed to load")
                }
        }
    }
}

sealed class SearchUiState {
    data object Idle : SearchUiState()
    data object Loading : SearchUiState()
    data class Success(val items: List<YTItem>) : SearchUiState()
    data class Home(val home: HomePage) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onSongClick: (SongItem) -> Unit,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val query by viewModel.query.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadHome()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.onQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search songs, artists, albums") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                },
                singleLine = true
            )

            when (val state = uiState) {
                is SearchUiState.Idle -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Search for your favorite music",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                is SearchUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is SearchUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
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
                is SearchUiState.Home -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        state.home.chips?.let { chips ->
                            item {
                                Text(
                                    text = "Browse",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                            items(chips.chunked(2)) { chipRow ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    chipRow.forEach { chip ->
                                        FilterChip(
                                            selected = false,
                                            onClick = { },
                                            label = { Text(chip.text) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        state.home.sections.forEach { section ->
                            item {
                                Text(
                                    text = section.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                            items(section.items) { item ->
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
                is SearchUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
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
                            Button(onClick = { viewModel.loadHome() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}