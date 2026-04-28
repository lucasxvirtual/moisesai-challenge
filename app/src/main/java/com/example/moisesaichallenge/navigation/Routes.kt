package com.example.moisesaichallenge.navigation

import kotlinx.serialization.Serializable

@Serializable object Splash
@Serializable object Home
@Serializable object Player
@Serializable object CreatePlaylist
@Serializable data class Album(val collectionId: Long)
@Serializable data class PlaylistDetail(val playlistId: Long)
