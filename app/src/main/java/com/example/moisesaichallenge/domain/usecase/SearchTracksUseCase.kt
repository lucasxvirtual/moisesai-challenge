package com.example.moisesaichallenge.domain.usecase

import com.example.moisesaichallenge.domain.model.SearchResult
import com.example.moisesaichallenge.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchTracksUseCase @Inject constructor(private val repository: MusicRepository) {

    operator fun invoke(query: String, page: Int = 1): Flow<SearchResult> =
        repository.searchTracks(query, page)
}
