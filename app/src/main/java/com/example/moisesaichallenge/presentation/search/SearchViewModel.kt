package com.example.moisesaichallenge.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moisesaichallenge.core.network.NetworkResult
import com.example.moisesaichallenge.core.pagination.PaginationParams
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.domain.usecase.GetRecentlyPlayedUseCase
import com.example.moisesaichallenge.domain.usecase.RecordTrackPlayedUseCase
import com.example.moisesaichallenge.domain.usecase.SearchTracksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchTracksUseCase: SearchTracksUseCase,
    private val getRecentlyPlayedUseCase: GetRecentlyPlayedUseCase,
    private val recordTrackPlayedUseCase: RecordTrackPlayedUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var currentPaginationParams = PaginationParams()
    private var currentQuery = ""

    init {
        refreshRecentlyPlayed()
    }

    fun search(query: String) {
        if (query.isBlank()) return
        currentQuery = query
        currentPaginationParams = PaginationParams()
        _uiState.update { SearchUiState(query = query, recentlyPlayed = it.recentlyPlayed) }
        loadTracks(isLoadingMore = false)
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || !state.hasMore) return
        currentPaginationParams = currentPaginationParams.nextPage()
        loadTracks(isLoadingMore = true)
    }

    fun onTrackPlayed(track: Track) {
        recordTrackPlayedUseCase(track)
        refreshRecentlyPlayed()
    }

    private fun refreshRecentlyPlayed() {
        _uiState.update { it.copy(recentlyPlayed = getRecentlyPlayedUseCase()) }
    }

    private fun loadTracks(isLoadingMore: Boolean) {
        viewModelScope.launch {
            _uiState.update { state ->
                if (isLoadingMore) state.copy(isLoadingMore = true, error = null)
                else state.copy(isLoading = true, error = null)
            }

            when (val result = searchTracksUseCase(currentQuery, currentPaginationParams)) {
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
}
