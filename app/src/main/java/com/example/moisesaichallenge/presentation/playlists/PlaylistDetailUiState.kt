package com.example.moisesaichallenge.presentation.playlists

import com.example.moisesaichallenge.domain.model.Playlist

data class PlaylistDetailUiState(
    val playlist: Playlist? = null,
    val currentTrackId: Long? = null,
    val isPlaying: Boolean = false
)
