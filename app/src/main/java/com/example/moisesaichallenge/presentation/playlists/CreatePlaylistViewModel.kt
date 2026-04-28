package com.example.moisesaichallenge.presentation.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moisesaichallenge.domain.model.SearchResult
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.domain.usecase.CreatePlaylistUseCase
import com.example.moisesaichallenge.domain.usecase.GetRecentlyPlayedUseCase
import com.example.moisesaichallenge.domain.usecase.SearchTracksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
class CreatePlaylistViewModel @Inject constructor(
    private val searchTracksUseCase: SearchTracksUseCase,
    private val getRecentlyPlayedUseCase: GetRecentlyPlayedUseCase,
    private val createPlaylistUseCase: CreatePlaylistUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePlaylistUiState())
    val uiState: StateFlow<CreatePlaylistUiState> = _uiState.asStateFlow()

    private val _created = MutableSharedFlow<Unit>(replay = 0)
    val created: SharedFlow<Unit> = _created.asSharedFlow()

    private val selectedTracks = LinkedHashMap<Long, Track>()
    private var searchJob: Job? = null
    private var loadJob: Job? = null

    init {
        _uiState.update { it.copy(recentlyPlayed = getRecentlyPlayedUseCase()) }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onQueryChange(query: String) {
        searchJob?.cancel()
        loadJob?.cancel()

        if (query.isBlank()) {
            _uiState.update { it.copy(query = query, tracks = emptyList(), isLoading = false, error = null) }
            return
        }

        _uiState.update { it.copy(query = query, tracks = emptyList(), isLoading = true, error = null) }

        searchJob = viewModelScope.launch {
            delay(300L)
            loadTracks(query)
        }
    }

    fun onToggleTrack(track: Track) {
        if (track.id in selectedTracks) {
            selectedTracks.remove(track.id)
        } else {
            selectedTracks[track.id] = track
        }
        _uiState.update { it.copy(selectedTrackIds = selectedTracks.keys.toSet()) }
    }

    fun createPlaylist() {
        val name = _uiState.value.name.trim()
        if (name.isBlank()) return
        createPlaylistUseCase(name, selectedTracks.values.toList())
        viewModelScope.launch { _created.emit(Unit) }
    }

    private fun loadTracks(query: String) {
        loadJob = viewModelScope.launch {
            searchTracksUseCase(query, page = 1).collect { emission ->
                when (emission) {
                    is SearchResult.Success -> _uiState.update {
                        it.copy(tracks = emission.tracks, isLoading = false)
                    }
                    is SearchResult.Error -> _uiState.update {
                        it.copy(isLoading = false, error = emission.throwable.message ?: "Error")
                    }
                }
            }
        }
    }
}
