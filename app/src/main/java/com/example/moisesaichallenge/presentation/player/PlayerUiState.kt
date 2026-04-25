package com.example.moisesaichallenge.presentation.player

import com.example.moisesaichallenge.domain.model.Track

data class PlayerUiState(
    val track: Track? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isRepeat: Boolean = false,
    val isLoading: Boolean = false
)
