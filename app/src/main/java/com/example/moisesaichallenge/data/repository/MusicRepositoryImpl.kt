package com.example.moisesaichallenge.data.repository

import com.example.moisesaichallenge.core.network.NetworkResult
import com.example.moisesaichallenge.core.network.map
import com.example.moisesaichallenge.core.pagination.PaginatedResponse
import com.example.moisesaichallenge.core.pagination.PaginationParams
import com.example.moisesaichallenge.data.cache.MusicCache
import com.example.moisesaichallenge.data.network.datasource.MusicRemoteDataSource
import com.example.moisesaichallenge.data.network.model.TrackDto
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.domain.repository.MusicRepository
import javax.inject.Inject

class MusicRepositoryImpl @Inject constructor(
    private val remoteDataSource: MusicRemoteDataSource,
    private val cache: MusicCache
) : MusicRepository {

    override suspend fun searchTracks(
        query: String,
        paginationParams: PaginationParams
    ): NetworkResult<PaginatedResponse<Track>> {
        cache.get(query, paginationParams)?.let { return NetworkResult.Success(it) }

        val networkResult = remoteDataSource.searchTracks(query, paginationParams).map { paginated ->
            PaginatedResponse(
                items = paginated.items.mapNotNull { it.toDomain() },
                hasMore = paginated.hasMore,
                currentPaginationParams = paginated.currentPaginationParams
            ).also { cache.put(query, paginationParams, it) }
        }

        if (networkResult is NetworkResult.Error) {
            cache.getStaleOrNull(query, paginationParams)?.let { return NetworkResult.Success(it) }
        }

        return networkResult
    }

    private fun TrackDto.toDomain(): Track? {
        val trackName = name?.takeIf { it.isNotBlank() } ?: return null
        return Track(
            id = id,
            name = trackName,
            artistName = artistName.orEmpty(),
            albumName = albumName.orEmpty(),
            artworkUrl = artworkUrl,
            previewUrl = previewUrl,
            durationMillis = durationMillis ?: 0L,
            genre = genre.orEmpty()
        )
    }
}
