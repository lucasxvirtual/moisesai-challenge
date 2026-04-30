package com.example.moisesaichallenge.presentation.home.playlists.details

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.turbineScope
import com.example.moisesaichallenge.core.playback.PlaybackManager
import com.example.moisesaichallenge.domain.model.Playlist
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.domain.repository.PlaylistRepository
import com.example.moisesaichallenge.util.MainDispatcherRule
import com.example.moisesaichallenge.util.makePlaylist
import com.example.moisesaichallenge.util.makeTrack
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class PlaylistDetailViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val playlistId = 1L
    private val playlistsFlow = MutableStateFlow<List<Playlist>>(emptyList())
    private val currentTrackFlow = MutableStateFlow<Track?>(null)
    private val isPlayingFlow = MutableStateFlow(false)
    private val isLoadingFlow = MutableStateFlow(false)

    private val playlistRepository = mockk<PlaylistRepository>(relaxed = true) {
        every { playlists } returns playlistsFlow
    }
    private val playbackManager = mockk<PlaybackManager>(relaxed = true) {
        every { currentTrack } returns currentTrackFlow
        every { isPlaying } returns isPlayingFlow
        every { isLoading } returns isLoadingFlow
    }

    private fun buildViewModel(): PlaylistDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("playlistId" to playlistId))
        return PlaylistDetailViewModel(savedStateHandle, playlistRepository, playbackManager)
    }

    @Test
    fun `uiState reflects playlist from repository`() {
        // Given
        val playlist = makePlaylist(id = playlistId, name = "My Playlist")
        val viewModel = buildViewModel()

        // When
        playlistsFlow.value = listOf(playlist)

        // Then
        assertEquals(playlist, viewModel.uiState.value.playlist)
    }

    @Test
    fun `uiState playlist is null when playlist id is not in repository`() {
        // Given
        val viewModel = buildViewModel()
        playlistsFlow.value = listOf(makePlaylist(id = 99L))

        // When / Then
        assertNull(viewModel.uiState.value.playlist)
    }

    @Test
    fun `onTrackClick sets queue at correct index and emits navigation`() = runTest {
        turbineScope {
            // Given
            val tracks = listOf(makeTrack(id = 1L), makeTrack(id = 2L), makeTrack(id = 3L))
            playlistsFlow.value = listOf(makePlaylist(id = playlistId, tracks = tracks))
            val viewModel = buildViewModel()
            val turbine = viewModel.navigateToPlayer.testIn(backgroundScope)

            // When
            viewModel.onTrackClick(tracks[2])

            // Then
            verify { playbackManager.setQueueKeepingCurrent(tracks, 2) }
            assertEquals(Unit, turbine.awaitItem())
        }
    }

    @Test
    fun `onTrackClick does nothing when playlist is null`() {
        // Given — no playlist loaded
        val viewModel = buildViewModel()

        // When
        viewModel.onTrackClick(makeTrack(id = 1L))

        // Then
        verify(exactly = 0) { playbackManager.setQueueKeepingCurrent(any(), any()) }
    }

    @Test
    fun `onPlayPauseClick calls pause when playing`() {
        // Given
        isPlayingFlow.value = true
        val viewModel = buildViewModel()

        // When
        viewModel.onPlayPauseClick()

        // Then
        verify(exactly = 1) { playbackManager.pause() }
    }

    @Test
    fun `onPlayPauseClick calls play when paused`() {
        // Given
        isPlayingFlow.value = false
        val viewModel = buildViewModel()

        // When
        viewModel.onPlayPauseClick()

        // Then
        verify(exactly = 1) { playbackManager.play() }
    }

    @Test
    fun `deletePlaylist calls repository delete and emits navigate back`() = runTest {
        turbineScope {
            // Given
            val viewModel = buildViewModel()
            val turbine = viewModel.navigateBack.testIn(backgroundScope)

            // When
            viewModel.deletePlaylist()

            // Then
            verify(exactly = 1) { playlistRepository.delete(playlistId) }
            assertEquals(Unit, turbine.awaitItem())
        }
    }

    @Test
    fun `currentTrack changes are reflected in uiState`() {
        // Given
        val track = makeTrack(id = 7L)
        val viewModel = buildViewModel()

        // When
        currentTrackFlow.value = track

        // Then
        assertEquals(track.id, viewModel.uiState.value.currentTrackId)
    }

    @Test
    fun `isPlaying changes are reflected in uiState`() {
        // Given
        val viewModel = buildViewModel()

        // When
        isPlayingFlow.value = true

        // Then
        assertEquals(true, viewModel.uiState.value.isPlaying)
    }
}
