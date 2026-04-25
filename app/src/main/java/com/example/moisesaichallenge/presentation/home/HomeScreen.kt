package com.example.moisesaichallenge.presentation.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.presentation.home.components.RecentlyPlayedItem
import com.example.moisesaichallenge.presentation.home.components.TrackOptionsBottomSheet
import com.example.moisesaichallenge.presentation.search.SearchUiState
import com.example.moisesaichallenge.presentation.search.SearchViewModel
import com.example.moisesaichallenge.ui.theme.MoisesaiChallengeTheme

@Composable
fun HomeScreen(
    onNavigateToPlayer: () -> Unit,
    onNavigateToAlbum: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var selectedTrack by remember { mutableStateOf<Track?>(null) }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 3 && uiState.hasMore && !uiState.isLoadingMore
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadNextPage()
    }

    LaunchedEffect(Unit) {
        viewModel.navigateToPlayer.collect { onNavigateToPlayer() }
    }

    HomeScreenContent(
        uiState = uiState,
        listState = listState,
        selectedTrack = selectedTrack,
        onQueryChange = viewModel::onQueryChange,
        onTrackClick = { viewModel.onTrackClick(it) },
        onMoreClick = { selectedTrack = it },
        onDismissBottomSheet = { selectedTrack = null },
        onNavigateToAlbum = onNavigateToAlbum
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    uiState: SearchUiState,
    listState: LazyListState,
    selectedTrack: Track?,
    onQueryChange: (String) -> Unit,
    onTrackClick: (Track) -> Unit,
    onMoreClick: (Track) -> Unit,
    onDismissBottomSheet: () -> Unit,
    onNavigateToAlbum: (Long) -> Unit = {},
    initialSearchVisible: Boolean = false
) {
    var isSearchVisible by remember { mutableStateOf(initialSearchVisible) }
    val focusRequester = remember { FocusRequester() }
    val isKeyboardVisible = WindowInsets.isImeVisible

    LaunchedEffect(isKeyboardVisible) {
        if (!isKeyboardVisible && uiState.query.isBlank()) {
            isSearchVisible = false
        }
    }

    LaunchedEffect(isSearchVisible) {
        if (isSearchVisible) focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Songs",
                        style = MaterialTheme.typography.displayMedium
                    )
                },
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
        }
    ) { innerPadding ->
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
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
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
                    onTrackClick = onTrackClick,
                    onMoreClick = onMoreClick
                )
            } else {
                SearchResultsSection(
                    uiState = uiState,
                    listState = listState,
                    onTrackClick = onTrackClick,
                    onMoreClick = onMoreClick
                )
            }
        }
    }

    selectedTrack?.let { track ->
        TrackOptionsBottomSheet(
            track = track,
            onDismiss = onDismissBottomSheet,
            onViewAlbum = {
                onDismissBottomSheet()
                onNavigateToAlbum(track.collectionId)
            }
        )
    }
}

@Composable
private fun RecentlyPlayedSection(
    uiState: SearchUiState,
    onTrackClick: (Track) -> Unit,
    onMoreClick: (Track) -> Unit
) {
    Text(
        text = "Recently Played",
        style = MaterialTheme.typography.titleMedium,
    )

    Spacer(modifier = Modifier.height(4.dp))

    if (uiState.recentlyPlayed.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No recently played songs yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn {
            items(items = uiState.recentlyPlayed, key = { it.id }) { track ->
                RecentlyPlayedItem(
                    track = track,
                    onClick = { onTrackClick(track) },
                    onMoreClick = { onMoreClick(track) }
                )
            }
        }
    }
}

@Composable
private fun SearchResultsSection(
    uiState: SearchUiState,
    listState: LazyListState,
    onTrackClick: (Track) -> Unit,
    onMoreClick: (Track) -> Unit
) {
    when {
        uiState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        uiState.error != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        uiState.tracks.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
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
                    RecentlyPlayedItem(
                        track = track,
                        onClick = { onTrackClick(track) },
                        onMoreClick = { onMoreClick(track) }
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

// region Previews

private val previewTracks = listOf(
    Track(1L, "Bohemian Rhapsody", "Queen", "A Night at the Opera", null, null, null, 354_000L, "Rock"),
    Track(2L, "Don't Stop Me Now", "Queen", "Jazz", null, null, null, 210_000L, "Rock"),
    Track(3L, "We Will Rock You", "Queen", "News of the World", null, null, null, 122_000L, "Rock"),
    Track(4L, "Somebody to Love", "Queen", "A Day at the Races", null, null, null, 295_000L, "Rock")
)

@PreviewLightDark
@Composable
private fun HomeScreenRecentlyPlayedPreview() {
    MoisesaiChallengeTheme(dynamicColor = false) {
        HomeScreenContent(
            uiState = SearchUiState(query = "", recentlyPlayed = previewTracks),
            listState = rememberLazyListState(),
            selectedTrack = null,
            onQueryChange = {},
            onTrackClick = {},
            onMoreClick = {},
            onDismissBottomSheet = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun HomeScreenEmptyRecentlyPlayedPreview() {
    MoisesaiChallengeTheme(dynamicColor = false) {
        HomeScreenContent(
            uiState = SearchUiState(query = "", recentlyPlayed = emptyList()),
            listState = rememberLazyListState(),
            selectedTrack = null,
            onQueryChange = {},
            onTrackClick = {},
            onMoreClick = {},
            onDismissBottomSheet = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun HomeScreenSearchActivePreview() {
    MoisesaiChallengeTheme(dynamicColor = false) {
        HomeScreenContent(
            uiState = SearchUiState(query = "Queen", tracks = previewTracks),
            listState = rememberLazyListState(),
            selectedTrack = null,
            onQueryChange = {},
            onTrackClick = {},
            onMoreClick = {},
            onDismissBottomSheet = {},
            initialSearchVisible = true
        )
    }
}

@PreviewLightDark
@Composable
private fun HomeScreenSearchLoadingPreview() {
    MoisesaiChallengeTheme(dynamicColor = false) {
        HomeScreenContent(
            uiState = SearchUiState(query = "Queen", isLoading = true),
            listState = rememberLazyListState(),
            selectedTrack = null,
            onQueryChange = {},
            onTrackClick = {},
            onMoreClick = {},
            onDismissBottomSheet = {},
            initialSearchVisible = true
        )
    }
}

@PreviewLightDark
@Composable
private fun HomeScreenSearchErrorPreview() {
    MoisesaiChallengeTheme(dynamicColor = false) {
        HomeScreenContent(
            uiState = SearchUiState(query = "Queen", error = "No internet connection"),
            listState = rememberLazyListState(),
            selectedTrack = null,
            onQueryChange = {},
            onTrackClick = {},
            onMoreClick = {},
            onDismissBottomSheet = {},
            initialSearchVisible = true
        )
    }
}
