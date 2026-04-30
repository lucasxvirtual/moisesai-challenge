package com.example.moisesaichallenge.presentation.home.search

import com.example.moisesaichallenge.domain.model.Track

data class SearchUiState(
    val query: String = "",
    val tracks: List<Track> = emptyList(),
    val recentlyPlayed: List<Track> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingNextPage: Boolean = false,
    val hasMore: Boolean = false,
    val currentTrackId: Long? = null,
    val isPlaying: Boolean = false
)
