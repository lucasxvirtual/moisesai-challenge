package com.example.moisesaichallenge.core.pagination

data class PaginatedResponse<T>(
    val items: List<T>,
    val hasMore: Boolean,
    val currentPaginationParams: PaginationParams
)
