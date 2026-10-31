package com.winter.wplayer

import android.app.Application
import android.content.ComponentName
import android.util.Log
import androidx.lifecycle.AndroidViewModel
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
        private val TAG = "MusicPlayerViewModel"
    }
    private val buildMutex = Mutex()
    private val _controller = MutableStateFlow<MediaController?>(null)
    val controller = _controller.asStateFlow()
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

    /**
     * 连接 PlaybackService 并创建 MediaController。
     * 幂等：已有 controller 时直接返回，并发调用会被 Mutex 串行化，
     * 保证整个进程里只会存在一个 MediaController 实例。
     * 必须在主线程调用（MediaController.Builder#buildAsync 的要求）。
     */
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
            }
        }
    }

    override fun onCleared() {
        _controller.value?.release()
        _controller.value = null
    }
}