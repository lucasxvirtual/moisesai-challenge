package com.example.moisesaichallenge.data.local

import com.example.moisesaichallenge.domain.model.Track
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackLocalDataSourceImpl @Inject constructor() : TrackLocalDataSource {

    private val tracks = LinkedHashMap<Long, Track>()

    override fun addAll(newTracks: List<Track>) {
        newTracks.forEach { tracks[it.id] = it }
    }

    override fun search(query: String): List<Track> {
        val lower = query.trim().lowercase()
        return tracks.values.filter {
            it.name.lowercase().contains(lower) || it.artistName.lowercase().contains(lower)
        }
    }
}
