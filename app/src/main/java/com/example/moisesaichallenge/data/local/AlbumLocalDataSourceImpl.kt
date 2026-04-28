package com.example.moisesaichallenge.data.local

import com.example.moisesaichallenge.domain.model.Album
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlbumLocalDataSourceImpl @Inject constructor() : AlbumLocalDataSource {

    private val cache = HashMap<Long, Album>()

    override fun get(id: Long): Album? = cache[id]

    override fun put(album: Album) { cache[album.id] = album }
}
