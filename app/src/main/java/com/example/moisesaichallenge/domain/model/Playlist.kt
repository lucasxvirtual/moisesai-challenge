package com.example.moisesaichallenge.domain.model

data class Playlist(
    val id: Long,
    val name: String,
    val tracks: List<Track>
)
