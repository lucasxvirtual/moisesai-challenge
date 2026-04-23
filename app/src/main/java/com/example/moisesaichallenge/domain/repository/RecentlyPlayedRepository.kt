package com.example.moisesaichallenge.domain.repository

import com.example.moisesaichallenge.domain.model.Track

interface RecentlyPlayedRepository {
    fun getRecentlyPlayed(): List<Track>
    fun recordPlayed(track: Track)
}
