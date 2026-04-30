package com.example.moisesaichallenge.data.repository

import com.example.moisesaichallenge.core.network.NetworkResult
import com.example.moisesaichallenge.core.pagination.PaginationParams.PAGE_SIZE
import com.example.moisesaichallenge.data.local.TrackLocalDataSource
import com.example.moisesaichallenge.data.network.datasource.MusicRemoteDataSource
import com.example.moisesaichallenge.data.network.model.TrackDto
import com.example.moisesaichallenge.domain.model.SearchResult
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MusicRepositoryImpl @Inject constructor(
    private val remoteDataSource: MusicRemoteDataSource,
    private val trackCache: TrackLocalDataSource
) : MusicRepository {

    override fun searchTracks(query: String, page: Int): Flow<SearchResult> = flow {
        val offset = (page - 1) * PAGE_SIZE

        if (page == 1) {
            val cached = trackCache.search(query)
            if (cached.isNotEmpty()) {
                emit(SearchResult.Success(cached.take(PAGE_SIZE), hasMore = cached.size > PAGE_SIZE))
            }

            when (val result = remoteDataSource.searchTracks(query)) {
                is NetworkResult.Success -> {
                    val tracks = result.data.mapNotNull { it.toDomain() }
                    trackCache.addAll(tracks)
                    val allCached = trackCache.search(query)
                    emit(SearchResult.Success(
                        tracks = allCached.take(PAGE_SIZE),
                        hasMore = allCached.size > PAGE_SIZE
                    ))
                }
                is NetworkResult.Error -> emit(SearchResult.Error(result.throwable))
            }
        } else {
            val allCached = trackCache.search(query)
            val slice = allCached.drop(offset).take(PAGE_SIZE)
            emit(SearchResult.Success(
                tracks = slice,
                hasMore = allCached.size > offset + PAGE_SIZE
            ))
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
