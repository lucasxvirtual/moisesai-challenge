package com.example.moisesaichallenge.presentation.home.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moisesaichallenge.core.playback.PlaybackManager
import com.example.moisesaichallenge.domain.model.SearchResult
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.domain.model.Playlist
import com.example.moisesaichallenge.domain.repository.MusicRepository
import com.example.moisesaichallenge.domain.repository.PlaylistRepository
import com.example.moisesaichallenge.domain.repository.RecentlyPlayedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val recentlyPlayedRepository: RecentlyPlayedRepository,
    private val playlistRepository: PlaylistRepository,
    private val playbackManager: PlaybackManager
) : ViewModel() {

    val playlists: StateFlow<List<Playlist>> = playlistRepository.playlists

    private val _navigateToPlayer = MutableSharedFlow<Unit>(replay = 0)
    val navigateToPlayer: SharedFlow<Unit> = _navigateToPlayer.asSharedFlow()

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var currentPage = 1
    private var searchJob: Job? = null
    private var loadJob: Job? = null

    init {
        playbackManager.currentTrack
            .onEach { track -> _uiState.update { it.copy(currentTrackId = track?.id) } }
            .launchIn(viewModelScope)
        playbackManager.isPlaying
            .onEach { playing -> _uiState.update { it.copy(isPlaying = playing) } }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(query: String) {
        searchJob?.cancel()
        loadJob?.cancel()

        if (query.isBlank()) {
            currentPage = 1
            _uiState.update { it.copy(query = query, tracks = emptyList(), hasMore = false, isLoading = false, error = null) }
            return
        }

        _uiState.update { it.copy(query = query, tracks = emptyList(), isLoading = true, error = null) }

        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            currentPage = 1
            loadTracks(isLoadingMore = false)
        }
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || !state.hasMore) return
        currentPage++
        loadTracks(isLoadingMore = true)
    }

    fun onTrackClick(track: Track) {
        val tracks = if (_uiState.value.query.isBlank()) _uiState.value.recentlyPlayed else _uiState.value.tracks
        val index = tracks.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        recentlyPlayedRepository.recordPlayed(track)
        if (track.id != playbackManager.currentTrack.value?.id) {
            playbackManager.setQueue(tracks, index)
        }
        viewModelScope.launch { _navigateToPlayer.emit(Unit) }
    }

    fun onPlayPauseClick() {
        if (playbackManager.isPlaying.value) playbackManager.pause() else playbackManager.play()
    }

    fun addTrackToPlaylist(playlistId: Long, track: Track) {
        playlistRepository.addTrack(playlistId, track)
    }

    fun refreshRecentlyPlayed() {
        _uiState.update { it.copy(recentlyPlayed = recentlyPlayedRepository.getRecentlyPlayed()) }
    }

    private fun loadTracks(isLoadingMore: Boolean) {
        val query = _uiState.value.query
        loadJob = viewModelScope.launch {
            _uiState.update { state ->
                if (isLoadingMore) state.copy(isLoadingMore = true) else state.copy(isLoading = true)
            }

            musicRepository.searchTracks(query, currentPage).collect { emission ->
                when (emission) {
                    is SearchResult.Success -> {
                        _uiState.update { state ->
                            if (isLoadingMore) {
                                state.copy(
                                    tracks = state.tracks + emission.tracks,
                                    isLoadingMore = false,
                                    hasMore = emission.hasMore
                                )
                            } else {
                                state.copy(
                                    tracks = emission.tracks,
                                    isLoading = false,
                                    hasMore = emission.hasMore
                                )
                            }
                        }
                    }
                    is SearchResult.Error -> {
                        val offline = emission.throwable is IOException
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                isLoadingMore = false,
                                error = if (offline) null else emission.throwable.message ?: "Unexpected error"
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val DEBOUNCE_MS = 300L
    }
}
