package com.example.moisesaichallenge.domain.model

data class Track(
    val id: Long,
    val name: String,
    val artistName: String,
    val albumName: String,
    val artworkUrl: String?,
    val previewUrl: String?,
    val durationMillis: Long,
    val genre: String
)
