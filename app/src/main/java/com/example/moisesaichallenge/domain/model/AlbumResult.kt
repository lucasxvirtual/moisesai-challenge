package com.example.moisesaichallenge.domain.model

sealed class AlbumResult {
    data class Success(val album: Album) : AlbumResult()
    data class Error(val throwable: Throwable) : AlbumResult()
}
