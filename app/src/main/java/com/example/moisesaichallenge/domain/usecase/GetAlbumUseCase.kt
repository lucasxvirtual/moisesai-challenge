package com.example.moisesaichallenge.domain.usecase

import com.example.moisesaichallenge.domain.model.AlbumResult
import com.example.moisesaichallenge.domain.repository.AlbumRepository
import javax.inject.Inject

class GetAlbumUseCase @Inject constructor(
    private val albumRepository: AlbumRepository
) {
    suspend operator fun invoke(collectionId: Long): AlbumResult =
        albumRepository.getAlbum(collectionId)
}
