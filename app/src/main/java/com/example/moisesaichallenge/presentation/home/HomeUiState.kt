package com.example.moisesaichallenge.presentation.home

import com.example.moisesaichallenge.domain.model.Track

data class HomeUiState(
    val recentlyPlayed: List<Track> = emptyList()
)
