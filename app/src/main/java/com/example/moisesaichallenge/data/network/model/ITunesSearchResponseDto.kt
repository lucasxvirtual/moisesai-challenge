package com.example.moisesaichallenge.data.network.model

import com.google.gson.annotations.SerializedName

data class ITunesSearchResponseDto(
    @SerializedName("resultCount") val resultCount: Int = 0,
    @SerializedName("results") val results: List<TrackDto> = emptyList()
)
