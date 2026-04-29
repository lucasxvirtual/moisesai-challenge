package com.example.moisesaichallenge.data.repository

import com.example.moisesaichallenge.domain.model.Playlist
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepositoryImpl @Inject constructor() : PlaylistRepository {

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    override val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private var nextId = 1L

    override fun create(name: String, tracks: List<Track>): Long {
        val id = nextId++
        _playlists.update { it + Playlist(id = id, name = name, tracks = tracks) }
        return id
    }

    override fun update(id: Long, name: String, tracks: List<Track>) {
        _playlists.update { list ->
            list.map { if (it.id == id) it.copy(name = name, tracks = tracks) else it }
        }
    }

    override fun delete(id: Long) {
        _playlists.update { list -> list.filter { it.id != id } }
    }

    override fun addTrack(playlistId: Long, track: Track) {
        _playlists.update { list ->
            list.map { playlist ->
                if (playlist.id == playlistId && playlist.tracks.none { it.id == track.id }) {
                    playlist.copy(tracks = playlist.tracks + track)
                } else playlist
            }
        }
    }
}
