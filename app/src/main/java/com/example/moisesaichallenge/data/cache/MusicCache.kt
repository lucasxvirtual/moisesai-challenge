package com.example.moisesaichallenge.data.cache

import com.example.moisesaichallenge.core.pagination.PaginatedResponse
import com.example.moisesaichallenge.core.pagination.PaginationParams
import com.example.moisesaichallenge.domain.model.Track

// This interface could be backed by a Room DAO instead of in-memory storage
// to provide true persistence across process restarts.
interface MusicCache {
    fun get(query: String, paginationParams: PaginationParams): PaginatedResponse<Track>?
    fun getStaleOrNull(query: String, paginationParams: PaginationParams): PaginatedResponse<Track>?
    fun put(query: String, paginationParams: PaginationParams, data: PaginatedResponse<Track>)
    fun clear()
}
