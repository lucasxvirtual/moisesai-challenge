package com.example.moisesaichallenge.presentation.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.moisesaichallenge.R
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.ui.theme.BottomSheetBackground80
import com.example.moisesaichallenge.ui.theme.MoisesaiChallengeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackOptionsBottomSheet(
    track: Track,
    onDismiss: () -> Unit,
    onViewAlbum: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = BottomSheetBackground80
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 24.dp, end = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = track.name,
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = track.artistName,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onViewAlbum)
                    .padding(vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_setlist),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "View album",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
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

private val previewTrackLongTitle = Track(
    id = 2L,
    name = "Don't Stop Me Now (Live at Hammersmith Odeon, London '75)",
    artistName = "Queen",
    albumName = "A Night at the Opera",
    artworkUrl = null,
    artworkUrlHd = null,
    previewUrl = null,
    durationMillis = 210_000L,
    genre = "Rock"
)

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
private fun TrackOptionsBottomSheetPreview() {
    MoisesaiChallengeTheme(dynamicColor = false) {
        TrackOptionsBottomSheet(
            track = previewTrack,
            onDismiss = {},
            onViewAlbum = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
private fun TrackOptionsBottomSheetLongTitlePreview() {
    MoisesaiChallengeTheme(dynamicColor = false) {
        TrackOptionsBottomSheet(
            track = previewTrackLongTitle,
            onDismiss = {},
            onViewAlbum = {}
        )
    }
}

// endregion
