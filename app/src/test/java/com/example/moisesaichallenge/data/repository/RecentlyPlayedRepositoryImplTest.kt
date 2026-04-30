package com.example.moisesaichallenge.data.repository

import com.example.moisesaichallenge.data.local.RecentlyPlayedLocalDataSource
import com.example.moisesaichallenge.util.makeTrack
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RecentlyPlayedRepositoryImplTest {

    private val localDataSource = mockk<RecentlyPlayedLocalDataSource>(relaxed = true)
    private lateinit var repository: RecentlyPlayedRepositoryImpl

    @Before
    fun setUp() {
        repository = RecentlyPlayedRepositoryImpl(localDataSource)
    }

    @Test
    fun `getRecentlyPlayed delegates to localDataSource getAll`() {
        // Given
        val tracks = listOf(makeTrack(id = 1L), makeTrack(id = 2L))
        every { localDataSource.getAll() } returns tracks

        // When
        val result = repository.getRecentlyPlayed()

        // Then
        assertEquals(tracks, result)
        verify(exactly = 1) { localDataSource.getAll() }
    }

    @Test
    fun `recordPlayed delegates to localDataSource add`() {
        // Given
        val track = makeTrack(id = 5L)

        // When
        repository.recordPlayed(track)

        // Then
        verify(exactly = 1) { localDataSource.add(track) }
    }

    @Test
    fun `getRecentlyPlayed returns empty list when data source is empty`() {
        // Given
        every { localDataSource.getAll() } returns emptyList()

        // When
        val result = repository.getRecentlyPlayed()

        // Then
        assertEquals(emptyList<Any>(), result)
    }
}
