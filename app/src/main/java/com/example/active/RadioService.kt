package com.example.active

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import com.example.active.NotificationUtils

class RadioService : Service() {

    companion object {
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        private const val CHANNEL_ID = "radio_channel"
        private const val NOTIFICATION_ID = 101
    }

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var connectivityManager: ConnectivityManager

    override fun onCreate() {
        super.onCreate()
        initMediaPlayer()
        initMediaSession()
        registerNetworkCallback()
    }

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build())
            setOnErrorListener { _, what, extra ->
                stopSelf()
                true
            }
        }
    }

    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(this, "RadioSession").apply {
            isActive = true
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() = handlePlay()
                override fun onPause() = handlePause()
            })
        }
    }

    private fun registerNetworkCallback() {
        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder().build()
        connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (!mediaPlayer.isPlaying) reconnectStream()
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> handlePlay()
            ACTION_PAUSE -> handlePause()
            else -> { /* ignore */ }
        }
        return START_STICKY
    }

    private fun handlePlay() {
        if (mediaPlayer.isPlaying) return

        mediaPlayer.reset()
        mediaPlayer.setDataSource("https://stream.radio-active.net:8443/active")
        mediaPlayer.setOnPreparedListener {
            it.start()
            updateMediaSessionPlayback(true)
            startForeground(NOTIFICATION_ID,
                NotificationUtils.createMediaNotification(
                    this, mediaSession, isPlaying = true
                )
            )
        }
        mediaPlayer.prepareAsync()
    }

    private fun handlePause() {
        if (!mediaPlayer.isPlaying) return

        mediaPlayer.pause()
        updateMediaSessionPlayback(false)
        startForeground(NOTIFICATION_ID,
            NotificationUtils.createMediaNotification(
                this, mediaSession, isPlaying = false
            )
        )
    }

    private fun reconnectStream() = handlePlay()

    private fun updateMediaSessionPlayback(playing: Boolean) {
        val state = if (playing) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(state, mediaPlayer.currentPosition.toLong(), 1f)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE)
                .build()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        mediaSession.release()
        connectivityManager.unregisterNetworkCallback(ConnectivityManager.NetworkCallback())
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
