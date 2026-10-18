package com.winter.wplayer

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlaybackService: MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private val player: ExoPlayer by lazy { ExoPlayer.Builder(this).build() }

    private val listener = object: Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            IndexHolder.update(player.currentMediaItemIndex)
            Log.d(TAG, "index ${IndexHolder.songIndex}")
        }
    }
    private val TAG = "Service"
    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSession.Builder(this, player).build()
        mediaSession?.player?.addListener(listener)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
object IndexHolder {
    private val _songIndex = MutableStateFlow(0)
    val songIndex = _songIndex.asStateFlow()

    fun update(value: Int){
        _songIndex.value = value
    }
}