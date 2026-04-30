package com.example.moisesaichallenge.presentation.album

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moisesaichallenge.core.playback.PlaybackManager
import com.example.moisesaichallenge.domain.model.AlbumResult
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.domain.repository.AlbumRepository
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
class AlbumViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val albumRepository: AlbumRepository,
    private val playbackManager: PlaybackManager
) : ViewModel() {

    private val collectionId: Long = savedStateHandle.get<Long>("collectionId")!!

    private val _uiState = MutableStateFlow(AlbumUiState(isLoading = true))
    val uiState: StateFlow<AlbumUiState> = _uiState.asStateFlow()

    private val _navigateToPlayer = MutableSharedFlow<Unit>(replay = 0)
    val navigateToPlayer: SharedFlow<Unit> = _navigateToPlayer.asSharedFlow()

    init {
        loadAlbum()
        playbackManager.currentTrack
            .onEach { track -> _uiState.update { it.copy(currentTrackId = track?.id) } }
            .launchIn(viewModelScope)
        playbackManager.isPlaying
            .onEach { playing -> _uiState.update { it.copy(isPlaying = playing) } }
            .launchIn(viewModelScope)
    }

    fun onPlayPauseClick() {
        if (playbackManager.isPlaying.value) playbackManager.pause() else playbackManager.play()
    }

    fun onTrackClick(track: Track) {
        val tracks = _uiState.value.album?.tracks ?: return
        val index = tracks.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        playbackManager.setQueueKeepingCurrent(tracks, index)
        viewModelScope.launch { _navigateToPlayer.emit(Unit) }
    }

    private fun loadAlbum() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = albumRepository.getAlbum(collectionId)) {
                is AlbumResult.Success -> _uiState.update {
                    it.copy(album = result.album, isLoading = false)
                }
                is AlbumResult.Error -> {
                    val offline = result.throwable is java.io.IOException
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isOffline = offline,
                            error = if (offline) null else result.throwable.message ?: "Unexpected error"
                        )
                    }
                }
            }
        }
    }
}
