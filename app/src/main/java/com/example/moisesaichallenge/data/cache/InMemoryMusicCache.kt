package com.example.moisesaichallenge.data.cache

import com.example.moisesaichallenge.core.pagination.PaginatedResponse
import com.example.moisesaichallenge.core.pagination.PaginationParams
import com.example.moisesaichallenge.domain.model.Track
import javax.inject.Inject

// Stores results in a HashMap keyed by (query, offset). Could be replaced by a
// Room-backed implementation to survive process death without changing callers.
class InMemoryMusicCache @Inject constructor() : MusicCache {

    private val entries = HashMap<CacheKey, CacheEntry>()

    override fun get(query: String, paginationParams: PaginationParams): PaginatedResponse<Track>? {
        val entry = entries[CacheKey(query, paginationParams.offset)] ?: return null
        return if (entry.isExpired()) null else entry.data
    }

    override fun getStaleOrNull(query: String, paginationParams: PaginationParams): PaginatedResponse<Track>? =
        entries[CacheKey(query, paginationParams.offset)]?.data

    override fun put(
        query: String,
        paginationParams: PaginationParams,
        data: PaginatedResponse<Track>
    ) {
        if (entries.size >= MAX_ENTRIES) evictOldest()
        entries[CacheKey(query, paginationParams.offset)] = CacheEntry(data)
    }

    override fun clear() = entries.clear()

    private fun evictOldest() {
        entries.minByOrNull { it.value.timestamp }?.key?.let { entries.remove(it) }
    }

    private data class CacheKey(val query: String, val offset: Int)

    private data class CacheEntry(
        val data: PaginatedResponse<Track>,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() - timestamp > TTL_MS
    }

    companion object {
        private const val TTL_MS = 5 * 60 * 1000L
        private const val MAX_ENTRIES = 100
    }
}
