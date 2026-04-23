package com.example.moisesaichallenge.presentation.home

import androidx.lifecycle.ViewModel
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.domain.usecase.GetRecentlyPlayedUseCase
import com.example.moisesaichallenge.domain.usecase.RecordTrackPlayedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRecentlyPlayedUseCase: GetRecentlyPlayedUseCase,
    private val recordTrackPlayedUseCase: RecordTrackPlayedUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun onTrackPlayed(track: Track) {
        recordTrackPlayedUseCase(track)
        refresh()
    }

    private fun refresh() {
        _uiState.update { it.copy(recentlyPlayed = getRecentlyPlayedUseCase()) }
    }
}
