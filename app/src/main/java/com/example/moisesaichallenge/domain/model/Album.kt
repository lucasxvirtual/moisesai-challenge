package com.example.moisesaichallenge.domain.model

data class Album(
    val id: Long,
    val name: String,
    val artistName: String,
    val artworkUrl: String?,
    val artworkUrlHd: String?,
    val tracks: List<Track>
)
