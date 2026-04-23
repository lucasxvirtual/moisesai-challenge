package com.example.moisesaichallenge.domain.repository

import com.example.moisesaichallenge.core.network.NetworkResult
import com.example.moisesaichallenge.core.pagination.PaginatedResponse
import com.example.moisesaichallenge.core.pagination.PaginationParams
import com.example.moisesaichallenge.domain.model.Track

interface MusicRepository {
    suspend fun searchTracks(
        query: String,
        paginationParams: PaginationParams
    ): NetworkResult<PaginatedResponse<Track>>
}
