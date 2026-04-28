package com.example.moisesaichallenge.data.network.datasource

import com.example.moisesaichallenge.core.network.NetworkResult
import com.example.moisesaichallenge.data.network.model.TrackDto

interface MusicRemoteDataSource {
    suspend fun searchTracks(query: String): NetworkResult<List<TrackDto>>
}
