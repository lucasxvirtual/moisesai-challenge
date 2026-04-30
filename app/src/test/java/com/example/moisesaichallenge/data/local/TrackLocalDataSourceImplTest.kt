package com.example.moisesaichallenge.data.local

import com.example.moisesaichallenge.util.makeTrack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TrackLocalDataSourceImplTest {

    private lateinit var dataSource: TrackLocalDataSourceImpl

    @Before
    fun setUp() {
        dataSource = TrackLocalDataSourceImpl()
    }

    @Test
    fun `search returns empty when cache is empty`() {
        // Given — no tracks stored

        // When
        val result = dataSource.search("queen")

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `search matches by track name case insensitive`() {
        // Given
        val track = makeTrack(id = 1L, name = "Bohemian Rhapsody", artistName = "Queen")
        dataSource.addAll(listOf(track))

        // When
        val result = dataSource.search("bohemian")

        // Then
        assertEquals(listOf(track), result)
    }

    @Test
    fun `search matches by artist name case insensitive`() {
        // Given
        val track = makeTrack(id = 1L, name = "Somebody", artistName = "Queen")
        dataSource.addAll(listOf(track))

        // When
        val result = dataSource.search("QUEEN")

        // Then
        assertEquals(listOf(track), result)
    }

    @Test
    fun `search trims whitespace from query`() {
        // Given
        val track = makeTrack(id = 1L, name = "Let It Be", artistName = "Beatles")
        dataSource.addAll(listOf(track))

        // When
        val result = dataSource.search("  let it  ")

        // Then
        assertEquals(listOf(track), result)
    }

    @Test
    fun `search returns only matching tracks`() {
        // Given
        val match = makeTrack(id = 1L, name = "Hey Jude", artistName = "Beatles")
        val noMatch = makeTrack(id = 2L, name = "Purple Haze", artistName = "Hendrix")
        dataSource.addAll(listOf(match, noMatch))

        // When
        val result = dataSource.search("jude")

        // Then
        assertEquals(listOf(match), result)
    }

    @Test
    fun `addAll overwrites track with same id`() {
        // Given
        val original = makeTrack(id = 1L, name = "Original Title")
        dataSource.addAll(listOf(original))

        val updated = makeTrack(id = 1L, name = "Updated Title")

        // When
        dataSource.addAll(listOf(updated))

        // Then
        val result = dataSource.search("updated")
        assertEquals(1, result.size)
        assertEquals(updated, result[0])
    }

    @Test
    fun `search returns empty when query matches nothing`() {
        // Given
        dataSource.addAll(listOf(makeTrack(id = 1L, name = "Symphony", artistName = "Beethoven")))

        // When
        val result = dataSource.search("zzznomatch")

        // Then
        assertTrue(result.isEmpty())
    }
}
