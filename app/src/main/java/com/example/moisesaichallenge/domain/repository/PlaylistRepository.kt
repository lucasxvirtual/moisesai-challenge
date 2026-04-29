package com.example.moisesaichallenge.domain.repository

import com.example.moisesaichallenge.domain.model.Playlist
import com.example.moisesaichallenge.domain.model.Track
import kotlinx.coroutines.flow.StateFlow

interface PlaylistRepository {
    val playlists: StateFlow<List<Playlist>>
    fun create(name: String, tracks: List<Track>): Long
    fun update(id: Long, name: String, tracks: List<Track>)
    fun delete(id: Long)
    fun addTrack(playlistId: Long, track: Track)
}
