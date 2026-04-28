package com.example.moisesaichallenge.core.pagination

data class PaginationParams(val page: Int = 1) {
    companion object {
        const val PAGE_SIZE = 20
        const val FETCH_LIMIT = 200
    }
}
