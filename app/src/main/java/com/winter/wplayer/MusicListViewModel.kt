package com.winter.wplayer

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.launch


class MusicListViewModel(app: Application): AndroidViewModel(app) {
    private val _songs = MutableStateFlow<List<AudioItem>>(emptyList())
    val songs: StateFlow<List<AudioItem>> = _songs.asStateFlow()

    private val _medias = MutableStateFlow<List<MediaItem>>(emptyList())
    val medias = _medias.asStateFlow()

    private var _loaded = false

    fun loadSongs() {
        viewModelScope.launch{
            val list = Searcher(getApplication()).queryAudioList(application)
            _songs.value = list
            _loaded = true
        }
    }

    fun loadMediaItems() {
        viewModelScope.launch {
            val list = Searcher(getApplication()).queryMediaList(application)
            _medias.value = list
        }
    }

    fun loadMediaItemsIfLoaded() {
        if (_loaded && _songs.value.isNotEmpty()) {
            _medias.value = _songs.value.map { MediaItem.fromUri(it.uri) }
        }
    }
}
