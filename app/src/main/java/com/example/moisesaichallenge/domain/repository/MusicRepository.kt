package com.example.moisesaichallenge.domain.repository

import com.example.moisesaichallenge.domain.model.SearchResult
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    fun searchTracks(query: String, page: Int): Flow<SearchResult>
}
