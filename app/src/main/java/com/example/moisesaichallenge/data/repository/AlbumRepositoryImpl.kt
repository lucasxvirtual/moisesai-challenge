package com.example.moisesaichallenge.data.repository

import com.example.moisesaichallenge.core.network.NetworkResult
import com.example.moisesaichallenge.data.local.AlbumLocalDataSource
import com.example.moisesaichallenge.data.network.datasource.AlbumRemoteDataSource
import com.example.moisesaichallenge.data.network.model.AlbumItemDto
import com.example.moisesaichallenge.data.network.model.AlbumLookupResponseDto
import com.example.moisesaichallenge.domain.model.Album
import com.example.moisesaichallenge.domain.model.AlbumResult
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.domain.repository.AlbumRepository
import javax.inject.Inject

class AlbumRepositoryImpl @Inject constructor(
    private val remoteDataSource: AlbumRemoteDataSource,
    private val albumLocalDataSource: AlbumLocalDataSource
) : AlbumRepository {

    override suspend fun getAlbum(collectionId: Long): AlbumResult {
        albumLocalDataSource.get(collectionId)?.let { return AlbumResult.Success(it) }

        return when (val result = remoteDataSource.lookupAlbum(collectionId)) {
            is NetworkResult.Success -> {
                val album = result.data.toDomain()
                    ?: return AlbumResult.Error(IllegalStateException("Album not found"))
                albumLocalDataSource.put(album)
                AlbumResult.Success(album)
            }
            is NetworkResult.Error -> AlbumResult.Error(result.throwable)
        }
    }

    private fun AlbumLookupResponseDto.toDomain(): Album? {
        val collection = results.firstOrNull { it.wrapperType == "collection" } ?: return null
        val tracks = results
            .filter { it.wrapperType == "track" && it.trackName?.isNotBlank() == true }
            .sortedBy { it.trackNumber ?: Int.MAX_VALUE }
            .map { it.toTrack(collection) }
        return Album(
            id = collection.collectionId,
            name = collection.collectionName.orEmpty(),
            artistName = collection.artistName.orEmpty(),
            artworkUrl = collection.artworkUrl,
            // See MusicRepositoryImpl for why this URL manipulation works
            artworkUrlHd = collection.artworkUrl?.replace("100x100bb", "600x600bb"),
            tracks = tracks
        )
    }

    private fun AlbumItemDto.toTrack(collection: AlbumItemDto): Track {
        val artwork = artworkUrl ?: collection.artworkUrl
        return Track(
            id = trackId,
            name = trackName.orEmpty(),
            artistName = artistName ?: collection.artistName.orEmpty(),
            albumName = collection.collectionName.orEmpty(),
            artworkUrl = artwork,
            artworkUrlHd = artwork?.replace("100x100bb", "600x600bb"),
            previewUrl = previewUrl,
            durationMillis = durationMillis ?: 0L,
            genre = genre ?: collection.genre.orEmpty(),
            collectionId = collection.collectionId
        )
    }
}
