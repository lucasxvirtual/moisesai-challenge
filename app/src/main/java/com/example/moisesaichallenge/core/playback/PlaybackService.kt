package com.example.moisesaichallenge.core.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.media.app.NotificationCompat as MediaNotificationCompat
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.moisesaichallenge.MainActivity
import com.example.moisesaichallenge.R
import com.example.moisesaichallenge.domain.model.Track
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : Service() {

    @Inject lateinit var playbackManager: PlaybackManager

    private lateinit var notificationManager: NotificationManagerCompat
    private var mediaSession: MediaSessionCompat? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        notificationManager = NotificationManagerCompat.from(this)
        createChannel()
        initMediaSession()
        observePlayback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> playbackManager.play()
            ACTION_PAUSE -> playbackManager.pause()
            ACTION_NEXT -> playbackManager.skipNext()
            ACTION_PREVIOUS -> playbackManager.skipPrevious()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?) = null

    override fun onDestroy() {
        serviceScope.cancel()
        mediaSession?.release()
        super.onDestroy()
    }

    private fun createChannel() {
        notificationManager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Now Playing", NotificationManager.IMPORTANCE_LOW)
                .apply { setShowBadge(false) }
        )
    }

    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(this, TAG).apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() { playbackManager.play() }
                override fun onPause() { playbackManager.pause() }
                override fun onSkipToNext() { playbackManager.skipNext() }
                override fun onSkipToPrevious() { playbackManager.skipPrevious() }
                override fun onSeekTo(pos: Long) { playbackManager.seekTo(pos) }
            })
            isActive = true
        }
    }

    private fun observePlayback() {
        serviceScope.launch {
            combine(playbackManager.currentTrack, playbackManager.isPlaying, playbackManager.durationMs) { track, playing, durationMs ->
                Triple(track, playing, durationMs)
            }.collect { (track, isPlaying, durationMs) ->
                if (track == null) {
                    ServiceCompat.stopForeground(this@PlaybackService, ServiceCompat.STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    return@collect
                }
                startForeground(NOTIFICATION_ID, buildNotification(track, isPlaying, null))
                val bitmap = loadArtwork(track.artworkUrl)
                updateMediaSession(track, isPlaying, durationMs, bitmap)
                notificationManager.notify(NOTIFICATION_ID, buildNotification(track, isPlaying, bitmap))
            }
        }
    }

    private suspend fun loadArtwork(url: String?): Bitmap? {
        url ?: return null
        return try {
            val request = ImageRequest.Builder(this).data(url).allowHardware(false).size(128).build()
            (applicationContext.imageLoader.execute(request) as? SuccessResult)?.drawable?.toBitmap()
        } catch (_: Exception) { null }
    }

    private fun updateMediaSession(track: Track, isPlaying: Boolean, durationMs: Long, bitmap: Bitmap?) {
        mediaSession?.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.name)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.artistName)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, track.albumName)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, durationMs)
                .apply { bitmap?.let { putBitmap(MediaMetadataCompat.METADATA_KEY_ART, it) } }
                .build()
        )
        mediaSession?.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(
                    if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                    playbackManager.positionMs.value,
                    if (isPlaying) 1f else 0f
                )
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
                .build()
        )
    }

    private fun buildNotification(track: Track, isPlaying: Boolean, artwork: Bitmap?): Notification {
        val openApp = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_setlist)
            .setContentTitle(track.name)
            .setContentText(track.artistName)
            .setLargeIcon(artwork)
            .setContentIntent(openApp)
            .addAction(R.drawable.ic_skip_previous, "Previous", actionIntent(ACTION_PREVIOUS))
            .addAction(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                if (isPlaying) "Pause" else "Play",
                actionIntent(if (isPlaying) ACTION_PAUSE else ACTION_PLAY)
            )
            .addAction(R.drawable.ic_skip_next, "Next", actionIntent(ACTION_NEXT))
            .setStyle(
                MediaNotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession?.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .build()
    }

    private fun actionIntent(action: String): PendingIntent =
        PendingIntent.getService(
            this, action.hashCode(),
            Intent(this, PlaybackService::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    companion object {
        private const val TAG = "PlaybackService"
        const val CHANNEL_ID = "playback"
        const val NOTIFICATION_ID = 1
        const val ACTION_PLAY = "com.example.moisesaichallenge.PLAY"
        const val ACTION_PAUSE = "com.example.moisesaichallenge.PAUSE"
        const val ACTION_NEXT = "com.example.moisesaichallenge.NEXT"
        const val ACTION_PREVIOUS = "com.example.moisesaichallenge.PREVIOUS"

        fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, PlaybackService::class.java))
        }
    }
}
