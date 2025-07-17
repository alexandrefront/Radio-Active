package com.radio.active

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.active.RadioService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.active.SongInfoFetcher
import com.example.active.SongInfo




class MainViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
    }

    private val _artist = MutableLiveData<String>("—")
    val artist: LiveData<String> = _artist

    private val _title = MutableLiveData<String>("—")
    val title: LiveData<String> = _title

    private val _isPlaying = MutableLiveData<Boolean>(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    /**
     * Called when user taps play/pause button.
     * Sends intent to RadioService and updates state.
     */
    fun togglePlayPause() {
        val context = getApplication<Application>().applicationContext
        val play = _isPlaying.value != true
        val action = if (play) ACTION_PLAY else ACTION_PAUSE
        val intent = Intent(context, RadioService::class.java)
            .setAction(action)
        ContextCompat.startForegroundService(context, intent)
        _isPlaying.value = play
    }

    /**
     * Update song info when broadcast received.
     */
    fun updateSongInfo(artist: String, title: String) {
        _artist.value = artist
        _title.value = title
    }

    /**
     * Periodically refreshes song info every 10 seconds.
     */
    fun startRefreshingInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                SongInfoFetcher.fetch(getApplication())
                delay(10_000)
            }
        }
    }
}
