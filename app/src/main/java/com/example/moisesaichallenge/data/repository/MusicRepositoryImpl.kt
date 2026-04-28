package com.example.moisesaichallenge.data.repository

import com.example.moisesaichallenge.core.network.NetworkResult
import com.example.moisesaichallenge.core.pagination.PaginationParams
import com.example.moisesaichallenge.data.local.TrackLocalDataSource
import com.example.moisesaichallenge.data.network.datasource.MusicRemoteDataSource
import com.example.moisesaichallenge.data.network.model.TrackDto
import com.example.moisesaichallenge.domain.model.SearchEmission
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MusicRepositoryImpl @Inject constructor(
    private val remoteDataSource: MusicRemoteDataSource,
    private val trackCache: TrackLocalDataSource
) : MusicRepository {

    override fun searchTracks(
        query: String,
        paginationParams: PaginationParams
    ): Flow<SearchEmission> = flow {
        if (paginationParams.offset == 0) {
            val cached = trackCache.search(query)
            if (cached.isNotEmpty()) emit(SearchEmission.Local(cached))
        }

        when (val result = remoteDataSource.searchTracks(query, paginationParams)) {
            is NetworkResult.Success -> {
                val tracks = result.data.items.mapNotNull { it.toDomain() }
                trackCache.addAll(tracks)
                emit(SearchEmission.Remote(tracks, result.data.hasMore))
            }
            is NetworkResult.Error -> emit(SearchEmission.Error(result.throwable))
        }
    }

    private fun TrackDto.toDomain(): Track? {
        val trackName = name?.takeIf { it.isNotBlank() } ?: return null
        return Track(
            id = id,
            name = trackName,
            artistName = artistName.orEmpty(),
            albumName = albumName.orEmpty(),
            artworkUrl = artworkUrl,
            // The iTunes CDN serves any resolution via URL — "100x100bb" is just a size token
            // that can be replaced with e.g. "600x600bb". Not in the API spec but widely used.
            artworkUrlHd = artworkUrl?.replace("100x100bb", "600x600bb"),
            previewUrl = previewUrl,
            durationMillis = durationMillis ?: 0L,
            genre = genre.orEmpty(),
            collectionId = collectionId
        )
    }
}
