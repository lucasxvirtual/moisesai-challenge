package com.example.moisesaichallenge.data.network.datasource

import com.example.moisesaichallenge.core.network.NetworkResult
import com.example.moisesaichallenge.data.network.model.ITunesSearchResponseDto
import com.example.moisesaichallenge.data.network.model.TrackDto
import com.example.moisesaichallenge.data.network.service.ITunesApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MusicRemoteDataSourceImplTest {

    private val apiService = mockk<ITunesApiService>()
    private lateinit var dataSource: MusicRemoteDataSourceImpl

    @Before
    fun setUp() {
        dataSource = MusicRemoteDataSourceImpl(apiService)
    }

    @Test
    fun `searchTracks returns Success with list of DTOs on successful API call`() = runTest {
        // Given
        val dtos = listOf(TrackDto(id = 1L, name = "Song"), TrackDto(id = 2L, name = "Another"))
        coEvery { apiService.searchMusic(any(), any(), any(), any()) } returns
            ITunesSearchResponseDto(resultCount = 2, results = dtos)

        // When
        val result = dataSource.searchTracks("queen")

        // Then
        assertTrue(result is NetworkResult.Success)
        assertEquals(dtos, (result as NetworkResult.Success).data)
        coVerify(exactly = 1) { apiService.searchMusic(term = "queen", limit = any()) }
    }

    @Test
    fun `searchTracks returns Error when API throws exception`() = runTest {
        // Given
        val exception = RuntimeException("Network error")
        coEvery { apiService.searchMusic(any(), any(), any(), any()) } throws exception

        // When
        val result = dataSource.searchTracks("queen")

        // Then
        assertTrue(result is NetworkResult.Error)
        assertEquals(exception, (result as NetworkResult.Error).throwable)
    }

    @Test
    fun `searchTracks returns Success with empty list when API returns no results`() = runTest {
        // Given
        coEvery { apiService.searchMusic(any(), any(), any(), any()) } returns
            ITunesSearchResponseDto(resultCount = 0, results = emptyList())

        // When
        val result = dataSource.searchTracks("zzz")

        // Then
        assertTrue(result is NetworkResult.Success)
        assertTrue((result as NetworkResult.Success).data.isEmpty())
    }
}
