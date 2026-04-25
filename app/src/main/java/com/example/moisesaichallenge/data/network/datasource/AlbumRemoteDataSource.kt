package com.example.moisesaichallenge.data.network.datasource

import com.example.moisesaichallenge.core.network.NetworkResult
import com.example.moisesaichallenge.data.network.model.AlbumLookupResponseDto

interface AlbumRemoteDataSource {
    suspend fun lookupAlbum(collectionId: Long): NetworkResult<AlbumLookupResponseDto>
}
