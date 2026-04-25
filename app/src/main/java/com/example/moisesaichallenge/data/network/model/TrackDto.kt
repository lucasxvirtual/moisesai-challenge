package com.example.moisesaichallenge.data.network.model

import com.google.gson.annotations.SerializedName

data class TrackDto(
    @SerializedName("trackId") val id: Long = 0,
    @SerializedName("trackName") val name: String? = null,
    @SerializedName("artistName") val artistName: String? = null,
    @SerializedName("collectionName") val albumName: String? = null,
    @SerializedName("collectionId") val collectionId: Long = 0,
    @SerializedName("artworkUrl100") val artworkUrl: String? = null,
    @SerializedName("previewUrl") val previewUrl: String? = null,
    @SerializedName("trackTimeMillis") val durationMillis: Long? = null,
    @SerializedName("primaryGenreName") val genre: String? = null,
    @SerializedName("releaseDate") val releaseDate: String? = null
)
