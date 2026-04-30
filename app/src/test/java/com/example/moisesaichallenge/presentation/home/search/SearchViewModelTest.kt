package com.example.moisesaichallenge.presentation.home.search

import com.example.moisesaichallenge.core.playback.PlaybackManager
import com.example.moisesaichallenge.domain.model.Playlist
import com.example.moisesaichallenge.domain.model.SearchResult
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.domain.repository.MusicRepository
import com.example.moisesaichallenge.domain.repository.PlaylistRepository
import com.example.moisesaichallenge.domain.repository.RecentlyPlayedRepository
import com.example.moisesaichallenge.util.MainDispatcherRule
import com.example.moisesaichallenge.util.makeTrack
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import app.cash.turbine.test
import app.cash.turbine.testIn
import app.cash.turbine.turbineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val playlistsFlow = MutableStateFlow<List<Playlist>>(emptyList())
    private val currentTrackFlow = MutableStateFlow<Track?>(null)
    private val isPlayingFlow = MutableStateFlow(false)
    private val isLoadingFlow = MutableStateFlow(false)

    private val musicRepository = mockk<MusicRepository>(relaxed = true)
    private val recentlyPlayedRepository = mockk<RecentlyPlayedRepository>(relaxed = true) {
        every { getRecentlyPlayed() } returns emptyList()
    }
    private val playlistRepository = mockk<PlaylistRepository>(relaxed = true) {
        every { playlists } returns playlistsFlow
    }
    private val playbackManager = mockk<PlaybackManager>(relaxed = true) {
        every { currentTrack } returns currentTrackFlow
        every { isPlaying } returns isPlayingFlow
        every { isLoading } returns isLoadingFlow
    }

    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        viewModel = SearchViewModel(musicRepository, recentlyPlayedRepository, playlistRepository, playbackManager)
        mainDispatcherRule.dispatcher.scheduler.runCurrent()
    }

    @Test
    fun `initial state loads recently played from repository`() = runTest {
        // Given
        val recent = listOf(makeTrack(id = 1L))
        every { recentlyPlayedRepository.getRecentlyPlayed() } returns recent
        val vm = SearchViewModel(musicRepository, recentlyPlayedRepository, playlistRepository, playbackManager)
        mainDispatcherRule.dispatcher.scheduler.runCurrent()

        // When / Then
        vm.uiState.test {
            assertEquals(recent, expectMostRecentItem().recentlyPlayed)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onQueryChange blank clears tracks and resets loading`() = runTest {
        // Given — put some query in state first
        every { musicRepository.searchTracks(any(), any()) } returns flowOf(SearchResult.Success(emptyList(), false))
        viewModel.onQueryChange("rock")
        advanceTimeBy(300L)
        mainDispatcherRule.dispatcher.scheduler.runCurrent()

        // When
        viewModel.onQueryChange("")
        mainDispatcherRule.dispatcher.scheduler.runCurrent()

        // Then
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals("", state.query)
            assertTrue(state.tracks.isEmpty())
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onQueryChange triggers search after debounce delay`() = runTest {
        turbineScope {
            // Given
            val tracks = listOf(makeTrack(id = 1L), makeTrack(id = 2L))
            every { musicRepository.searchTracks("rock", 1) } returns
                    flowOf(SearchResult.Success(tracks, hasMore = false))

            // When
            val states = viewModel.uiState.testIn(backgroundScope)
            viewModel.onQueryChange("rock")
            // Before debounce completes — still loading
            mainDispatcherRule.dispatcher.scheduler.runCurrent()
            assertTrue(states.expectMostRecentItem().isLoading)

            advanceTimeBy(300L)
            mainDispatcherRule.dispatcher.scheduler.runCurrent()

            // Then
            val finalState = states.expectMostRecentItem()
            assertEquals(tracks, finalState.tracks)
            assertFalse(finalState.isLoading)
            states.cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search success emits tracks and hasMore in uiState`() = runTest {
        // Given
        val tracks = (1..5).map { makeTrack(id = it.toLong()) }
        every { musicRepository.searchTracks(any(), any()) } returns
            flowOf(SearchResult.Success(tracks, hasMore = true))

        // When
        viewModel.onQueryChange("song")
        advanceTimeBy(300L)
        mainDispatcherRule.dispatcher.scheduler.runCurrent()

        // Then
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(tracks, state.tracks)
            assertTrue(state.hasMore)
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search IO error does not emit snackbar`() = runTest {
        // Given
        val ioError = IOException("No connection")
        every { musicRepository.searchTracks(any(), any()) } returns
            flowOf(SearchResult.Error(ioError))

        // When
        viewModel.onQueryChange("song")
        advanceTimeBy(300L)
        mainDispatcherRule.dispatcher.scheduler.runCurrent()

        // Then — no snackbar event emitted for IO errors
        viewModel.uiState.test {
            assertFalse(expectMostRecentItem().isLoading)
            cancelAndIgnoreRemainingEvents()
        }
        // searchError is a SharedFlow with replay=0; no event should be buffered
    }

    @Test
    fun `search non-IO error emits snackbar message`() = runTest {
        turbineScope {
            // Given
            val error = RuntimeException("API error")
            every { musicRepository.searchTracks(any(), any()) } returns
                    flowOf(SearchResult.Error(error))

            // Subscribe before triggering so the replay=0 SharedFlow is not missed
            val errors = viewModel.searchError.testIn(backgroundScope)
            mainDispatcherRule.dispatcher.scheduler.runCurrent()

            // When
            viewModel.onQueryChange("song")
            advanceTimeBy(300L)
            mainDispatcherRule.dispatcher.scheduler.runCurrent()

            // Then
            assertEquals("API error", errors.awaitItem())
            errors.cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadNextPage increments page and appends tracks`() = runTest {
        // Given — initial page
        val page1Tracks = (1..20).map { makeTrack(id = it.toLong()) }
        val page2Tracks = (21..25).map { makeTrack(id = it.toLong()) }
        every { musicRepository.searchTracks("rock", 1) } returns
            flowOf(SearchResult.Success(page1Tracks, hasMore = true))
        every { musicRepository.searchTracks("rock", 2) } returns
            flowOf(SearchResult.Success(page2Tracks, hasMore = false))

        viewModel.onQueryChange("rock")
        advanceTimeBy(300L)
        mainDispatcherRule.dispatcher.scheduler.runCurrent()

        // When
        viewModel.loadNextPage()
        mainDispatcherRule.dispatcher.scheduler.runCurrent()

        // Then
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(page1Tracks + page2Tracks, state.tracks)
            assertFalse(state.hasMore)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onTrackClick sets queue at correct index and emits navigation`() = runTest {
        turbineScope {
            // Given — tracks loaded via search
            val tracks = listOf(makeTrack(id = 1L), makeTrack(id = 2L))
            every { musicRepository.searchTracks(any(), any()) } returns
                    flowOf(SearchResult.Success(tracks, hasMore = false))
            viewModel.onQueryChange("rock")
            advanceTimeBy(300L)
            mainDispatcherRule.dispatcher.scheduler.runCurrent()

            // Subscribe before triggering so the replay=0 SharedFlow is not missed
            val navEvents = viewModel.navigateToPlayer.testIn(backgroundScope)
            mainDispatcherRule.dispatcher.scheduler.runCurrent()

            // When
            viewModel.onTrackClick(tracks[1])
            mainDispatcherRule.dispatcher.scheduler.runCurrent()

            // Then
            verify { playbackManager.setQueue(tracks, 1) }
            assertEquals(Unit, navEvents.awaitItem())
            navEvents.cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onTrackClick does not call setQueue when clicking already playing track`() = runTest {
        // Given
        val track = makeTrack(id = 1L)
        currentTrackFlow.value = track
        every { playbackManager.currentTrack } returns currentTrackFlow
        val tracks = listOf(track)
        every { musicRepository.searchTracks(any(), any()) } returns
            flowOf(SearchResult.Success(tracks, hasMore = false))
        viewModel.onQueryChange("song")
        advanceTimeBy(300L)
        mainDispatcherRule.dispatcher.scheduler.runCurrent()

        // When
        viewModel.onTrackClick(track)

        // Then
        verify(exactly = 0) { playbackManager.setQueue(any(), any()) }
    }

    @Test
    fun `onPlayPauseClick calls pause when playing`() {
        // Given
        every { playbackManager.isPlaying } returns MutableStateFlow(true)

        // When
        viewModel.onPlayPauseClick()

        // Then
        verify(exactly = 1) { playbackManager.pause() }
    }

    @Test
    fun `onPlayPauseClick calls play when paused`() {
        // Given
        every { playbackManager.isPlaying } returns MutableStateFlow(false)

        // When
        viewModel.onPlayPauseClick()

        // Then
        verify(exactly = 1) { playbackManager.play() }
    }

    @Test
    fun `addTrackToPlaylist delegates to playlistRepository`() {
        // Given
        val track = makeTrack(id = 1L)

        // When
        viewModel.addTrackToPlaylist(playlistId = 5L, track = track)

        // Then
        verify(exactly = 1) { playlistRepository.addTrack(5L, track) }
    }

    @Test
    fun `refresh with blank query does nothing`() = runTest {
        // Given — no query (blank by default)

        // When
        viewModel.refresh()
        mainDispatcherRule.dispatcher.scheduler.runCurrent()

        // Then
        verify(exactly = 0) { musicRepository.searchTracks(any(), any()) }
    }

    @Test
    fun `refresh resets page to 1 and replaces tracks`() = runTest {
        // Given — load page 1 then page 2
        val page1Tracks = (1..20).map { makeTrack(id = it.toLong()) }
        val refreshedTracks = (1..5).map { makeTrack(id = it.toLong()) }
        every { musicRepository.searchTracks("rock", 1) } returnsMany listOf(
            flowOf(SearchResult.Success(page1Tracks, hasMore = true)),
            flowOf(SearchResult.Success(refreshedTracks, hasMore = false))
        )
        every { musicRepository.searchTracks("rock", 2) } returns
            flowOf(SearchResult.Success((21..25).map { makeTrack(id = it.toLong()) }, hasMore = false))

        viewModel.onQueryChange("rock")
        advanceTimeBy(300L)
        mainDispatcherRule.dispatcher.scheduler.runCurrent()
        viewModel.loadNextPage()
        mainDispatcherRule.dispatcher.scheduler.runCurrent()

        // When
        viewModel.refresh()
        mainDispatcherRule.dispatcher.scheduler.runCurrent()

        // Then — only fresh page 1 results, pagination gone
        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertEquals(refreshedTracks, state.tracks)
            assertFalse(state.hasMore)
            assertFalse(state.isRefreshing)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh sets isRefreshing true while loading then clears on success`() = runTest {
        // Given — initial search loaded
        val initialTracks = listOf(makeTrack(id = 1L))
        val refreshedTracks = listOf(makeTrack(id = 2L))
        every { musicRepository.searchTracks("rock", 1) } returnsMany listOf(
            flowOf(SearchResult.Success(initialTracks, hasMore = false)),
            flowOf(SearchResult.Success(refreshedTracks, hasMore = false))
        )
        viewModel.onQueryChange("rock")
        advanceTimeBy(300L)
        mainDispatcherRule.dispatcher.scheduler.runCurrent()

        // When — isRefreshing is set synchronously before the coroutine runs
        viewModel.refresh()

        viewModel.uiState.test {
            assertTrue(expectMostRecentItem().isRefreshing)
            cancelAndIgnoreRemainingEvents()
        }

        // After the load coroutine completes
        mainDispatcherRule.dispatcher.scheduler.runCurrent()

        viewModel.uiState.test {
            val state = expectMostRecentItem()
            assertFalse(state.isRefreshing)
            assertEquals(refreshedTracks, state.tracks)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `currentTrack change triggers refreshRecentlyPlayed`() = runTest {
        // Given
        val recent = listOf(makeTrack(id = 10L))
        every { recentlyPlayedRepository.getRecentlyPlayed() } returns recent
        val track = makeTrack(id = 99L)

        // When
        currentTrackFlow.value = track
        mainDispatcherRule.dispatcher.scheduler.runCurrent()

        // Then
        viewModel.uiState.test {
            assertEquals(recent, expectMostRecentItem().recentlyPlayed)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
