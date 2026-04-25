package com.example.moisesaichallenge.data.network.model

import com.google.gson.annotations.SerializedName

data class AlbumItemDto(
    @SerializedName("wrapperType") val wrapperType: String? = null,
    // Shared
    @SerializedName("collectionId") val collectionId: Long = 0,
    @SerializedName("artistName") val artistName: String? = null,
    @SerializedName("artworkUrl100") val artworkUrl: String? = null,
    @SerializedName("primaryGenreName") val genre: String? = null,
    // Collection-only
    @SerializedName("collectionName") val collectionName: String? = null,
    // Track-only
    @SerializedName("trackId") val trackId: Long = 0,
    @SerializedName("trackName") val trackName: String? = null,
    @SerializedName("trackNumber") val trackNumber: Int? = null,
    @SerializedName("trackTimeMillis") val durationMillis: Long? = null,
    @SerializedName("previewUrl") val previewUrl: String? = null
)
