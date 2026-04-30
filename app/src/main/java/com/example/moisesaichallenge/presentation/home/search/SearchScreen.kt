package com.example.moisesaichallenge.presentation.home.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.moisesaichallenge.R
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.presentation.components.TrackItem
import com.example.moisesaichallenge.presentation.components.TrackOptionsBottomSheet
import com.example.moisesaichallenge.presentation.home.playlists.components.AddToPlaylistBottomSheet

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    innerPadding: PaddingValues,
    isSearchVisible: Boolean,
    onHideSearch: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    onNavigateToAlbum: (Long) -> Unit,
    onShowSnackbar: (String) -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val isKeyboardVisible = WindowInsets.isImeVisible
    var selectedTrack by remember { mutableStateOf<Track?>(null) }
    var trackForPlaylist by remember { mutableStateOf<Track?>(null) }
    val addedToPlaylistFormat = stringResource(R.string.snackbar_added_to_playlist)

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 3 && uiState.hasMore && !uiState.isLoadingNextPage
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadNextPage()
    }

    LaunchedEffect(Unit) {
        viewModel.navigateToPlayer.collect { onNavigateToPlayer() }
    }

    LaunchedEffect(Unit) {
        viewModel.searchError.collect { onShowSnackbar(it) }
    }

    LaunchedEffect(isKeyboardVisible) {
        if (!isKeyboardVisible && uiState.query.isBlank()) onHideSearch()
    }

    LaunchedEffect(isSearchVisible) {
        if (isSearchVisible) focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 24.dp)
    ) {
        if (isSearchVisible) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                placeholder = { Text(stringResource(R.string.hint_search)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.cd_clear_search))
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
            RecentlyPlayedSection(
                uiState = uiState,
                onTrackClick = { viewModel.onTrackClick(it) },
                onMoreClick = { selectedTrack = it },
                onPlayPauseClick = viewModel::onPlayPauseClick
            )
        } else {
            SearchResultsSection(
                uiState = uiState,
                listState = listState,
                onRefresh = viewModel::refresh,
                onTrackClick = { viewModel.onTrackClick(it) },
                onMoreClick = { selectedTrack = it },
                onPlayPauseClick = viewModel::onPlayPauseClick
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
            },
            onAddToPlaylist = {
                trackForPlaylist = track
                selectedTrack = null
            }
        )
    }

    trackForPlaylist?.let { track ->
        AddToPlaylistBottomSheet(
            playlists = playlists,
            onDismiss = { trackForPlaylist = null },
            onPlaylistSelected = { playlistId ->
                val playlistName = playlists.find { it.id == playlistId }?.name
                viewModel.addTrackToPlaylist(playlistId, track)
                trackForPlaylist = null
                if (playlistName != null) onShowSnackbar(addedToPlaylistFormat.format(playlistName))
            }
        )
    }
}

@Composable
private fun RecentlyPlayedSection(
    uiState: SearchUiState,
    onTrackClick: (Track) -> Unit,
    onMoreClick: (Track) -> Unit,
    onPlayPauseClick: () -> Unit
) {
    Text(text = stringResource(R.string.title_recently_played), style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(4.dp))

    if (uiState.recentlyPlayed.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.empty_recently_played),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchResultsSection(
    uiState: SearchUiState,
    listState: LazyListState,
    onRefresh: () -> Unit,
    onTrackClick: (Track) -> Unit,
    onMoreClick: (Track) -> Unit,
    onPlayPauseClick: () -> Unit
) {
    when {
        uiState.isLoading && uiState.tracks.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        uiState.tracks.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.no_results_for, uiState.query),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        else -> {
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh,
            ) {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
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

                    if (uiState.isLoadingNextPage) {
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
}
