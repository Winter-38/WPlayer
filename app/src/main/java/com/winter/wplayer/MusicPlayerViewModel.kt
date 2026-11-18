package com.winter.wplayer

import android.app.Application
import android.content.ComponentName
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.winter.wplayer.room.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MusicPlayerViewModel(app: Application): AndroidViewModel(app) {
    companion object {
        private const val TAG = "MusicPlayerVM"
    }
    private val buildMutex = Mutex()

    private var queueIds: List<String> = emptyList()
    private var restored = false
    private var _controller = MutableStateFlow<MediaController?>(null)
    val controller = _controller.asStateFlow()

    private val datastore = app.dataStore
    private var isRestoring = false

    private val _isPlaying = MutableStateFlow<Boolean?>(false)
    val isPlaying = _isPlaying.asStateFlow()
    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode = _repeatMode.asStateFlow()
    private val _isShuffleEnabled = MutableStateFlow<Boolean?>(false)
    val isShuffleEnabled = _isShuffleEnabled.asStateFlow()



    fun play() {
        val controller = _controller.value ?: return
        controller.play()
    }

    fun pause() {
        val controller = _controller.value ?: return
        controller.pause()
    }
    fun seekToPrevious() {
        val controller = _controller.value ?: return
        if (controller.hasPreviousMediaItem()) controller.seekToPrevious()
    }

    fun seekToNext() {
        val controller = _controller.value ?: return
        if (controller.hasNextMediaItem()) controller.seekToNext()
    }

    fun seekTo(mediaItemIndex: Int, posMs: Long){
        val controller = _controller.value ?: return
        controller.seekTo(mediaItemIndex, posMs)
    }

    fun setMediaItems(mediaItems: List<MediaItem>, startIndex: Int, posMs: Long){
        val controller = _controller.value ?: return
        controller.setMediaItems(mediaItems, startIndex, posMs)
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
                            if (!isRestoring) {
                                viewModelScope.launch {
                                    datastore.edit { it[ConfigHolder.SHUFFLE] = shuffleModeEnabled }
                                }
                            }
                        }

                        override fun onRepeatModeChanged(repeatMode: Int) {
                            _repeatMode.value = repeatMode
                            if (!isRestoring) {
                                viewModelScope.launch {
                                    datastore.edit { it[ConfigHolder.REPEAT_MODE] = repeatMode }
                                }
                            }
                        }
                    })
                },
                ContextCompat.getMainExecutor(context)
            )
        }
    }

    /* fun syncQueue(items: List<MediaItem>) {
        if (items.isEmpty()) return
        val controller = _controller.value ?: return

        val ids = items.map { it.mediaId }
        if (ids == queueIds) return
        queueIds = ids

        val currentId = controller.currentMediaItem?.mediaId
        val newIndex = if (currentId == null) 0
            else items.indexOfFirst { it.mediaId == currentId }.takeIf { it >= 0 } ?: 0
        val keepPos = currentId != null && items[newIndex].mediaId == currentId
        val pos = if (keepPos) controller.currentPosition else 0L
        val wasPlaying = controller.isPlaying

        controller.setMediaItems(items, newIndex, pos)
        controller.prepare()
        if (wasPlaying) controller.play()

        IndexHolder.update(currentId)

        if (!restored) {
            restored = true
            viewModelScope.launch {
                val config = datastore.data.first()
                isRestoring = true
                controller.repeatMode = config[ConfigHolder.REPEAT_MODE] ?: Player.REPEAT_MODE_OFF
                controller.shuffleModeEnabled = config[ConfigHolder.SHUFFLE] ?: false
                isRestoring = false
            }
        }
    } */

    fun playSong(song: Song) {
        val controller = _controller.value ?: return
        val mediaId = song.songId.toString()

        val index = (0 until controller.mediaItemCount).firstOrNull {
            controller.getMediaItemAt(it).mediaId == mediaId
        }
        if (index != null) {
            controller.seekTo(index, 0L)
        } else {
            controller.addMediaItem(
                MediaItem.Builder()
                    .setMediaId(mediaId)
                    .setUri(song.uri)
                    .build()
            )
            controller.prepare()
            controller.seekTo(controller.mediaItemCount - 1, 0L)
        }
        controller.play()
    }

    override fun onCleared() {
        _controller.value?.release()
        _controller.value = null
    }
}