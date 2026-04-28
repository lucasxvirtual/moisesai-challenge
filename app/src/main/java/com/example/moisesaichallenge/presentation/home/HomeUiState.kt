package com.example.moisesaichallenge.presentation.home

import com.example.moisesaichallenge.domain.model.Track

data class HomeUiState(
    val query: String = "",
    val tracks: List<Track> = emptyList(),
    val recentlyPlayed: List<Track> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val error: String? = null,
    val isOffline: Boolean = false,
    val currentTrackId: Long? = null,
    val isPlaying: Boolean = false
)
