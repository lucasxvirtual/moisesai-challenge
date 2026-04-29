package com.example.moisesaichallenge.domain.usecase

import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.domain.repository.PlaylistRepository
import javax.inject.Inject

class CreatePlaylistUseCase @Inject constructor(private val repository: PlaylistRepository) {
    operator fun invoke(name: String, tracks: List<Track>): Long = repository.create(name, tracks)
}
