package com.example.moisesaichallenge.domain.usecase

import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.domain.repository.RecentlyPlayedRepository
import javax.inject.Inject

class GetRecentlyPlayedUseCase @Inject constructor(private val repository: RecentlyPlayedRepository) {
    operator fun invoke(): List<Track> = repository.getRecentlyPlayed()
}
