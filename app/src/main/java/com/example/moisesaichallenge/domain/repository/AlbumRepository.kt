package com.example.moisesaichallenge.domain.repository

import com.example.moisesaichallenge.core.network.NetworkResult
import com.example.moisesaichallenge.domain.model.Album

interface AlbumRepository {
    suspend fun getAlbum(collectionId: Long): NetworkResult<Album>
}
