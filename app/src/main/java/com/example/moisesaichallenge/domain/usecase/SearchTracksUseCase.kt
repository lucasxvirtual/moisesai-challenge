package com.example.moisesaichallenge.domain.usecase

import com.example.moisesaichallenge.core.network.NetworkResult
import com.example.moisesaichallenge.core.pagination.PaginatedResponse
import com.example.moisesaichallenge.core.pagination.PaginationParams
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.domain.repository.MusicRepository
import javax.inject.Inject

class SearchTracksUseCase @Inject constructor(private val repository: MusicRepository) {

    suspend operator fun invoke(
        query: String,
        paginationParams: PaginationParams = PaginationParams()
    ): NetworkResult<PaginatedResponse<Track>> = repository.searchTracks(query, paginationParams)
}
