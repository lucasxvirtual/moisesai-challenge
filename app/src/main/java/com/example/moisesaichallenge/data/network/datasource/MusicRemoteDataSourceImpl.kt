package com.example.moisesaichallenge.data.network.datasource

import com.example.moisesaichallenge.core.network.NetworkResult
import com.example.moisesaichallenge.core.network.safeApiCall
import com.example.moisesaichallenge.core.pagination.PaginationParams.FETCH_LIMIT
import com.example.moisesaichallenge.data.network.model.TrackDto
import com.example.moisesaichallenge.data.network.service.ITunesApiService
import javax.inject.Inject

class MusicRemoteDataSourceImpl @Inject constructor(
    private val apiService: ITunesApiService
) : MusicRemoteDataSource {

    override suspend fun searchTracks(query: String): NetworkResult<List<TrackDto>> = safeApiCall {
        apiService.searchMusic(term = query, limit = FETCH_LIMIT).results
    }
}
