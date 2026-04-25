package com.example.moisesaichallenge.presentation.album

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moisesaichallenge.core.network.NetworkResult
import com.example.moisesaichallenge.core.playback.PlaybackManager
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.domain.usecase.GetAlbumUseCase
import com.example.moisesaichallenge.domain.usecase.RecordTrackPlayedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAlbumUseCase: GetAlbumUseCase,
    private val playbackManager: PlaybackManager,
    private val recordTrackPlayedUseCase: RecordTrackPlayedUseCase
) : ViewModel() {

    private val collectionId: Long = checkNotNull(savedStateHandle["collectionId"])

    private val _uiState = MutableStateFlow(AlbumUiState(isLoading = true))
    val uiState: StateFlow<AlbumUiState> = _uiState.asStateFlow()

    private val _navigateToPlayer = MutableSharedFlow<Unit>(replay = 0)
    val navigateToPlayer: SharedFlow<Unit> = _navigateToPlayer.asSharedFlow()

    init {
        loadAlbum()
    }

    fun onTrackClick(track: Track) {
        val tracks = _uiState.value.album?.tracks ?: return
        val index = tracks.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        recordTrackPlayedUseCase(track)
        playbackManager.setQueue(tracks, index)
        viewModelScope.launch { _navigateToPlayer.emit(Unit) }
    }

    private fun loadAlbum() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getAlbumUseCase(collectionId)) {
                is NetworkResult.Success -> _uiState.update {
                    it.copy(album = result.data, isLoading = false)
                }
                is NetworkResult.Error -> _uiState.update {
                    it.copy(error = result.throwable.message ?: "Unexpected error", isLoading = false)
                }
            }
        }
    }
}
