package com.winter.wplayer

import android.app.Application
import android.net.Uri
import androidx.annotation.OptIn
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.winter.wplayer.room.PlaylistRepository
import com.winter.wplayer.room.Song
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.emptyList
@OptIn(UnstableApi::class)

class MusicListViewModel(
    app: Application,
    private val repo: PlaylistRepository
): AndroidViewModel(app) {
    private var _loaded = false
    private val dataStore = app.dataStore


    val songLists: StateFlow<List<Song>> = repo
        .getAllSongs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val playlists = repo
        .getAllPlaylists()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    fun search(order: String, sort: String) {
        viewModelScope.launch {
            val loaded = dataStore.data.first()
            _loaded = loaded[ConfigHolder.MEDIA_LOADED] ?: false
            if (!_loaded) {
                val searcher = Searcher(application)
                val songs = searcher.queryAudioList(application, order, sort)
                repo.insertSongs(songs)
                dataStore.edit { it[ConfigHolder.MEDIA_LOADED] = true }
            }
        }
    }

    fun createPlaylist(name: String, coverUri: Uri?, onCreated: (Long) -> Unit = {} ) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val playlistId = repo.createPlaylist(name = trimmed, coverUri = coverUri)
            onCreated(playlistId)
        }
    }

    fun addSongsToPlaylist(playlistId: Long, songs: List<Song>) {
        viewModelScope.launch {
            repo.addSongsToPlaylist(playlistId, songs)
        }
    }

    fun removeFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repo.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun getPlaylistSongs(playlistId: Long) = repo.getSongsInPlaylist(playlistId)
}
