package com.example.moisesaichallenge.presentation.album

import com.example.moisesaichallenge.domain.model.Album

data class AlbumUiState(
    val album: Album? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isOffline: Boolean = false,
    val currentTrackId: Long? = null,
    val isPlaying: Boolean = false
)
