package com.winter.wplayer

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService: MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private val player: ExoPlayer by lazy { ExoPlayer.Builder(this).build() }

    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSession.Builder(this, player).build()
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