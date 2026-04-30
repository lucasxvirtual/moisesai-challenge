package com.example.moisesaichallenge.data.network.datasource

import com.example.moisesaichallenge.core.network.NetworkResult
import com.example.moisesaichallenge.data.network.model.AlbumItemDto
import com.example.moisesaichallenge.data.network.model.AlbumLookupResponseDto
import com.example.moisesaichallenge.data.network.service.ITunesApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AlbumRemoteDataSourceImplTest {

    private val apiService = mockk<ITunesApiService>()
    private lateinit var dataSource: AlbumRemoteDataSourceImpl

    @Before
    fun setUp() {
        dataSource = AlbumRemoteDataSourceImpl(apiService)
    }

    @Test
    fun `lookupAlbum returns Success with response DTO on successful API call`() = runTest {
        // Given
        val collectionId = 123L
        val responseDto = AlbumLookupResponseDto(
            resultCount = 1,
            results = listOf(AlbumItemDto(wrapperType = "collection", collectionId = collectionId))
        )
        coEvery { apiService.lookupAlbum(any(), any()) } returns responseDto

        // When
        val result = dataSource.lookupAlbum(collectionId)

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(responseDto, (result as NetworkResult.Success).data)
        coVerify(exactly = 1) { apiService.lookupAlbum(collectionId = collectionId, entity = any()) }
    }

    @Test
    fun `lookupAlbum returns Error when API throws exception`() = runTest {
        // Given
        val exception = RuntimeException("Server error")
        coEvery { apiService.lookupAlbum(any(), any()) } throws exception

        // When
        val result = dataSource.lookupAlbum(99L)

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals(exception, (result as NetworkResult.Error).throwable)
    }
}
