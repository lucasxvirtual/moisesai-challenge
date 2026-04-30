package com.example.moisesaichallenge.presentation.home.playlists.list

import com.example.moisesaichallenge.domain.repository.PlaylistRepository
import com.example.moisesaichallenge.util.MainDispatcherRule
import com.example.moisesaichallenge.util.makePlaylist
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PlaylistsViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val playlistsFlow = MutableStateFlow(emptyList<com.example.moisesaichallenge.domain.model.Playlist>())
    private val playlistRepository = mockk<PlaylistRepository>(relaxed = true) {
        io.mockk.every { playlists } returns playlistsFlow
    }
    private lateinit var viewModel: PlaylistsViewModel

    @Before
    fun setUp() {
        viewModel = PlaylistsViewModel(playlistRepository)
    }

    @Test
    fun `playlists exposes repository state flow`() {
        // Given
        val expected = listOf(makePlaylist(id = 1L), makePlaylist(id = 2L))

        // When
        playlistsFlow.value = expected

        // Then
        assertEquals(expected, viewModel.playlists.value)
    }

    @Test
    fun `deletePlaylist delegates to repository`() {
        // Given
        val targetId = 3L

        // When
        viewModel.deletePlaylist(targetId)

        // Then
        verify(exactly = 1) { playlistRepository.delete(targetId) }
    }
}
