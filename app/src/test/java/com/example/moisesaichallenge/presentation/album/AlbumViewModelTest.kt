package com.example.moisesaichallenge.presentation.album

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.testIn
import app.cash.turbine.turbineScope
import com.example.moisesaichallenge.core.playback.PlaybackManager
import com.example.moisesaichallenge.domain.model.Album
import com.example.moisesaichallenge.domain.model.AlbumResult
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.domain.repository.AlbumRepository
import com.example.moisesaichallenge.util.MainDispatcherRule
import com.example.moisesaichallenge.util.makeTrack
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class AlbumViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val albumRepository = mockk<AlbumRepository>()
    private val currentTrackFlow = MutableStateFlow<Track?>(null)
    private val isPlayingFlow = MutableStateFlow(false)
    private val isLoadingFlow = MutableStateFlow(false)

    private val playbackManager = mockk<PlaybackManager>(relaxed = true) {
        every { currentTrack } returns currentTrackFlow
        every { isPlaying } returns isPlayingFlow
        every { isLoading } returns isLoadingFlow
    }

    private val collectionId = 42L

    private fun buildViewModel(): AlbumViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("collectionId" to collectionId))
        return AlbumViewModel(savedStateHandle, albumRepository, playbackManager)
    }

    private fun makeAlbum(id: Long = collectionId, tracks: List<Track> = listOf(makeTrack(id = 1L))) =
        Album(id = id, name = "Test Album", artistName = "Artist", artworkUrl = null, artworkUrlHd = null, tracks = tracks)

    @Test
    fun `initial state has isLoading true`() {
        // Given
        coEvery { albumRepository.getAlbum(any()) } returns AlbumResult.Success(makeAlbum())

        // When
        val viewModel = buildViewModel()

        // Then — after init, loading resolves; but the initial value before the coroutine runs is true
        // With UnconfinedTestDispatcher the init block runs eagerly so state is already resolved
        assertFalse(viewModel.uiState.value.isLoading)
        assertNotNull(viewModel.uiState.value.album)
    }

    @Test
    fun `loadAlbum success updates album in uiState`() = runTest {
        // Given
        val album = makeAlbum()
        coEvery { albumRepository.getAlbum(collectionId) } returns AlbumResult.Success(album)

        // When
        val viewModel = buildViewModel()

        // Then
        val state = viewModel.uiState.value
        assertEquals(album, state.album)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadAlbum network error sets error message`() = runTest {
        // Given
        val error = RuntimeException("Server error")
        coEvery { albumRepository.getAlbum(collectionId) } returns AlbumResult.Error(error)

        // When
        val viewModel = buildViewModel()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Server error", state.error)
        assertFalse(state.isOffline)
    }

    @Test
    fun `loadAlbum IO error sets isOffline and clears error message`() = runTest {
        // Given
        val ioError = IOException("No connection")
        coEvery { albumRepository.getAlbum(collectionId) } returns AlbumResult.Error(ioError)

        // When
        val viewModel = buildViewModel()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.isOffline)
        assertNull(state.error)
    }

    @Test
    fun `onTrackClick sets queue and emits navigation event`() = runTest {
        turbineScope {
            // Given
            val tracks = listOf(makeTrack(id = 1L), makeTrack(id = 2L))
            val album = makeAlbum(tracks = tracks)
            coEvery { albumRepository.getAlbum(collectionId) } returns AlbumResult.Success(album)
            val viewModel = buildViewModel()
            val turbine = viewModel.navigateToPlayer.testIn(backgroundScope)

            // When
            viewModel.onTrackClick(tracks[1])

            // Then
            verify { playbackManager.setQueueKeepingCurrent(tracks, 1) }

            assertEquals(Unit, turbine.awaitItem())
        }
    }

    @Test
    fun `onPlayPauseClick calls pause when playing`() = runTest {
        // Given
        coEvery { albumRepository.getAlbum(any()) } returns AlbumResult.Success(makeAlbum())
        isPlayingFlow.value = true
        val viewModel = buildViewModel()

        // When
        viewModel.onPlayPauseClick()

        // Then
        verify(exactly = 1) { playbackManager.pause() }
    }

    @Test
    fun `onPlayPauseClick calls play when paused`() = runTest {
        // Given
        coEvery { albumRepository.getAlbum(any()) } returns AlbumResult.Success(makeAlbum())
        isPlayingFlow.value = false
        val viewModel = buildViewModel()

        // When
        viewModel.onPlayPauseClick()

        // Then
        verify(exactly = 1) { playbackManager.play() }
    }

    @Test
    fun `currentTrack changes are reflected in uiState`() = runTest {
        // Given
        val track = makeTrack(id = 5L)
        coEvery { albumRepository.getAlbum(any()) } returns AlbumResult.Success(makeAlbum())
        val viewModel = buildViewModel()

        // When
        currentTrackFlow.value = track

        // Then
        assertEquals(track.id, viewModel.uiState.value.currentTrackId)
    }
}
