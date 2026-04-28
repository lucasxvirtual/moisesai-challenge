package com.example.moisesaichallenge.presentation.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.presentation.components.TrackOptionsBottomSheet
import com.example.moisesaichallenge.presentation.player.components.TrackProgressSlider
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(82.dp))

            AsyncImage(
                model = uiState.track?.artworkUrlHd,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(264.dp)
                    .clip(RoundedCornerShape(20.dp))
            )

            Spacer(Modifier.weight(1F))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1F)
                ) {
                    Text(
                        text = uiState.track?.name ?: "",
                        style = MaterialTheme.typography.headlineLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = uiState.track?.artistName ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onToggleRepeat) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Repeat",
                        tint = if (uiState.isRepeat) MaterialTheme.colorScheme.onPrimary
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            TrackProgressSlider(
                positionMs = uiState.positionMs,
                durationMs = uiState.durationMs,
                onSeekTo = onSeekTo,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onSkipPrevious,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        modifier = Modifier.size(36.dp)
                    )
                }

                FilledIconButton(
                    onClick = { if (uiState.isPlaying) onPause() else onPlay() },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(36.dp)
                    )
                }

                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(Modifier.height(33.dp))
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
