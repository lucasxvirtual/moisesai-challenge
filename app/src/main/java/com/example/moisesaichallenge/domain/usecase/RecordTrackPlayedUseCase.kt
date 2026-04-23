package com.example.moisesaichallenge.domain.usecase

import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.domain.repository.RecentlyPlayedRepository
import javax.inject.Inject

class RecordTrackPlayedUseCase @Inject constructor(private val repository: RecentlyPlayedRepository) {
    operator fun invoke(track: Track) = repository.recordPlayed(track)
}
