package com.example.moisesaichallenge.data.repository

import com.example.moisesaichallenge.util.makePlaylist
import com.example.moisesaichallenge.util.makeTrack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PlaylistRepositoryImplTest {

    private lateinit var repository: PlaylistRepositoryImpl

    @Before
    fun setUp() {
        repository = PlaylistRepositoryImpl()
    }

    @Test
    fun `playlists starts empty`() {
        // Given — fresh repository

        // When / Then
        assertTrue(repository.playlists.value.isEmpty())
    }

    @Test
    fun `create adds playlist and returns auto-incremented id`() {
        // Given
        val tracks = listOf(makeTrack(id = 1L))

        // When
        val id = repository.create("My Playlist", tracks)

        // Then
        assertEquals(1L, id)
        val playlists = repository.playlists.value
        assertEquals(1, playlists.size)
        assertEquals("My Playlist", playlists[0].name)
        assertEquals(tracks, playlists[0].tracks)
    }

    @Test
    fun `create increments id for each new playlist`() {
        // Given / When
        val id1 = repository.create("First", emptyList())
        val id2 = repository.create("Second", emptyList())

        // Then
        assertEquals(1L, id1)
        assertEquals(2L, id2)
    }

    @Test
    fun `update changes name and tracks of existing playlist`() {
        // Given
        val id = repository.create("Old Name", emptyList())
        val newTracks = listOf(makeTrack(id = 99L))

        // When
        repository.update(id, "New Name", newTracks)

        // Then
        val updated = repository.playlists.value.find { it.id == id }
        assertEquals("New Name", updated?.name)
        assertEquals(newTracks, updated?.tracks)
    }

    @Test
    fun `update does not affect other playlists`() {
        // Given
        val id1 = repository.create("First", emptyList())
        val id2 = repository.create("Second", emptyList())

        // When
        repository.update(id1, "Updated First", emptyList())

        // Then
        val second = repository.playlists.value.find { it.id == id2 }
        assertEquals("Second", second?.name)
    }

    @Test
    fun `delete removes playlist from list`() {
        // Given
        val id = repository.create("To Delete", emptyList())

        // When
        repository.delete(id)

        // Then
        assertTrue(repository.playlists.value.none { it.id == id })
    }

    @Test
    fun `delete does not remove other playlists`() {
        // Given
        val id1 = repository.create("Keep", emptyList())
        val id2 = repository.create("Delete Me", emptyList())

        // When
        repository.delete(id2)

        // Then
        assertEquals(1, repository.playlists.value.size)
        assertEquals(id1, repository.playlists.value[0].id)
    }

    @Test
    fun `addTrack appends track to matching playlist`() {
        // Given
        val id = repository.create("My Playlist", emptyList())
        val track = makeTrack(id = 10L)

        // When
        repository.addTrack(id, track)

        // Then
        val playlist = repository.playlists.value.find { it.id == id }
        assertEquals(listOf(track), playlist?.tracks)
    }

    @Test
    fun `addTrack does not duplicate track already in playlist`() {
        // Given
        val track = makeTrack(id = 10L)
        val id = repository.create("My Playlist", listOf(track))

        // When
        repository.addTrack(id, track)

        // Then
        val playlist = repository.playlists.value.find { it.id == id }
        assertEquals(1, playlist?.tracks?.size)
    }

    @Test
    fun `addTrack does not affect other playlists`() {
        // Given
        val id1 = repository.create("Target", emptyList())
        val id2 = repository.create("Other", emptyList())
        val track = makeTrack(id = 5L)

        // When
        repository.addTrack(id1, track)

        // Then
        val other = repository.playlists.value.find { it.id == id2 }
        assertTrue(other?.tracks?.isEmpty() == true)
    }
}
