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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.presentation.components.TrackOptionsBottomSheet
import com.example.moisesaichallenge.ui.theme.MoisesaiChallengeTheme

@Composable
fun PlayerScreen(
    onBack: () -> Unit,
    onNavigateToAlbum: (Long) -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }

    PlayerScreenContent(
        uiState = uiState,
        showBottomSheet = showBottomSheet,
        onBack = onBack,
        onShowBottomSheet = { showBottomSheet = true },
        onDismissBottomSheet = { showBottomSheet = false },
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
    showBottomSheet: Boolean,
    onBack: () -> Unit,
    onShowBottomSheet: () -> Unit,
    onDismissBottomSheet: () -> Unit,
    onNavigateToAlbum: (Long) -> Unit = {},
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleRepeat: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Now playing",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.displaySmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onShowBottomSheet) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
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
                }
            )
        }
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
    MoisesaiChallengeTheme(dynamicColor = false) {
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
    MoisesaiChallengeTheme(dynamicColor = false) {
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

// endregion
