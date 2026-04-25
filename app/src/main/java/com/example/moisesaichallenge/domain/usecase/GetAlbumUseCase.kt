package com.example.moisesaichallenge.domain.usecase

import com.example.moisesaichallenge.core.network.NetworkResult
import com.example.moisesaichallenge.domain.model.Album
import com.example.moisesaichallenge.domain.repository.AlbumRepository
import javax.inject.Inject

class GetAlbumUseCase @Inject constructor(
    private val albumRepository: AlbumRepository
) {
    suspend operator fun invoke(collectionId: Long): NetworkResult<Album> =
        albumRepository.getAlbum(collectionId)
}
