package com.example.moisesaichallenge.domain.usecase

import com.example.moisesaichallenge.domain.model.Playlist
import com.example.moisesaichallenge.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetPlaylistsUseCase @Inject constructor(private val repository: PlaylistRepository) {
    operator fun invoke(): StateFlow<List<Playlist>> = repository.playlists
}
