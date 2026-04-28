package com.example.moisesaichallenge.data.local

import com.example.moisesaichallenge.domain.model.Album

interface AlbumLocalDataSource {
    fun get(id: Long): Album?
    fun put(album: Album)
}
