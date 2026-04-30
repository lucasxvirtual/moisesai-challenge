package com.example.moisesaichallenge.data.repository

import com.example.moisesaichallenge.core.network.NetworkResult
import com.example.moisesaichallenge.data.local.AlbumLocalDataSource
import com.example.moisesaichallenge.data.network.datasource.AlbumRemoteDataSource
import com.example.moisesaichallenge.data.network.model.AlbumItemDto
import com.example.moisesaichallenge.data.network.model.AlbumLookupResponseDto
import com.example.moisesaichallenge.domain.model.Album
import com.example.moisesaichallenge.domain.model.AlbumResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AlbumRepositoryImplTest {

    private val remoteDataSource = mockk<AlbumRemoteDataSource>()
    private val albumLocalDataSource = mockk<AlbumLocalDataSource>(relaxed = true)
    private lateinit var repository: AlbumRepositoryImpl

    @Before
    fun setUp() {
        repository = AlbumRepositoryImpl(remoteDataSource, albumLocalDataSource)
    }

    private fun makeCollectionDto(id: Long = 1L, name: String = "Album") = AlbumItemDto(
        wrapperType = "collection",
        collectionId = id,
        collectionName = name,
        artistName = "Artist",
        artworkUrl = "https://example.com/art100x100bb.jpg"
    )

    private fun makeTrackDto(id: Long = 10L, name: String = "Track") = AlbumItemDto(
        wrapperType = "track",
        trackId = id,
        trackName = name,
        artistName = "Artist",
        collectionId = 1L,
        trackNumber = 1,
        durationMillis = 30_000L
    )

    private fun makeAlbum(id: Long = 1L) = Album(
        id = id, name = "Album", artistName = "Artist",
        artworkUrl = null, artworkUrlHd = null, tracks = emptyList()
    )

    @Test
    fun `getAlbum returns cached album without calling remote`() = runTest {
        // Given
        val cached = makeAlbum(id = 42L)
        every { albumLocalDataSource.get(42L) } returns cached

        // When
        val result = repository.getAlbum(42L)

        // Then
        assertTrue(result is AlbumResult.Success)
        assertEquals(cached, (result as AlbumResult.Success).album)
        coVerify(exactly = 0) { remoteDataSource.lookupAlbum(any()) }
    }

    @Test
    fun `getAlbum fetches from remote and caches album on cache miss`() = runTest {
        // Given
        every { albumLocalDataSource.get(any()) } returns null
        val response = AlbumLookupResponseDto(
            results = listOf(makeCollectionDto(id = 1L), makeTrackDto(id = 10L))
        )
        coEvery { remoteDataSource.lookupAlbum(1L) } returns NetworkResult.Success(response)

        // When
        val result = repository.getAlbum(1L)

        // Then
        assertTrue(result is AlbumResult.Success)
        val album = (result as AlbumResult.Success).album
        assertEquals(1L, album.id)
        assertEquals("Album", album.name)
        assertEquals(1, album.tracks.size)
        verify { albumLocalDataSource.put(album) }
    }

    @Test
    fun `getAlbum maps HD artwork URL from 100x100bb to 600x600bb`() = runTest {
        // Given
        every { albumLocalDataSource.get(any()) } returns null
        val response = AlbumLookupResponseDto(
            results = listOf(makeCollectionDto(id = 1L))
        )
        coEvery { remoteDataSource.lookupAlbum(1L) } returns NetworkResult.Success(response)

        // When
        val result = repository.getAlbum(1L) as AlbumResult.Success

        // Then
        assertEquals("https://example.com/art600x600bb.jpg", result.album.artworkUrlHd)
    }

    @Test
    fun `getAlbum returns Error when remote call fails`() = runTest {
        // Given
        every { albumLocalDataSource.get(any()) } returns null
        val error = RuntimeException("Server down")
        coEvery { remoteDataSource.lookupAlbum(any()) } returns NetworkResult.Error(error)

        // When
        val result = repository.getAlbum(1L)

        // Then
        assertTrue(result is AlbumResult.Error)
        assertEquals(error, (result as AlbumResult.Error).throwable)
    }

    @Test
    fun `getAlbum returns Error when response has no collection item`() = runTest {
        // Given
        every { albumLocalDataSource.get(any()) } returns null
        val response = AlbumLookupResponseDto(results = listOf(makeTrackDto()))
        coEvery { remoteDataSource.lookupAlbum(any()) } returns NetworkResult.Success(response)

        // When
        val result = repository.getAlbum(1L)

        // Then
        assertTrue(result is AlbumResult.Error)
    }

    @Test
    fun `getAlbum sorts tracks by track number`() = runTest {
        // Given
        every { albumLocalDataSource.get(any()) } returns null
        val track2 = makeTrackDto(id = 2L, name = "Second").copy(trackNumber = 2)
        val track1 = makeTrackDto(id = 1L, name = "First").copy(trackNumber = 1)
        val response = AlbumLookupResponseDto(results = listOf(makeCollectionDto(), track2, track1))
        coEvery { remoteDataSource.lookupAlbum(any()) } returns NetworkResult.Success(response)

        // When
        val result = repository.getAlbum(1L) as AlbumResult.Success

        // Then — tracks sorted by track number regardless of order in response
        assertEquals("First", result.album.tracks[0].name)
        assertEquals("Second", result.album.tracks[1].name)
    }

    @Test
    fun `getAlbum filters out tracks with blank name`() = runTest {
        // Given
        every { albumLocalDataSource.get(any()) } returns null
        val blankNameTrack = makeTrackDto(id = 99L, name = "  ")
        val validTrack = makeTrackDto(id = 1L, name = "Valid")
        val response = AlbumLookupResponseDto(results = listOf(makeCollectionDto(), blankNameTrack, validTrack))
        coEvery { remoteDataSource.lookupAlbum(any()) } returns NetworkResult.Success(response)

        // When
        val result = repository.getAlbum(1L) as AlbumResult.Success

        // Then
        assertEquals(1, result.album.tracks.size)
        assertEquals("Valid", result.album.tracks[0].name)
    }
}
