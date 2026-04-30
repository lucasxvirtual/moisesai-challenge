package com.example.moisesaichallenge.data.local

import com.example.moisesaichallenge.util.makeTrack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecentlyPlayedLocalDataSourceImplTest {

    private lateinit var dataSource: RecentlyPlayedLocalDataSourceImpl

    @Before
    fun setUp() {
        dataSource = RecentlyPlayedLocalDataSourceImpl()
    }

    @Test
    fun `getAll returns empty list initially`() {
        // Given — freshly created data source

        // When
        val result = dataSource.getAll()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `add inserts track at the front`() {
        // Given
        val first = makeTrack(id = 1L, name = "First")
        val second = makeTrack(id = 2L, name = "Second")

        // When
        dataSource.add(first)
        dataSource.add(second)

        // Then — most recently added is first
        assertEquals(second, dataSource.getAll()[0])
        assertEquals(first, dataSource.getAll()[1])
    }

    @Test
    fun `add does not insert duplicate track`() {
        // Given
        val track = makeTrack(id = 1L)

        // When
        dataSource.add(track)
        dataSource.add(track)

        // Then
        assertEquals(1, dataSource.getAll().size)
    }

    @Test
    fun `add keeps existing position when duplicate is added`() {
        // Given
        val trackA = makeTrack(id = 1L)
        val trackB = makeTrack(id = 2L)
        dataSource.add(trackA)
        dataSource.add(trackB) // list is [B, A]

        // When — add A again (already in list)
        dataSource.add(trackA)

        // Then — order is unchanged
        assertEquals(listOf(trackB, trackA), dataSource.getAll())
    }

    @Test
    fun `add caps list at 50 entries`() {
        // Given — fill to max
        repeat(50) { i -> dataSource.add(makeTrack(id = i.toLong())) }

        // When — add one more unique track
        val overflow = makeTrack(id = 99L)
        dataSource.add(overflow)

        // Then — size stays at 50 and overflow is at front, oldest was removed
        val result = dataSource.getAll()
        assertEquals(50, result.size)
        assertEquals(overflow, result.first())
    }

    @Test
    fun `clear empties the list`() {
        // Given
        dataSource.add(makeTrack(id = 1L))
        dataSource.add(makeTrack(id = 2L))

        // When
        dataSource.clear()

        // Then
        assertTrue(dataSource.getAll().isEmpty())
    }
}
