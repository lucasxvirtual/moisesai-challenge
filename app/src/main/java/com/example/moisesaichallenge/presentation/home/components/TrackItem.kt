package com.example.moisesaichallenge.presentation.home.components

import android.R.attr.onClick
import android.R.attr.track
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.ui.theme.MoisesaiChallengeTheme

@Composable
fun RecentlyPlayedItem(
    track: Track,
    onClick: () -> Unit,
    onMoreClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = track.artworkUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.width(6.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = track.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artistName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        onMoreClick?.let {
            IconButton(onClick = it) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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
    durationMillis = 210_000L,
    genre = "Rock"
)

private val previewTrackLongName = Track(
    id = 2L,
    name = "The Show Must Go On (Live at Wembley Stadium, July 1986)",
    artistName = "Queen & David Bowie & Elton John & Friends",
    albumName = "Live at Wembley '86",
    artworkUrl = null,
    artworkUrlHd = null,
    previewUrl = null,
    durationMillis = 420_000L,
    genre = "Rock"
)

@PreviewLightDark
@Composable
private fun RecentlyPlayedItemPreview() {
    MoisesaiChallengeTheme(dynamicColor = false) {
        Surface {
            RecentlyPlayedItem(
                track = previewTrack,
                onClick = {},
                onMoreClick = {}
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun RecentlyPlayedItemLongNamePreview() {
    MoisesaiChallengeTheme(dynamicColor = false) {
        Surface {
            RecentlyPlayedItem(
                track = previewTrackLongName,
                onClick = {},
                onMoreClick = {}
            )
        }
    }
}

// endregion
