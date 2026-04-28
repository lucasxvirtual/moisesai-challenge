package com.example.moisesaichallenge.presentation.album

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.moisesaichallenge.domain.model.Album
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.presentation.components.TrackItem
import com.example.moisesaichallenge.ui.theme.MoisesaiChallengeTheme

@Composable
fun AlbumScreen(
    onBack: () -> Unit,
    onNavigateToPlayer: () -> Unit,
    viewModel: AlbumViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigateToPlayer.collect { onNavigateToPlayer() }
    }

    AlbumScreenContent(
        uiState = uiState,
        onBack = onBack,
        onTrackClick = viewModel::onTrackClick,
        onPlayPauseClick = viewModel::onPlayPauseClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlbumScreenContent(
    uiState: AlbumUiState,
    onBack: () -> Unit,
    onTrackClick: (Track) -> Unit,
    onPlayPauseClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.album?.name ?: "",
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
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }

            uiState.album != null -> {
                AlbumContent(
                    album = uiState.album,
                    onTrackClick = onTrackClick,
                    modifier = Modifier.padding(innerPadding),
                    currentTrackId = uiState.currentTrackId,
                    isPlaying = uiState.isPlaying,
                    onPlayPauseClick = onPlayPauseClick
                )
            }
        }
    }
}

@Composable
private fun AlbumContent(
    album: Album,
    onTrackClick: (Track) -> Unit,
    modifier: Modifier = Modifier,
    currentTrackId: Long? = null,
    isPlaying: Boolean = false,
    onPlayPauseClick: () -> Unit = {}
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(Modifier.height(8.dp))

            AsyncImage(
                model = album.artworkUrlHd,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp))
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
                    text = album.name,
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 20.sp),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = album.artistName,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(48.dp))
        }

        items(items = album.tracks, key = { it.id }) { track ->
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

// region Previews

private val previewAlbum = Album(
    id = 1L,
    name = "A Night at the Opera",
    artistName = "Queen",
    artworkUrl = null,
    artworkUrlHd = null,
    tracks = listOf(
        Track(1L, "Death on Two Legs", "Queen", "A Night at the Opera", null, null, null, 214_000L, "Rock", 1L),
        Track(2L, "Lazing on a Sunday Afternoon", "Queen", "A Night at the Opera", null, null, null, 69_000L, "Rock", 1L),
        Track(3L, "I'm in Love with My Car", "Queen", "A Night at the Opera", null, null, null, 183_000L, "Rock", 1L),
        Track(4L, "You're My Best Friend", "Queen", "A Night at the Opera", null, null, null, 170_000L, "Rock", 1L),
        Track(5L, "Bohemian Rhapsody", "Queen", "A Night at the Opera", null, null, null, 354_000L, "Rock", 1L),
    )
)

@PreviewLightDark
@Composable
private fun AlbumScreenLoadedPreview() {
    MoisesaiChallengeTheme(dynamicColor = false) {
        AlbumScreenContent(
            uiState = AlbumUiState(album = previewAlbum),
            onBack = {},
            onTrackClick = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun AlbumScreenLoadingPreview() {
    MoisesaiChallengeTheme(dynamicColor = false) {
        AlbumScreenContent(
            uiState = AlbumUiState(isLoading = true),
            onBack = {},
            onTrackClick = {}
        )
    }
}

// endregion
