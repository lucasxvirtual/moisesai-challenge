package com.example.moisesaichallenge.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.moisesaichallenge.domain.model.Playlist
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.presentation.components.PlaylistArtwork
import com.example.moisesaichallenge.presentation.components.TrackItem
import com.example.moisesaichallenge.presentation.components.TrackOptionsBottomSheet
import com.example.moisesaichallenge.presentation.playlists.PlaylistsViewModel

private enum class HomeTab { Search, Playlists }

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPlayer: () -> Unit,
    onNavigateToAlbum: (Long) -> Unit,
    onNavigateToCreatePlaylist: () -> Unit,
    onNavigateToPlaylist: (Long) -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel(),
    playlistsViewModel: PlaylistsViewModel = hiltViewModel()
) {
    val uiState by homeViewModel.uiState.collectAsState()
    val playlists by playlistsViewModel.playlists.collectAsState()
    val listState = rememberLazyListState()

    var selectedTab by remember { mutableStateOf(HomeTab.Search) }
    var selectedTrack by remember { mutableStateOf<Track?>(null) }
    var isSearchVisible by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val isKeyboardVisible = WindowInsets.isImeVisible

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 3 && uiState.hasMore && !uiState.isLoadingMore
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) homeViewModel.loadNextPage()
    }

    LaunchedEffect(Unit) {
        homeViewModel.navigateToPlayer.collect { onNavigateToPlayer() }
    }

    LaunchedEffect(isKeyboardVisible) {
        if (!isKeyboardVisible && uiState.query.isBlank()) isSearchVisible = false
    }

    LaunchedEffect(isSearchVisible) {
        if (isSearchVisible) focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            when (selectedTab) {
                HomeTab.Search -> TopAppBar(
                    title = { Text("Songs", style = MaterialTheme.typography.displayMedium) },
                    actions = {
                        if (!isSearchVisible) {
                            IconButton(onClick = { isSearchVisible = true }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                HomeTab.Playlists -> TopAppBar(
                    title = { Text("Playlists", style = MaterialTheme.typography.displayMedium) },
                    actions = {
                        IconButton(onClick = onNavigateToCreatePlaylist) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create playlist",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == HomeTab.Search,
                    onClick = { selectedTab = HomeTab.Search },
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text("Search") }
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.Playlists,
                    onClick = { selectedTab = HomeTab.Playlists },
                    icon = { Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null) },
                    label = { Text("Playlists") }
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            HomeTab.Search -> SearchTabContent(
                uiState = uiState,
                listState = listState,
                innerPadding = innerPadding,
                isSearchVisible = isSearchVisible,
                focusRequester = focusRequester,
                onQueryChange = homeViewModel::onQueryChange,
                onTrackClick = { homeViewModel.onTrackClick(it) },
                onMoreClick = { selectedTrack = it },
                onPlayPauseClick = homeViewModel::onPlayPauseClick
            )
            HomeTab.Playlists -> PlaylistsTabContent(
                playlists = playlists,
                innerPadding = innerPadding,
                onPlaylistClick = onNavigateToPlaylist
            )
        }
    }

    selectedTrack?.let { track ->
        TrackOptionsBottomSheet(
            track = track,
            onDismiss = { selectedTrack = null },
            onViewAlbum = {
                selectedTrack = null
                onNavigateToAlbum(track.collectionId)
            }
        )
    }
}

@Composable
private fun SearchTabContent(
    uiState: HomeUiState,
    listState: LazyListState,
    innerPadding: PaddingValues,
    isSearchVisible: Boolean,
    focusRequester: FocusRequester,
    onQueryChange: (String) -> Unit,
    onTrackClick: (Track) -> Unit,
    onMoreClick: (Track) -> Unit,
    onPlayPauseClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 24.dp)
    ) {
        if (isSearchVisible) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                placeholder = { Text("Search songs, artists...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(20),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    cursorColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.query.isBlank()) {
            RecentlyPlayedSection(uiState, onTrackClick, onMoreClick, onPlayPauseClick)
        } else {
            SearchResultsSection(uiState, listState, onTrackClick, onMoreClick, onPlayPauseClick)
        }
    }
}

@Composable
private fun PlaylistsTabContent(
    playlists: List<Playlist>,
    innerPadding: PaddingValues,
    onPlaylistClick: (Long) -> Unit
) {
    if (playlists.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No playlists yet. Tap + to create one.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            items(playlists, key = { it.id }) { playlist ->
                PlaylistRow(playlist, onClick = { onPlaylistClick(playlist.id) })
            }
        }
    }
}

@Composable
private fun PlaylistRow(playlist: Playlist, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlaylistArtwork(playlist = playlist, size = 52.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${playlist.tracks.size} ${if (playlist.tracks.size == 1) "track" else "tracks"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecentlyPlayedSection(
    uiState: HomeUiState,
    onTrackClick: (Track) -> Unit,
    onMoreClick: (Track) -> Unit,
    onPlayPauseClick: () -> Unit
) {
    Text(text = "Recently Played", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(4.dp))

    if (uiState.recentlyPlayed.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No recently played songs yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn {
            items(items = uiState.recentlyPlayed, key = { it.id }) { track ->
                TrackItem(
                    track = track,
                    onClick = { onTrackClick(track) },
                    onMoreClick = { onMoreClick(track) },
                    isNowPlaying = track.id == uiState.currentTrackId,
                    isPlaying = uiState.isPlaying && track.id == uiState.currentTrackId,
                    onPlayPauseClick = onPlayPauseClick
                )
            }
        }
    }
}

@Composable
private fun SearchResultsSection(
    uiState: HomeUiState,
    listState: LazyListState,
    onTrackClick: (Track) -> Unit,
    onMoreClick: (Track) -> Unit,
    onPlayPauseClick: () -> Unit
) {
    when {
        uiState.isLoading && uiState.tracks.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        uiState.error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = uiState.error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        uiState.tracks.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No results for \"${uiState.query}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        else -> {
            LazyColumn(state = listState) {
                items(items = uiState.tracks, key = { it.id }) { track ->
                    TrackItem(
                        track = track,
                        onClick = { onTrackClick(track) },
                        onMoreClick = { onMoreClick(track) },
                        isNowPlaying = track.id == uiState.currentTrackId,
                        isPlaying = uiState.isPlaying && track.id == uiState.currentTrackId,
                        onPlayPauseClick = onPlayPauseClick
                    )
                }

                if (uiState.isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}
