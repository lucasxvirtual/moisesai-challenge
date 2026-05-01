package com.example.moisesaichallenge.presentation.player

import app.cash.turbine.testIn
import app.cash.turbine.turbineScope
import com.example.moisesaichallenge.core.playback.PlaybackManager
import com.example.moisesaichallenge.domain.model.Playlist
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.domain.repository.PlaylistRepository
import com.example.moisesaichallenge.util.MainDispatcherRule
import com.example.moisesaichallenge.util.makeTrack
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PlayerViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val currentTrackFlow = MutableStateFlow<Track?>(null)
    private val isPlayingFlow = MutableStateFlow(false)
    private val positionMsFlow = MutableStateFlow(0L)
    private val durationMsFlow = MutableStateFlow(0L)
    private val isRepeatFlow = MutableStateFlow(false)
    private val isLoadingFlow = MutableStateFlow(false)

    private val playbackManager = mockk<PlaybackManager>(relaxed = true) {
        every { currentTrack } returns currentTrackFlow
        every { isPlaying } returns isPlayingFlow
        every { positionMs } returns positionMsFlow
        every { durationMs } returns durationMsFlow
        every { isRepeat } returns isRepeatFlow
        every { isLoading } returns isLoadingFlow
    }
    private val playlistRepository = mockk<PlaylistRepository>(relaxed = true) {
        every { playlists } returns MutableStateFlow<List<Playlist>>(emptyList())
    }

    private lateinit var viewModel: PlayerViewModel

    @Before
    fun setUp() {
        viewModel = PlayerViewModel(playbackManager, playlistRepository)
    }

    @Test
    fun `uiState reflects current playback state`() = runTest {
        turbineScope {
            // Given — subscribe first so WhileSubscribed starts collecting, then set values
            val states = viewModel.uiState.testIn(backgroundScope)
            val track = makeTrack(id = 1L)
            currentTrackFlow.value = track
            isPlayingFlow.value = true
            positionMsFlow.value = 15_000L
            durationMsFlow.value = 30_000L
            isRepeatFlow.value = true

            // When / Then
            val state = states.expectMostRecentItem()
            assertEquals(track, state.track)
            assertTrue(state.isPlaying)
            assertEquals(15_000L, state.positionMs)
            assertEquals(30_000L, state.durationMs)
            assertTrue(state.isRepeat)
            states.cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState is empty when no track is playing`() = runTest {
        turbineScope {
            // Given — all flows at defaults
            val states = viewModel.uiState.testIn(backgroundScope)

            // When / Then
            val state = states.expectMostRecentItem()
            assertNull(state.track)
            assertFalse(state.isPlaying)
            assertEquals(0L, state.positionMs)
            states.cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `play delegates to playbackManager`() {
        // When
        viewModel.play()

        // Then
        verify(exactly = 1) { playbackManager.play() }
    }

    @Test
    fun `pause delegates to playbackManager`() {
        // When
        viewModel.pause()

        // Then
        verify(exactly = 1) { playbackManager.pause() }
    }

    @Test
    fun `seekTo delegates to playbackManager`() {
        // When
        viewModel.seekTo(12_000L)

        // Then
        verify(exactly = 1) { playbackManager.seekTo(12_000L) }
    }

    @Test
    fun `skipNext delegates to playbackManager`() {
        // When
        viewModel.skipNext()

        // Then
        verify(exactly = 1) { playbackManager.skipNext() }
    }

    @Test
    fun `skipPrevious delegates to playbackManager`() {
        // When
        viewModel.skipPrevious()

        // Then
        verify(exactly = 1) { playbackManager.skipPrevious() }
    }

    @Test
    fun `toggleRepeat delegates to playbackManager`() {
        // When
        viewModel.toggleRepeat()

        // Then
        verify(exactly = 1) { playbackManager.toggleRepeat() }
    }

    @Test
    fun `isLoading state is reflected in uiState`() = runTest {
        turbineScope {
            // Given — subscribe first, then trigger change
            val states = viewModel.uiState.testIn(backgroundScope)
            isLoadingFlow.value = true

            // When / Then
            assertTrue(states.expectMostRecentItem().isLoading)
            states.cancelAndIgnoreRemainingEvents()
        }
    }
}
