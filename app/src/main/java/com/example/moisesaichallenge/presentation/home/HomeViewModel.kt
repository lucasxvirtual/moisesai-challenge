package com.example.moisesaichallenge.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moisesaichallenge.core.pagination.PaginationParams
import com.example.moisesaichallenge.core.playback.PlaybackManager
import com.example.moisesaichallenge.domain.model.SearchEmission
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.domain.usecase.GetRecentlyPlayedUseCase
import com.example.moisesaichallenge.domain.usecase.RecordTrackPlayedUseCase
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val searchTracksUseCase: SearchTracksUseCase,
    private val getRecentlyPlayedUseCase: GetRecentlyPlayedUseCase,
    private val recordTrackPlayedUseCase: RecordTrackPlayedUseCase,
    private val playbackManager: PlaybackManager
) : ViewModel() {

    private val _navigateToPlayer = MutableSharedFlow<Unit>(replay = 0)
    val navigateToPlayer: SharedFlow<Unit> = _navigateToPlayer.asSharedFlow()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var currentPaginationParams = PaginationParams()
    private var searchJob: Job? = null
    private var loadJob: Job? = null

    init {
        refreshRecentlyPlayed()
        playbackManager.currentTrack
            .onEach { track -> _uiState.update { it.copy(currentTrackId = track?.id) } }
            .launchIn(viewModelScope)
        playbackManager.isPlaying
            .onEach { playing -> _uiState.update { it.copy(isPlaying = playing) } }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query, error = null, isOffline = false) }
        searchJob?.cancel()
        loadJob?.cancel()

        if (query.isBlank()) {
            currentPaginationParams = PaginationParams()
            _uiState.update { it.copy(tracks = emptyList(), hasMore = false, isLoading = false, isOffline = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_MS)
            currentPaginationParams = PaginationParams()
            loadTracks(isLoadingMore = false)
        }
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || !state.hasMore) return
        currentPaginationParams = currentPaginationParams.nextPage()
        loadTracks(isLoadingMore = true)
    }

    fun onTrackClick(track: Track) {
        val tracks = if (_uiState.value.query.isBlank()) _uiState.value.recentlyPlayed else _uiState.value.tracks
        val index = tracks.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        recordTrackPlayedUseCase(track)
        refreshRecentlyPlayed()
        if (track.id != playbackManager.currentTrack.value?.id) {
            playbackManager.setQueue(tracks, index)
        }
        viewModelScope.launch { _navigateToPlayer.emit(Unit) }
    }

    fun onPlayPauseClick() {
        if (playbackManager.isPlaying.value) playbackManager.pause() else playbackManager.play()
    }

    fun onTrackPlayed(track: Track) {
        recordTrackPlayedUseCase(track)
        refreshRecentlyPlayed()
    }

    private fun refreshRecentlyPlayed() {
        _uiState.update { it.copy(recentlyPlayed = getRecentlyPlayedUseCase()) }
    }

    private fun loadTracks(isLoadingMore: Boolean) {
        val query = _uiState.value.query
        loadJob = viewModelScope.launch {
            _uiState.update { state ->
                if (isLoadingMore) state.copy(isLoadingMore = true) else state.copy(isLoading = true)
            }

            searchTracksUseCase(query, currentPaginationParams).collect { emission ->
                when (emission) {
                    is SearchEmission.Local -> {
                        _uiState.update { it.copy(tracks = emission.tracks, isLoading = true) }
                    }
                    is SearchEmission.Remote -> {
                        _uiState.update { state ->
                            if (isLoadingMore) {
                                val existingIds = state.tracks.mapTo(HashSet()) { it.id }
                                val newItems = emission.tracks.takeWhile { it.id !in existingIds }
                                val looped = newItems.size < emission.tracks.size
                                state.copy(
                                    tracks = state.tracks + newItems,
                                    isLoadingMore = false,
                                    hasMore = emission.hasMore && !looped
                                )
                            } else {
                                val apiIds = emission.tracks.mapTo(HashSet()) { it.id }
                                val localOnly = state.tracks.filter { it.id !in apiIds }
                                state.copy(
                                    tracks = emission.tracks + localOnly,
                                    isLoading = false,
                                    hasMore = emission.hasMore
                                )
                            }
                        }
                    }
                    is SearchEmission.Error -> {
                        val offline = emission.throwable is IOException
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                isLoadingMore = false,
                                isOffline = offline,
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
