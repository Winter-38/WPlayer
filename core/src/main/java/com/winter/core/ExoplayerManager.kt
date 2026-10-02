package com.winter.core

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class ExoplayerManager(val context: Context) {

    private val _exoplayer: ExoPlayer by lazy {
        ExoPlayer.Builder(context).build()
    }

    fun play(mediaItem: MediaItem){
        _exoplayer.setMediaItem(mediaItem)
        _exoplayer.prepare()
        _exoplayer.play()
    }

    fun release() {
        _exoplayer.release()
    }

}