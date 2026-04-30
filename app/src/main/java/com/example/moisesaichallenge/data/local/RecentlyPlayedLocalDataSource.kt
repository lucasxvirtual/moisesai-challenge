package com.example.moisesaichallenge.data.local

import com.example.moisesaichallenge.domain.model.Track

interface RecentlyPlayedLocalDataSource {
    fun getAll(): List<Track>
    fun add(track: Track)
    fun clear()
}
