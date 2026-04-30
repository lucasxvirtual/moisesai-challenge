package com.example.moisesaichallenge.presentation.home.playlists.create

import androidx.lifecycle.SavedStateHandle
import com.example.moisesaichallenge.domain.model.Playlist
import com.example.moisesaichallenge.domain.model.SearchResult
import com.example.moisesaichallenge.domain.repository.MusicRepository
import com.example.moisesaichallenge.domain.repository.PlaylistRepository
import com.example.moisesaichallenge.domain.repository.RecentlyPlayedRepository
import com.example.moisesaichallenge.util.MainDispatcherRule
import com.example.moisesaichallenge.util.makePlaylist
import com.example.moisesaichallenge.util.makeTrack
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CreatePlaylistViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val playlistsFlow = MutableStateFlow<List<Playlist>>(emptyList())
    private val musicRepository = mockk<MusicRepository>(relaxed = true)
    private val recentlyPlayedRepository = mockk<RecentlyPlayedRepository>(relaxed = true) {
        every { getRecentlyPlayed() } returns emptyList()
    }
    private val playlistRepository = mockk<PlaylistRepository>(relaxed = true) {
        every { playlists } returns playlistsFlow
        every { create(any(), any()) } returns 42L
    }

    private fun buildViewModel(playlistId: Long = 0L): CreatePlaylistViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("playlistId" to playlistId))
        return CreatePlaylistViewModel(savedStateHandle, musicRepository, recentlyPlayedRepository, playlistRepository)
    }

    @Test
    fun `create mode has isEditMode false`() = runTest {
        // Given / When
        val viewModel = buildViewModel(playlistId = 0L)

        // Then
        assertFalse(viewModel.uiState.value.isEditMode)
    }

    @Test
    fun `edit mode has isEditMode true and loads existing playlist data`() = runTest {
        // Given
        val existingTracks = listOf(makeTrack(id = 1L), makeTrack(id = 2L))
        val playlist = makePlaylist(id = 5L, name = "Existing", tracks = existingTracks)
        playlistsFlow.value = listOf(playlist)

        // When
        val viewModel = buildViewModel(playlistId = 5L)

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.isEditMode)
        assertEquals("Existing", state.name)
        assertEquals(existingTracks.map { it.id }.toSet(), state.selectedTrackIds)
    }

    @Test
    fun `onNameChange updates name in state`() = runTest {
        // Given
        val viewModel = buildViewModel()

        // When
        viewModel.onNameChange("New Playlist")

        // Then
        assertEquals("New Playlist", viewModel.uiState.value.name)
    }

    @Test
    fun `onQueryChange blank clears tracks and stops loading`() = runTest {
        // Given
        val viewModel = buildViewModel()

        // When
        viewModel.onQueryChange("   ")

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.tracks.isEmpty())
        assertFalse(state.isLoading)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `onQueryChange non-blank triggers debounced search`() = runTest {
        // Given
        val tracks = listOf(makeTrack(id = 1L))
        every { musicRepository.searchTracks(any(), any()) } returns
            flowOf(SearchResult.Success(tracks, hasMore = false))
        val viewModel = buildViewModel()

        // When
        viewModel.onQueryChange("rock")
        advanceTimeBy(300L)
        mainDispatcherRule.dispatcher.scheduler.runCurrent()

        // Then
        assertEquals(tracks, viewModel.uiState.value.tracks)
    }

    @Test
    fun `onToggleTrack adds track to selection`() = runTest {
        // Given
        val track = makeTrack(id = 10L)
        val viewModel = buildViewModel()

        // When
        viewModel.onToggleTrack(track)

        // Then
        assertTrue(track.id in viewModel.uiState.value.selectedTrackIds)
        assertTrue(track in viewModel.uiState.value.selectedTracksList)
    }

    @Test
    fun `onToggleTrack removes already selected track`() = runTest {
        // Given
        val track = makeTrack(id = 10L)
        val viewModel = buildViewModel()
        viewModel.onToggleTrack(track) // select

        // When
        viewModel.onToggleTrack(track) // deselect

        // Then
        assertFalse(track.id in viewModel.uiState.value.selectedTrackIds)
    }

    @Test
    fun `savePlaylist in create mode calls repository create and emits new id`() = runTest {
        // Given
        val viewModel = buildViewModel()
        viewModel.onNameChange("Summer Hits")
        viewModel.onToggleTrack(makeTrack(id = 1L))

        // When
        viewModel.savePlaylist()

        // Then
        verify { playlistRepository.create("Summer Hits", any()) }
        assertEquals(42L, viewModel.created.first())
    }

    @Test
    fun `savePlaylist in edit mode calls repository update and emits 0`() = runTest {
        // Given
        val playlist = makePlaylist(id = 5L, name = "Old Name")
        playlistsFlow.value = listOf(playlist)
        val viewModel = buildViewModel(playlistId = 5L)
        viewModel.onNameChange("New Name")

        // When
        viewModel.savePlaylist()

        // Then
        verify { playlistRepository.update(5L, "New Name", any()) }
        assertEquals(0L, viewModel.created.first())
    }

    @Test
    fun `savePlaylist with blank name does nothing`() = runTest {
        // Given
        val viewModel = buildViewModel()
        // name starts blank

        // When
        viewModel.savePlaylist()

        // Then
        verify(exactly = 0) { playlistRepository.create(any(), any()) }
    }

    @Test
    fun `recently played is loaded in initial state`() = runTest {
        // Given
        val recent = listOf(makeTrack(id = 99L))
        every { recentlyPlayedRepository.getRecentlyPlayed() } returns recent

        // When
        val viewModel = buildViewModel()

        // Then
        assertEquals(recent, viewModel.uiState.value.recentlyPlayed)
    }
}
