package com.winter.wplayer

import android.app.Application
import android.net.Uri
import androidx.annotation.OptIn
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.winter.wplayer.room.PlaylistRepository
import com.winter.wplayer.room.Song
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.emptyList
@OptIn(UnstableApi::class)

class MusicListViewModel(
    app: Application,
    private val repo: PlaylistRepository
): AndroidViewModel(app) {
    private var _loaded = false


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
    fun search() {
        if (!_loaded) {
            viewModelScope.launch {
                val searcher = Searcher(application)
                val songs = searcher.queryAudioList(application, "TITLE", "ASC")
                repo.insertSongs(songs)
                _loaded = true
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
