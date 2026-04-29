package com.example.moisesaichallenge.presentation.home.playlists.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.moisesaichallenge.domain.model.Playlist
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.presentation.home.playlists.components.PlaylistArtwork
import com.example.moisesaichallenge.presentation.home.playlists.components.PlaylistOptionsBottomSheet
import com.example.moisesaichallenge.presentation.components.TrackItem

@Composable
fun PlaylistDetailScreen(
    onBack: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    onNavigateToEdit: () -> Unit,
    viewModel: PlaylistDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigateToPlayer.collect { onNavigateToPlayer() }
    }
    LaunchedEffect(Unit) {
        viewModel.navigateBack.collect { onBack() }
    }

    PlaylistDetailContent(
        uiState = uiState,
        onBack = onBack,
        onNavigateToEdit = onNavigateToEdit,
        onDeletePlaylist = viewModel::deletePlaylist,
        onTrackClick = viewModel::onTrackClick,
        onPlayPauseClick = viewModel::onPlayPauseClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistDetailContent(
    uiState: PlaylistDetailUiState,
    onBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onDeletePlaylist: () -> Unit,
    onTrackClick: (Track) -> Unit,
    onPlayPauseClick: () -> Unit
) {
    var showOptions by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.playlist?.name ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.displaySmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showOptions = true }) {
                        Icon(
                            Icons.Default.MoreVert, contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        uiState.playlist?.let { playlist ->
            PlaylistContent(
                playlist = playlist,
                modifier = Modifier.padding(innerPadding),
                currentTrackId = uiState.currentTrackId,
                isPlaying = uiState.isPlaying,
                onTrackClick = onTrackClick,
                onPlayPauseClick = onPlayPauseClick
            )

            if (showOptions) {
                PlaylistOptionsBottomSheet(
                    playlist = playlist,
                    onDismiss = { showOptions = false },
                    onEdit = { showOptions = false; onNavigateToEdit() },
                    onDelete = { showOptions = false; onDeletePlaylist() }
                )
            }
        }
    }
}

@Composable
private fun PlaylistContent(
    playlist: Playlist,
    modifier: Modifier = Modifier,
    currentTrackId: Long? = null,
    isPlaying: Boolean = false,
    onTrackClick: (Track) -> Unit,
    onPlayPauseClick: () -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(Modifier.height(8.dp))

            PlaylistArtwork(
                playlist = playlist,
                size = 200.dp,
                modifier = Modifier.size(200.dp)
            )

            Spacer(Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 20.sp),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(48.dp))
        }

        if (playlist.tracks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No songs yet.\nSearch and add songs to get started.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(items = playlist.tracks, key = { it.id }) { track ->
                TrackItem(
                    track = track,
                    onClick = { onTrackClick(track) },
                    onMoreClick = null,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    isNowPlaying = track.id == currentTrackId,
                    isPlaying = isPlaying && track.id == currentTrackId,
                    onPlayPauseClick = onPlayPauseClick
                )
            }
        }
    }
}
