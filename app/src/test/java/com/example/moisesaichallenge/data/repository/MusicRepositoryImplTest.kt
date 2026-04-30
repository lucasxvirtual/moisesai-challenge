package com.example.moisesaichallenge.data.repository

import com.example.moisesaichallenge.core.network.NetworkResult
import com.example.moisesaichallenge.data.local.TrackLocalDataSource
import com.example.moisesaichallenge.data.network.datasource.MusicRemoteDataSource
import com.example.moisesaichallenge.data.network.model.TrackDto
import com.example.moisesaichallenge.domain.model.SearchResult
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.util.makeTrack
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MusicRepositoryImplTest {

    private val remoteDataSource = mockk<MusicRemoteDataSource>()
    private val trackCache = mockk<TrackLocalDataSource>(relaxed = true)
    private lateinit var repository: MusicRepositoryImpl

    @Before
    fun setUp() {
        repository = MusicRepositoryImpl(remoteDataSource, trackCache)
    }

    private fun makeDto(id: Long, name: String = "Track $id") = TrackDto(
        id = id,
        name = name,
        artistName = "Artist",
        albumName = "Album",
        collectionId = 10L,
        durationMillis = 30_000L
    )

    @Test
    fun `page 1 emits cached result first when cache is not empty`() = runTest {
        // Given
        val cached = listOf(makeTrack(id = 1L))
        every { trackCache.search(any()) } returns cached
        coEvery { remoteDataSource.searchTracks(any()) } returns NetworkResult.Success(emptyList())

        // When
        val emissions = repository.searchTracks("queen", page = 1).toList()

        // Then — first emission is from cache
        val first = emissions.first() as SearchResult.Success
        assertEquals(cached, first.tracks)
    }

    @Test
    fun `page 1 does not emit cached result when cache is empty`() = runTest {
        // Given — empty cache
        every { trackCache.search(any()) } returns emptyList()
        val remoteTracks = listOf(makeTrack(id = 1L))
        val dtos = listOf(makeDto(id = 1L))
        coEvery { remoteDataSource.searchTracks(any()) } returns NetworkResult.Success(dtos)
        every { trackCache.search(any()) } returnsMany listOf(emptyList(), remoteTracks)

        // When
        val emissions = repository.searchTracks("queen", page = 1).toList()

        // Then — only one emission (from remote), no cached pre-emit
        assertEquals(1, emissions.size)
    }

    @Test
    fun `page 1 remote success updates cache and emits merged result`() = runTest {
        // Given
        every { trackCache.search(any()) } returnsMany listOf(emptyList(), listOf(makeTrack(id = 1L)))
        val dto = makeDto(id = 1L)
        coEvery { remoteDataSource.searchTracks(any()) } returns NetworkResult.Success(listOf(dto))

        // When
        val emissions = repository.searchTracks("song", page = 1).toList()

        // Then — last emission has the remote-merged track
        val last = emissions.last() as SearchResult.Success
        assertEquals(1, last.tracks.size)
        verify { trackCache.addAll(any()) }
    }

    @Test
    fun `page 1 emits Error when remote call fails`() = runTest {
        // Given
        every { trackCache.search(any()) } returns emptyList()
        val error = RuntimeException("API error")
        coEvery { remoteDataSource.searchTracks(any()) } returns NetworkResult.Error(error)

        // When
        val emissions = repository.searchTracks("queen", page = 1).toList()

        // Then
        val last = emissions.last()
        assertTrue(last is SearchResult.Error)
        assertEquals(error, (last as SearchResult.Error).throwable)
    }

    @Test
    fun `page 2 slices cache without calling remote`() = runTest {
        // Given — cache has 25 tracks (PAGE_SIZE = 20)
        val cachedTracks = (1..25).map { makeTrack(id = it.toLong()) }
        every { trackCache.search(any()) } returns cachedTracks

        // When
        val emissions = repository.searchTracks("song", page = 2).toList()

        // Then — offset = 20, so tracks 21-25 are returned
        val result = emissions.single() as SearchResult.Success
        assertEquals(cachedTracks.drop(20), result.tracks)
        assertFalse(result.hasMore)
    }

    @Test
    fun `toDomain returns null and filters out tracks with blank name`() = runTest {
        // Given
        every { trackCache.search(any()) } returns emptyList()
        val blankNameDto = makeDto(id = 1L, name = "  ")
        val validDto = makeDto(id = 2L, name = "Valid Track")
        coEvery { remoteDataSource.searchTracks(any()) } returns NetworkResult.Success(listOf(blankNameDto, validDto))
        every { trackCache.search(any()) } returnsMany listOf(emptyList(), listOf(makeTrack(id = 2L, name = "Valid Track")))

        // When
        val emissions = repository.searchTracks("valid", page = 1).toList()

        // Then — only the valid track is added to cache
        verify { trackCache.addAll(match { tracks -> tracks.none { it.name.isBlank() } }) }
    }

    @Test
    fun `hasMore is true when there are more tracks beyond page size`() = runTest {
        // Given — 21 tracks in cache (PAGE_SIZE = 20)
        val cachedTracks = (1..21).map { makeTrack(id = it.toLong()) }
        every { trackCache.search(any()) } returnsMany listOf(emptyList(), cachedTracks)
        coEvery { remoteDataSource.searchTracks(any()) } returns NetworkResult.Success(emptyList())

        // When
        val emissions = repository.searchTracks("song", page = 1).toList()

        // Then
        val last = emissions.last() as SearchResult.Success
        assertTrue(last.hasMore)
    }
}
