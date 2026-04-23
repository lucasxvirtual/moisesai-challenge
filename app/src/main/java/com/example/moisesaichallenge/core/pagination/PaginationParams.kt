package com.example.moisesaichallenge.core.pagination

data class PaginationParams(
    val limit: Int = DEFAULT_PAGE_SIZE,
    val offset: Int = 0
) {
    fun nextPage(): PaginationParams = copy(offset = offset + limit)

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }
}
