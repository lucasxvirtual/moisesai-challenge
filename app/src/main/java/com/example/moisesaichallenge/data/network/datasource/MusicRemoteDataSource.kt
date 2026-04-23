package com.example.moisesaichallenge.data.network.datasource

import com.example.moisesaichallenge.core.network.NetworkResult
import com.example.moisesaichallenge.core.pagination.PaginatedResponse
import com.example.moisesaichallenge.core.pagination.PaginationParams
import com.example.moisesaichallenge.data.network.model.TrackDto

interface MusicRemoteDataSource {
    suspend fun searchTracks(
        query: String,
        paginationParams: PaginationParams
    ): NetworkResult<PaginatedResponse<TrackDto>>
}
