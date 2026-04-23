package com.example.moisesaichallenge.data.network.datasource

import com.example.moisesaichallenge.core.network.NetworkResult
import com.example.moisesaichallenge.core.network.safeApiCall
import com.example.moisesaichallenge.core.pagination.PaginatedResponse
import com.example.moisesaichallenge.core.pagination.PaginationParams
import com.example.moisesaichallenge.data.network.model.TrackDto
import com.example.moisesaichallenge.data.network.service.ITunesApiService
import javax.inject.Inject

class MusicRemoteDataSourceImpl @Inject constructor(
    private val apiService: ITunesApiService
) : MusicRemoteDataSource {

    override suspend fun searchTracks(
        query: String,
        paginationParams: PaginationParams
    ): NetworkResult<PaginatedResponse<TrackDto>> = safeApiCall {
        val response = apiService.searchMusic(
            term = query,
            limit = paginationParams.limit,
            offset = paginationParams.offset
        )
        PaginatedResponse(
            items = response.results,
            hasMore = response.resultCount == paginationParams.limit,
            currentPaginationParams = paginationParams
        )
    }
}
