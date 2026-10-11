package com.winter.wplayer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.emptyList


class MusicListViewModel(
    app: Application,
    ): AndroidViewModel(app) {
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    private val database = AppDataBase.get(application)
    private val songDao = database.songDao()
    private val playlistDao = database.playlistDao()
    private val playlistSongDao = database.playlistSongDao()

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

    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _medias = MutableStateFlow<List<MediaItem>>(emptyList())
    val medias: StateFlow<List<MediaItem>> = _medias.asStateFlow()

    private val searcher: Searcher = Searcher(app)
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
