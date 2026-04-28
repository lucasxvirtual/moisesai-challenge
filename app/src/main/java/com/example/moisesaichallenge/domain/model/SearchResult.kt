package com.example.moisesaichallenge.domain.model

sealed class SearchResult {
    data class Success(val tracks: List<Track>, val hasMore: Boolean) : SearchResult()
    data class Error(val throwable: Throwable) : SearchResult()
}
