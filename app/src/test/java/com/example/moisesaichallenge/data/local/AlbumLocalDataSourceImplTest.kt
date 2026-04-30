package com.example.moisesaichallenge.data.local

import com.example.moisesaichallenge.domain.model.Album
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AlbumLocalDataSourceImplTest {

    private lateinit var dataSource: AlbumLocalDataSourceImpl

    @Before
    fun setUp() {
        dataSource = AlbumLocalDataSourceImpl()
    }

    private fun makeAlbum(id: Long = 1L, name: String = "Album $id") =
        Album(id = id, name = name, artistName = "Artist", artworkUrl = null, artworkUrlHd = null, tracks = emptyList())

    @Test
    fun `get returns null for unknown id`() {
        // Given — empty cache

        // When
        val result = dataSource.get(42L)

        // Then
        assertNull(result)
    }

    @Test
    fun `put then get returns stored album`() {
        // Given
        val album = makeAlbum(id = 1L, name = "Abbey Road")

        // When
        dataSource.put(album)
        val result = dataSource.get(1L)

        // Then
        assertEquals(album, result)
    }

    @Test
    fun `put overwrites existing entry with same id`() {
        // Given
        val original = makeAlbum(id = 1L, name = "Original")
        val updated = makeAlbum(id = 1L, name = "Updated")
        dataSource.put(original)

        // When
        dataSource.put(updated)

        // Then
        assertEquals(updated, dataSource.get(1L))
    }

    @Test
    fun `put stores multiple albums independently`() {
        // Given
        val albumA = makeAlbum(id = 1L)
        val albumB = makeAlbum(id = 2L)

        // When
        dataSource.put(albumA)
        dataSource.put(albumB)

        // Then
        assertEquals(albumA, dataSource.get(1L))
        assertEquals(albumB, dataSource.get(2L))
    }
}
