package com.winter.wplayer

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MusicPlayerViewModel(app: Application): AndroidViewModel(app) {
    companion object {
        private val TAG = "MusicPlayerVM"
    }
    private val buildMutex = Mutex()
    private var _controller = MutableStateFlow<MediaController?>(null)
    val controller = _controller.asStateFlow()

    private val _isPlaying = MutableStateFlow<Boolean?>(false)
    val isPlaying = _isPlaying.asStateFlow()
    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode = _repeatMode.asStateFlow()
    private val _isShuffleEnabled = MutableStateFlow<Boolean?>(false)
    val isShuffleEnabled = _isShuffleEnabled.asStateFlow()


    fun play() {
        if (_controller.value != null) {
            _controller.value!!.play()
        }
    }

    fun pause() {
        if (_controller.value != null) {
            _controller.value!!.pause()
        }
    }
    fun seekToPrevious() {
        if (_controller.value != null && _controller.value!!.hasPreviousMediaItem()) {
            _controller.value!!.seekToPrevious()
        }
    }

    fun seekToNext() {
        if (_controller.value != null && _controller.value!!.hasNextMediaItem()) {
            _controller.value!!.seekToNext()
        }
    }

    fun switchRepeatMode() {
        val controller = _controller.value ?: return
        controller.repeatMode = when (controller.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun switchShuffledMode(){
        val controller = _controller.value ?: return
        controller.shuffleModeEnabled = !controller.shuffleModeEnabled
    }
    
    suspend fun buildController() {
        buildMutex.withLock {
            if (_controller.value != null) return
            val context = getApplication<Application>()
            val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
            val future = MediaController.Builder(context, sessionToken).buildAsync()
            try {
                _controller.value = future.await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to build MediaController", e)
                _isPlaying.value = false
            }
            future.addListener(
                {
                    _controller.value = future.get()
                    _isPlaying.value = _controller.value?.isPlaying
                    _controller.value?.addListener(object : Player.Listener {
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            _isPlaying.value = isPlaying
                        }

                        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                            _isShuffleEnabled.value = shuffleModeEnabled
                        }

                        override fun onRepeatModeChanged(repeatMode: Int) {
                            _repeatMode.value = repeatMode
                        }
                    })
                },
                ContextCompat.getMainExecutor(context)
            )
        }
    }

    override fun onCleared() {
        _controller.value?.release()
        _controller.value = null
    }
}