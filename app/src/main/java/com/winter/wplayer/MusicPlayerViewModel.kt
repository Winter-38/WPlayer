package com.winter.wplayer

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MusicPlayerViewModel(app: Application): AndroidViewModel(app) {
    private val _controller = MutableStateFlow<MediaController?>(null)
    val controller = _controller.asStateFlow()
    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    init {
        connect()
    }
    private fun connect() {
        val context = getApplication<Application>()
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        future.addListener({
            _controller.value = future.get()
            _isReady.value = true
        }, MoreExecutors.directExecutor())
    }

    fun play(mediaItem: MediaItem) {
        if (_controller.value != null) {
            controller.run {
                play(mediaItem)
            }
        }
    }

    fun play(mediaItems: List<MediaItem>) {
        if (_controller.value != null) {
            controller.run {
                play(mediaItems)
            }
        }
    }

    override fun onCleared() {
        _controller.value?.release()
        _controller.value = null
    }
}