package com.example.moisesaichallenge.domain.repository

import com.example.moisesaichallenge.core.pagination.PaginationParams
import com.example.moisesaichallenge.domain.model.SearchEmission
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    fun searchTracks(
        query: String,
        paginationParams: PaginationParams
    ): Flow<SearchEmission>
}
