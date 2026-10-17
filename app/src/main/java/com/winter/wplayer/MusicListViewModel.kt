package com.winter.wplayer

import android.app.Application
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.emptyList
@OptIn(UnstableApi::class)

class MusicListViewModel
    (app: Application): AndroidViewModel(app) {
    private val TAG = "MusicListViewModel"
    private val database = AppDataBase.get(application)
    private val songDao = database.songDao()
    private val playlistDao = database.playlistDao()
    private val playlistSongDao = database.playlistSongDao()
    private val _loaded = MutableStateFlow(false)
    val loaded = _loaded.asStateFlow()

    val songLists: StateFlow<List<Song>> = songDao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val playlists = playlistDao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        if (!_loaded.value) {
            viewModelScope.launch {
                val searcher: Searcher = Searcher(app)
                val list = searcher.queryAudioList(application)
                songDao.insertAll(list)
                _loaded.value = true
            }
        }
    }

    fun importSongs(list: List<Song>) = viewModelScope.launch {
        songDao.insertAll(list)
    }

    fun createPlaylist(name: String, onCreated: (Long) -> Unit = {} ) =
        viewModelScope.launch {
            val playlistId = playlistDao.insert(Playlist(0, name, null))
            onCreated(playlistId)
        }

    fun addToPlaylist(playlistId: Long, songId: Long) = viewModelScope.launch {
        val pos = playlistSongDao.nextPos(playlistId)
        playlistSongDao.insertCrossRef(PlaylistSongCrossRef(playlistId, songId, pos))
    }

    fun removeFromPlaylist(playlistId: Long, songId: Long) = viewModelScope.launch {
        playlistSongDao.delete(playlistId, songId)
    }

    fun playlistSongs(playlistId: Long) = playlistSongDao.getSongsInPlaylist(playlistId)
}
