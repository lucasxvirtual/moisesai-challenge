package com.example.moisesaichallenge.data.network.model

import com.google.gson.annotations.SerializedName

data class AlbumLookupResponseDto(
    @SerializedName("resultCount") val resultCount: Int = 0,
    @SerializedName("results") val results: List<AlbumItemDto> = emptyList()
)
