package com.example.moisesaichallenge.domain.repository

import com.example.moisesaichallenge.domain.model.AlbumResult

interface AlbumRepository {
    suspend fun getAlbum(collectionId: Long): AlbumResult
}
