// file: app/src/main/java/com/example/active/SongInfoFetcher.kt
package com.example.active

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class SongInfoFetcher {
    /** Fetches song info from a JSON stream */
    suspend fun fetch(url: String): SongInfo = withContext(Dispatchers.IO) {
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            val text = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(text)
            return@withContext SongInfo(
                title = json.optString("title", "Unknown"),
                artist = json.optString("artist", "Unknown")
            )
        } finally {
            conn.disconnect()
        }
    }
}
