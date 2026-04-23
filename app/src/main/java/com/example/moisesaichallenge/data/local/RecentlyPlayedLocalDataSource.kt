package com.example.moisesaichallenge.data.local

import com.example.moisesaichallenge.domain.model.Track

// This interface could be backed by a Room DAO to persist recently played songs
// across app sessions without changing any layer above this one.
interface RecentlyPlayedLocalDataSource {
    fun getAll(): List<Track>
    fun add(track: Track)
    fun clear()
}
