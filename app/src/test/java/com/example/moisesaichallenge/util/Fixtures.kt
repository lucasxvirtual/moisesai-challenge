package com.example.moisesaichallenge.util

import com.example.moisesaichallenge.domain.model.Playlist
import com.example.moisesaichallenge.domain.model.Track

fun makeTrack(
    id: Long = 1L,
    name: String = "Track $id",
    artistName: String = "Artist",
    albumName: String = "Album",
    previewUrl: String? = "https://example.com/preview.m4a",
    collectionId: Long = 10L
) = Track(
    id = id,
    name = name,
    artistName = artistName,
    albumName = albumName,
    artworkUrl = null,
    artworkUrlHd = null,
    previewUrl = previewUrl,
    durationMillis = 30_000L,
    genre = "Pop",
    collectionId = collectionId
)

fun makePlaylist(
    id: Long = 1L,
    name: String = "Playlist $id",
    tracks: List<Track> = emptyList()
) = Playlist(id = id, name = name, tracks = tracks)
