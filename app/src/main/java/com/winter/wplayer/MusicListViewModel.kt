package com.winter.wplayer

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class MusicListViewModel(app: Application): AndroidViewModel(app) {
    private val _songs = MutableStateFlow<List<AudioItem>>(emptyList())
    val songs: StateFlow<List<AudioItem>> = _songs.asStateFlow()

    private val _medias = MutableStateFlow<List<MediaItem>>(emptyList())
    val medias: StateFlow<List<MediaItem>> = _medias.asStateFlow()

    val searcher: Searcher = Searcher(app)
    private val TAG = "MusicListViewModel"
    fun loadSongs() {
        if (_songs.value.isNotEmpty()) return
        viewModelScope.launch{
            val list = searcher.queryAudioList(application)
            _songs.value = list
        }
    }


    fun loadMediaItems() {
        viewModelScope.launch {
            val list = _songs.filter { it.isNotEmpty() }.first()
            _medias.value = _songs.value.map { MediaItem.fromUri(it.uri) }
        }
    }
}
