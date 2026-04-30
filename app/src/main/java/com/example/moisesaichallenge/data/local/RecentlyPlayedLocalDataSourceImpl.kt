package com.example.moisesaichallenge.data.local

import com.example.moisesaichallenge.domain.model.Track
import javax.inject.Inject

class RecentlyPlayedLocalDataSourceImpl @Inject constructor() : RecentlyPlayedLocalDataSource {

    private val tracks = ArrayDeque<Track>()

    override fun getAll(): List<Track> = tracks.toList()

    override fun add(track: Track) {
        if (tracks.any { it.id == track.id }) return
        tracks.addFirst(track)
        if (tracks.size > MAX_SIZE) tracks.removeLast()
    }

    override fun clear() = tracks.clear()

    companion object {
        private const val MAX_SIZE = 50
    }
}
