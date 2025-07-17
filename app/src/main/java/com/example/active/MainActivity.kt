package com.radio.active

import android.annotation.SuppressLint
import android.content.*
import android.os.Bundle
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.active.R
import com.example.active.RadioService

class MainActivity : AppCompatActivity() {

    private lateinit var playPauseButton: ImageButton
    private lateinit var volumeSeekBar: SeekBar
    private lateinit var artistTextView: TextView
    private lateinit var titleTextView: TextView

    private var isPlaying = false

    private val songInfoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val artist = intent.getStringExtra("artist") ?: "Unknown"
            val title = intent.getStringExtra("title") ?: "—"
            val thumbUrl = intent.getStringExtra("thumb_url")  // optional

            artistTextView.text = artist
            titleTextView.text = title

            // Optionally handle thumb updates if using an image view
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playPauseButton = findViewById(R.id.btnPlayPause)
        volumeSeekBar = findViewById(R.id.volumeSeekBar)
        artistTextView = findViewById(R.id.txtArtist)
        titleTextView = findViewById(R.id.txtTitle)

        // volume bar setup
        // ... (AudioManager initialization as earlier)

        playPauseButton.setOnClickListener {
            val action = if (isPlaying) RadioService.ACTION_PAUSE else RadioService.ACTION_PLAY
            ContextCompat.startForegroundService(
                this, Intent(this, RadioService::class.java).setAction(action)
            )
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onStart() {
        super.onStart()
        // register the receiver for updates
        registerReceiver(
            songInfoReceiver, IntentFilter("com.radio.UPDATE_SONG_INFO")
        )
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(songInfoReceiver)
    }
}
