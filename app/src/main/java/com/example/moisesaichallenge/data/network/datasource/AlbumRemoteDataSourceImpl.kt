package com.example.moisesaichallenge.data.network.datasource

import com.example.moisesaichallenge.core.network.NetworkResult
import com.example.moisesaichallenge.core.network.safeApiCall
import com.example.moisesaichallenge.data.network.model.AlbumLookupResponseDto
import com.example.moisesaichallenge.data.network.service.ITunesApiService
import javax.inject.Inject

class AlbumRemoteDataSourceImpl @Inject constructor(
    private val apiService: ITunesApiService
) : AlbumRemoteDataSource {

    override suspend fun lookupAlbum(collectionId: Long): NetworkResult<AlbumLookupResponseDto> =
        safeApiCall { apiService.lookupAlbum(collectionId) }
}
