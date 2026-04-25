package com.example.moisesaichallenge.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moisesaichallenge.core.playback.PlaybackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playbackManager: PlaybackManager
) : ViewModel() {

    val uiState: StateFlow<PlayerUiState> = combine(
        playbackManager.currentTrack,
        playbackManager.isPlaying,
        playbackManager.positionMs,
        playbackManager.durationMs,
        playbackManager.isRepeat
    ) { track, isPlaying, positionMs, durationMs, isRepeat ->
        PlayerUiState(
            track = track,
            isPlaying = isPlaying,
            positionMs = positionMs,
            durationMs = durationMs,
            isRepeat = isRepeat
        )
    }.combine(playbackManager.isLoading) { state, isLoading ->
        state.copy(isLoading = isLoading)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlayerUiState())

    fun play() = playbackManager.play()
    fun pause() = playbackManager.pause()
    fun seekTo(ms: Long) = playbackManager.seekTo(ms)
    fun skipNext() = playbackManager.skipNext()
    fun skipPrevious() = playbackManager.skipPrevious()
    fun toggleRepeat() = playbackManager.toggleRepeat()
}
