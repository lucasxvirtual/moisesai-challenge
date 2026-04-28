package com.example.moisesaichallenge.domain.usecase

import com.example.moisesaichallenge.core.pagination.PaginationParams
import com.example.moisesaichallenge.domain.model.SearchEmission
import com.example.moisesaichallenge.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchTracksUseCase @Inject constructor(private val repository: MusicRepository) {

    operator fun invoke(
        query: String,
        paginationParams: PaginationParams = PaginationParams()
    ): Flow<SearchEmission> = repository.searchTracks(query, paginationParams)
}
