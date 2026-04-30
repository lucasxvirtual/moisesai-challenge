package com.example.moisesaichallenge.presentation.home.playlists.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.moisesaichallenge.domain.model.Playlist
import com.example.moisesaichallenge.presentation.home.playlists.components.PlaylistArtwork
import com.example.moisesaichallenge.presentation.home.playlists.components.PlaylistOptionsBottomSheet

@Composable
fun PlaylistsScreen(
    innerPadding: PaddingValues,
    onPlaylistClick: (Long) -> Unit,
    onEditPlaylist: (Long) -> Unit,
    viewModel: PlaylistsViewModel = hiltViewModel()
) {
    val playlists by viewModel.playlists.collectAsState()
    var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }

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
                PlaylistRow(
                    playlist = playlist,
                    onClick = { onPlaylistClick(playlist.id) },
                    onMoreClick = { selectedPlaylist = playlist }
                )
            }
        }
    }

    selectedPlaylist?.let { playlist ->
        PlaylistOptionsBottomSheet(
            playlist = playlist,
            onDismiss = { selectedPlaylist = null },
            onEdit = { selectedPlaylist = null; onEditPlaylist(playlist.id) },
            onDelete = { viewModel.deletePlaylist(playlist.id); selectedPlaylist = null }
        )
    }
}

@Composable
private fun PlaylistRow(
    playlist: Playlist,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick),
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
        IconButton(onClick = onMoreClick) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
    }
}
