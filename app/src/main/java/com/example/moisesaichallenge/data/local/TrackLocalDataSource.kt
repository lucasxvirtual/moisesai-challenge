package com.example.moisesaichallenge.data.local

import com.example.moisesaichallenge.domain.model.Track

interface TrackLocalDataSource {
    fun addAll(tracks: List<Track>)
    fun search(query: String): List<Track>
}
