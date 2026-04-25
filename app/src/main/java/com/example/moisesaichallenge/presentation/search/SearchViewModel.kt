package com.example.moisesaichallenge.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moisesaichallenge.core.network.NetworkResult
import com.example.moisesaichallenge.core.pagination.PaginationParams
import com.example.moisesaichallenge.core.playback.PlaybackManager
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchTracksUseCase: SearchTracksUseCase,
    private val getRecentlyPlayedUseCase: GetRecentlyPlayedUseCase,
    private val recordTrackPlayedUseCase: RecordTrackPlayedUseCase,
    private val playbackManager: PlaybackManager
) : ViewModel() {

    private val _navigateToPlayer = MutableSharedFlow<Unit>(replay = 0)
    val navigateToPlayer: SharedFlow<Unit> = _navigateToPlayer.asSharedFlow()

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var currentPaginationParams = PaginationParams()
    private var searchJob: Job? = null

    init {
        refreshRecentlyPlayed()
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query, error = null) }
        searchJob?.cancel()

        if (query.isBlank()) {
            currentPaginationParams = PaginationParams()
            _uiState.update { it.copy(tracks = emptyList(), hasMore = false, isLoading = false) }
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
        playbackManager.setQueue(tracks, index)
        viewModelScope.launch { _navigateToPlayer.emit(Unit) }
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
        viewModelScope.launch {
            _uiState.update { state ->
                if (isLoadingMore) state.copy(isLoadingMore = true)
                else state.copy(isLoading = true)
            }

            when (val result = searchTracksUseCase(query, currentPaginationParams)) {
                is NetworkResult.Success -> {
                    val paginated = result.data
                    _uiState.update { state ->
                        state.copy(
                            tracks = if (isLoadingMore) state.tracks + paginated.items
                                     else paginated.items,
                            isLoading = false,
                            isLoadingMore = false,
                            hasMore = paginated.hasMore
                        )
                    }
                }

                is NetworkResult.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            error = result.throwable.message ?: "Unexpected error"
                        )
                    }
                }
            }
        }
    }

    companion object {
        private const val DEBOUNCE_MS = 300L
    }
}
