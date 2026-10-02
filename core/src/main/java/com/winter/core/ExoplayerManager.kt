package com.winter.core

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class ExoplayerManager(val context: Context) {
    private val _exoplayer: ExoPlayer by lazy { ExoPlayer.Builder(context).build() }

    enum class PlayerState{
        IDLE, PLAYING, PAUSED, ENDED
    }

    fun getState(): PlayerState {
        when (_exoplayer.playbackState) {
            Player.STATE_IDLE -> return PlayerState.IDLE
            Player.STATE_BUFFERING -> return PlayerState.PAUSED
            Player.STATE_READY -> if (_exoplayer.isPlaying) return PlayerState.PLAYING else return PlayerState.PAUSED
            Player.STATE_ENDED -> return PlayerState.ENDED
            else -> return PlayerState.IDLE
        }
    }
    fun play(mediaItem: MediaItem){
        _exoplayer.setMediaItem(mediaItem)
        _exoplayer.prepare()
        _exoplayer.play()
    }

    fun play(){
        _exoplayer.play()
    }

    fun pause(){
        _exoplayer.pause()
    }

    fun release() {
        _exoplayer.release()
    }

}
