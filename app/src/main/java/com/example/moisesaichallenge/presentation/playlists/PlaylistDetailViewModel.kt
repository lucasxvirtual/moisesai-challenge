package com.example.moisesaichallenge.presentation.playlists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.moisesaichallenge.core.playback.PlaybackManager
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.domain.usecase.GetPlaylistsUseCase
import com.example.moisesaichallenge.domain.usecase.RecordTrackPlayedUseCase
import com.example.moisesaichallenge.navigation.PlaylistDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getPlaylistsUseCase: GetPlaylistsUseCase,
    private val playbackManager: PlaybackManager,
    private val recordTrackPlayedUseCase: RecordTrackPlayedUseCase
) : ViewModel() {

    private val playlistId: Long = savedStateHandle.toRoute<PlaylistDetail>().playlistId

    private val _uiState = MutableStateFlow(PlaylistDetailUiState())
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState.asStateFlow()

    private val _navigateToPlayer = MutableSharedFlow<Unit>(replay = 0)
    val navigateToPlayer: SharedFlow<Unit> = _navigateToPlayer.asSharedFlow()

    init {
        getPlaylistsUseCase()
            .onEach { playlists ->
                _uiState.update { it.copy(playlist = playlists.find { p -> p.id == playlistId }) }
            }
            .launchIn(viewModelScope)

        playbackManager.currentTrack
            .onEach { track -> _uiState.update { it.copy(currentTrackId = track?.id) } }
            .launchIn(viewModelScope)

        playbackManager.isPlaying
            .onEach { playing -> _uiState.update { it.copy(isPlaying = playing) } }
            .launchIn(viewModelScope)
    }

    fun onTrackClick(track: Track) {
        val tracks = _uiState.value.playlist?.tracks ?: return
        val index = tracks.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        recordTrackPlayedUseCase(track)
        playbackManager.setQueueKeepingCurrent(tracks, index)
        viewModelScope.launch { _navigateToPlayer.emit(Unit) }
    }

    fun onPlayPauseClick() {
        if (playbackManager.isPlaying.value) playbackManager.pause() else playbackManager.play()
    }
}
