package com.example.active

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import androidx.media.app.NotificationCompat.MediaStyle
import com.example.active.R


object NotificationUtils {

    private const val CHANNEL_ID = "radio_channel"
    private const val CHANNEL_NAME = "Radio Playback"

    fun createMediaNotification(
        context: Context,
        mediaSession: MediaSessionCompat,
        isPlaying: Boolean
    ): Notification {
        val playPauseIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            if (isPlaying) android.support.v4.media.session.PlaybackStateCompat.ACTION_PAUSE
            else android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY
        )

        val contentIntent = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val icon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play

        createNotificationChannel(context)

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(mediaSession.controller.metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE))
            .setContentText(mediaSession.controller.metadata?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST))
            .setSmallIcon(R.mipmap.ic_radio)
            .setContentIntent(contentIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(NotificationCompat.Action(icon, if (isPlaying) "Pause" else "Play", playPauseIntent))
            .setStyle(MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowActionsInCompactView(0)
            )
            .setOngoing(isPlaying)
            .build()
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
            }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(chan)
        }
    }
}

/*
private fun Unit.addAction(action: Any) {}

private fun Unit.setVisibility(visibilityPublic: Int) {
    TODO("Not yet implemented")
}
*/
