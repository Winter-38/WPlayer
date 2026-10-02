package com.winter.core

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class MusicViewModel(app: Application): AndroidViewModel(app) {
    private val _songs = MutableStateFlow<List<AudioItem>>(emptyList())
    val songs: StateFlow<List<AudioItem>> = _songs.asStateFlow()

    private var _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    private var _index = MutableStateFlow(0)
    var index: StateFlow<Int> = _index.asStateFlow()

    fun loadSongs(){
        viewModelScope.launch{
            val list = Searcher(getApplication()).queryAudioList(application)
            _songs.value = list
            _loaded.value = true
        }
    }

}
