package com.example.moisesaichallenge.presentation.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.moisesaichallenge.R
import com.example.moisesaichallenge.domain.model.Playlist
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.presentation.components.TrackOptionsBottomSheet
import com.example.moisesaichallenge.presentation.home.playlists.components.AddToPlaylistBottomSheet
import com.example.moisesaichallenge.ui.theme.MoisesaiChallengeTheme

@Composable
fun PlayerScreen(
    onBack: () -> Unit,
    onNavigateToAlbum: (Long) -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    var trackForPlaylist by remember { mutableStateOf<Track?>(null) }
    val addedToPlaylistFormat = androidx.compose.ui.res.stringResource(R.string.snackbar_added_to_playlist)

    PlayerScreenContent(
        uiState = uiState,
        playlists = playlists,
        snackbarHostState = snackbarHostState,
        showBottomSheet = showBottomSheet,
        trackForPlaylist = trackForPlaylist,
        onBack = onBack,
        onShowBottomSheet = { showBottomSheet = true },
        onDismissBottomSheet = { showBottomSheet = false },
        onAddToPlaylist = { trackForPlaylist = uiState.track },
        onDismissPlaylistSheet = { trackForPlaylist = null },
        onPlaylistSelected = { playlistId ->
            trackForPlaylist?.let { track ->
                viewModel.addTrackToPlaylist(playlistId, track)
                val name = playlists.find { it.id == playlistId }?.name
                if (name != null) scope.launch { snackbarHostState.showSnackbar(addedToPlaylistFormat.format(name)) }
            }
            trackForPlaylist = null
        },
        onNavigateToAlbum = onNavigateToAlbum,
        onPlay = viewModel::play,
        onPause = viewModel::pause,
        onSeekTo = viewModel::seekTo,
        onSkipNext = viewModel::skipNext,
        onSkipPrevious = viewModel::skipPrevious,
        onToggleRepeat = viewModel::toggleRepeat
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerScreenContent(
    uiState: PlayerUiState,
    playlists: List<Playlist> = emptyList(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    showBottomSheet: Boolean,
    trackForPlaylist: Track? = null,
    onBack: () -> Unit,
    onShowBottomSheet: () -> Unit,
    onDismissBottomSheet: () -> Unit,
    onAddToPlaylist: () -> Unit = {},
    onDismissPlaylistSheet: () -> Unit = {},
    onPlaylistSelected: (Long) -> Unit = {},
    onNavigateToAlbum: (Long) -> Unit = {},
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleRepeat: () -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.title_now_playing),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.displaySmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onShowBottomSheet) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.cd_more_options),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(82.dp))
            PlayerContent(
                uiState = uiState,
                artworkSize = 264.dp,
                modifier = Modifier.weight(1f),
                onPlay = onPlay,
                onPause = onPause,
                onSeekTo = onSeekTo,
                onSkipNext = onSkipNext,
                onSkipPrevious = onSkipPrevious,
                onToggleRepeat = onToggleRepeat
            )
        }
    }

    if (showBottomSheet) {
        uiState.track?.let { track ->
            TrackOptionsBottomSheet(
                track = track,
                onDismiss = onDismissBottomSheet,
                onViewAlbum = {
                    onDismissBottomSheet()
                    onNavigateToAlbum(track.collectionId)
                },
                onAddToPlaylist = {
                    onDismissBottomSheet()
                    onAddToPlaylist()
                }
            )
        }
    }

    trackForPlaylist?.let {
        AddToPlaylistBottomSheet(
            playlists = playlists,
            onDismiss = onDismissPlaylistSheet,
            onPlaylistSelected = onPlaylistSelected
        )
    }
}

// region Previews

private val previewTrack = Track(
    id = 1L,
    name = "Bohemian Rhapsody",
    artistName = "Queen",
    albumName = "A Night at the Opera",
    artworkUrl = null,
    artworkUrlHd = null,
    previewUrl = null,
    durationMillis = 354_000L,
    genre = "Rock"
)

@PreviewLightDark
@Composable
private fun PlayerScreenPlayingPreview() {
    MoisesaiChallengeTheme {
        PlayerScreenContent(
            uiState = PlayerUiState(
                track = previewTrack,
                isPlaying = true,
                positionMs = 86_000L,
                durationMs = 354_000L,
                isRepeat = false
            ),
            showBottomSheet = false,
            onBack = {},
            onShowBottomSheet = {},
            onDismissBottomSheet = {},
            onPlay = {},
            onPause = {},
            onSeekTo = {},
            onSkipNext = {},
            onSkipPrevious = {},
            onToggleRepeat = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun PlayerScreenPausedRepeatPreview() {
    MoisesaiChallengeTheme {
        PlayerScreenContent(
            uiState = PlayerUiState(
                track = previewTrack,
                isPlaying = false,
                positionMs = 0L,
                durationMs = 354_000L,
                isRepeat = true
            ),
            showBottomSheet = false,
            onBack = {},
            onShowBottomSheet = {},
            onDismissBottomSheet = {},
            onPlay = {},
            onPause = {},
            onSeekTo = {},
            onSkipNext = {},
            onSkipPrevious = {},
            onToggleRepeat = {}
        )
    }
}
