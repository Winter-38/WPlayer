package com.winter.wplayer

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.guava.await

class MusicPlayerViewModel(app: Application): AndroidViewModel(app) {
    companion object {
        private val TAG = "MusicPlayerViewModel"
    }
    private val _controller = MutableStateFlow<MediaController?>(null)
    val controller = _controller.asStateFlow()

    private val _songIndex = MutableStateFlow(0)
    val songIndex = _songIndex.asStateFlow()

    private val listener = object: Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            _songIndex.value = _controller.value?.currentMediaItemIndex ?: 0
        }
    }

    private val _currentRepeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val currentRepeatMode = _currentRepeatMode.asStateFlow()
    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled = _isShuffleEnabled.asStateFlow()

    fun cycleRepeatMode() {
        val controller = _controller.value ?: return
        val repeatMode = when (controller.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        val isShuffle = if (controller.shuffleModeEnabled) true else false
        controller.repeatMode = repeatMode
        controller.shuffleModeEnabled = isShuffle
        _currentRepeatMode.value = repeatMode
        _isShuffleEnabled.value = isShuffle
    }

    suspend fun buildController() {
        val context = getApplication<Application>()
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        _controller.value = future.await()
        _controller.value?.addListener(listener)
    }

    override fun onCleared() {
        _controller.value?.release()
        _controller.value = null
    }
}