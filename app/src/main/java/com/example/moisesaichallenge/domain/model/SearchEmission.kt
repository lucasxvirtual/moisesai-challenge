package com.example.moisesaichallenge.domain.model

sealed class SearchEmission {
    data class Local(val tracks: List<Track>) : SearchEmission()
    data class Remote(val tracks: List<Track>, val hasMore: Boolean) : SearchEmission()
    data class Error(val throwable: Throwable) : SearchEmission()
}
