package com.example.moisesaichallenge.presentation.playlists

import androidx.lifecycle.ViewModel
import com.example.moisesaichallenge.domain.model.Playlist
import com.example.moisesaichallenge.domain.usecase.DeletePlaylistUseCase
import com.example.moisesaichallenge.domain.usecase.GetPlaylistsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    getPlaylistsUseCase: GetPlaylistsUseCase,
    private val deletePlaylistUseCase: DeletePlaylistUseCase
) : ViewModel() {
    val playlists: StateFlow<List<Playlist>> = getPlaylistsUseCase()

    fun deletePlaylist(id: Long) = deletePlaylistUseCase(id)
}
