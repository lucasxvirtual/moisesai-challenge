package com.example.moisesaichallenge.presentation.playlists

import com.example.moisesaichallenge.domain.model.Track

data class CreatePlaylistUiState(
    val name: String = "",
    val query: String = "",
    val tracks: List<Track> = emptyList(),
    val recentlyPlayed: List<Track> = emptyList(),
    val selectedTrackIds: Set<Long> = emptySet(),
    val selectedTracksList: List<Track> = emptyList(),
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val error: String? = null
)
