package com.example.moisesaichallenge.core.playback

import android.content.Context
import android.media.MediaPlayer
import com.example.moisesaichallenge.domain.model.Track
import com.example.moisesaichallenge.domain.repository.RecentlyPlayedRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recentlyPlayedRepository: RecentlyPlayedRepository
) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var mediaPlayer: MediaPlayer? = null
    private var positionUpdateJob: Job? = null

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    private val _currentIndex = MutableStateFlow(-1)
    private val _isPlaying = MutableStateFlow(false)
    private val _positionMs = MutableStateFlow(0L)
    private val _durationMs = MutableStateFlow(0L)
    private val _isRepeat = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)

    val currentTrack: StateFlow<Track?> = combine(_queue, _currentIndex) { queue, index ->
        queue.getOrNull(index)
    }.stateIn(scope, SharingStarted.Eagerly, null)

    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    val positionMs: StateFlow<Long> = _positionMs.asStateFlow()
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()
    val isRepeat: StateFlow<Boolean> = _isRepeat.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun setQueue(tracks: List<Track>, startIndex: Int) {
        _queue.value = tracks
        loadTrack(startIndex)
        PlaybackService.start(context)
    }

    fun setQueueKeepingCurrent(tracks: List<Track>, startIndex: Int) {
        val clickedId = tracks.getOrNull(startIndex)?.id
        val alreadyCurrent = currentTrack.value?.id == clickedId
        _queue.value = tracks
        if (alreadyCurrent) {
            _currentIndex.value = startIndex
        } else {
            loadTrack(startIndex)
        }
        PlaybackService.start(context)
    }

    fun play() {
        if (_isLoading.value) return
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                _isPlaying.value = true
                startPositionUpdates()
            }
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
                positionUpdateJob?.cancel()
            }
        }
    }

    fun seekTo(ms: Long) {
        mediaPlayer?.seekTo(ms.toInt())
        _positionMs.value = ms
    }

    fun skipNext() {
        val next = _currentIndex.value + 1
        if (next < _queue.value.size) loadTrack(next)
    }

    fun skipPrevious() {
        if (_positionMs.value <= 2_000L) {
            val prev = _currentIndex.value - 1
            if (prev >= 0) loadTrack(prev) else restart()
        } else {
            restart()
        }
    }

    fun toggleRepeat() {
        _isRepeat.value = !_isRepeat.value
    }

    private fun restart() {
        mediaPlayer?.seekTo(0)
        _positionMs.value = 0L
        if (!_isPlaying.value) {
            mediaPlayer?.start()
            _isPlaying.value = true
            startPositionUpdates()
        }
    }

    private fun loadTrack(index: Int) {
        val track = _queue.value.getOrNull(index) ?: return
        val url = track.previewUrl

        _currentIndex.value = index
        recentlyPlayedRepository.recordPlayed(track)
        _positionMs.value = 0L
        _isPlaying.value = false
        positionUpdateJob?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null

        if (url == null) {
            _isLoading.value = false
            _durationMs.value = 0L
            return
        }

        _isLoading.value = true

        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            setOnPreparedListener { mp ->
                _durationMs.value = mediaPlayer?.duration?.toLong() ?: 0
                _isLoading.value = false
                mediaPlayer?.start()
                _isPlaying.value = true
                startPositionUpdates()
            }
            setOnCompletionListener { mp ->
                if (_isRepeat.value) {
                    mp.seekTo(0)
                    mp.start()
                    _positionMs.value = 0L
                    startPositionUpdates()
                } else {
                    _isPlaying.value = false
                    positionUpdateJob?.cancel()
                    val next = _currentIndex.value + 1
                    if (next < _queue.value.size) loadTrack(next)
                }
            }
            setOnErrorListener { a, b, c ->
                _isLoading.value = false
                _isPlaying.value = false
                true
            }
            prepareAsync()
        }
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = scope.launch {
            while (isActive) {
                delay(500)
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) _positionMs.value = mp.currentPosition.toLong()
                }
            }
        }
    }
}
