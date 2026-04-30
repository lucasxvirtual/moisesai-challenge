package com.example.moisesaichallenge.presentation.home.playlists.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.moisesaichallenge.domain.model.SearchResult
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.domain.repository.MusicRepository
import com.example.moisesaichallenge.domain.repository.PlaylistRepository
import com.example.moisesaichallenge.domain.repository.RecentlyPlayedRepository
import com.example.moisesaichallenge.navigation.CreatePlaylist
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
    savedStateHandle: SavedStateHandle,
    private val musicRepository: MusicRepository,
    private val recentlyPlayedRepository: RecentlyPlayedRepository,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val playlistId: Long = savedStateHandle.toRoute<CreatePlaylist>().playlistId
    private val isEditMode = playlistId != 0L

    private val _uiState = MutableStateFlow(CreatePlaylistUiState())
    val uiState: StateFlow<CreatePlaylistUiState> = _uiState.asStateFlow()

    private val _created = MutableSharedFlow<Long>(replay = 0)
    val created: SharedFlow<Long> = _created.asSharedFlow()

    private val selectedTracks = LinkedHashMap<Long, Track>()
    private var searchJob: Job? = null
    private var loadJob: Job? = null

    init {
        _uiState.update { it.copy(recentlyPlayed = recentlyPlayedRepository.getRecentlyPlayed(), isEditMode = isEditMode) }

        if (isEditMode) {
            val playlist = playlistRepository.playlists.value.find { it.id == playlistId }
            playlist?.let { p ->
                p.tracks.forEach { selectedTracks[it.id] = it }
                _uiState.update {
                    it.copy(
                        name = p.name,
                        selectedTrackIds = selectedTracks.keys.toSet(),
                        selectedTracksList = selectedTracks.values.toList()
                    )
                }
            }
        }
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
        _uiState.update {
            it.copy(
                selectedTrackIds = selectedTracks.keys.toSet(),
                selectedTracksList = selectedTracks.values.toList()
            )
        }
    }

    fun savePlaylist() {
        val name = _uiState.value.name.trim()
        if (name.isBlank()) return
        val tracks = selectedTracks.values.toList()
        val emitId = if (isEditMode) {
            playlistRepository.update(playlistId, name, tracks)
            0L
        } else {
            playlistRepository.create(name, tracks)
        }
        viewModelScope.launch { _created.emit(emitId) }
    }

    private fun loadTracks(query: String) {
        loadJob = viewModelScope.launch {
            musicRepository.searchTracks(query, page = 1).collect { emission ->
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
