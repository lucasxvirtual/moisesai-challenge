package com.example.moisesaichallenge.presentation.home.playlists.list

import androidx.lifecycle.ViewModel
import com.example.moisesaichallenge.domain.model.Playlist
import com.example.moisesaichallenge.domain.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {
    val playlists: StateFlow<List<Playlist>> = playlistRepository.playlists

    fun deletePlaylist(id: Long) = playlistRepository.delete(id)
}
