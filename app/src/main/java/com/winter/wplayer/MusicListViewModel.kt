package com.winter.wplayer

import android.app.Application
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.application
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import com.winter.wplayer.room.PlaylistRepository
import com.winter.wplayer.room.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

class MusicListViewModel(
    app: Application,
    private val repo: PlaylistRepository
): AndroidViewModel(app) {
    private var _loaded = false
    private val dataStore = app.dataStore
    private val sortPrefs = dataStore.data.map {
        (it[ConfigHolder.SONG_ORDER] ?: "TITLE") to (it[ConfigHolder.SONG_SORT] ?: "ASC")
    }


    val songLists: StateFlow<List<Song>> =
        combine(repo.getAllSongs(), sortPrefs) { songs, (order, sort) ->
            songs.sortedWith(comparatorFor(order, sort))
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val mediaItemLists: StateFlow<List<MediaItem>> =
        songLists
            .map { songs -> songs.map { MediaItem.fromUri(it.uri) } }
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
    fun search() { //init function
        viewModelScope.launch {
            val config = dataStore.data.first()
            _loaded = config[ConfigHolder.MEDIA_LOADED] ?: false

            if (!_loaded) {
                val order = config[ConfigHolder.SONG_ORDER] ?: "TITLE"
                val sort = config[ConfigHolder.SONG_SORT] ?: "ASC"

                val searcher = Searcher(application)
                val songs = searcher.queryAudioList(application, order, sort)
                repo.insertSongs(songs)
                dataStore.edit { it[ConfigHolder.MEDIA_LOADED] = true }
            }
        }
    }

    fun setSongOrder(order: String) {
        viewModelScope.launch {
            dataStore.edit { it[ConfigHolder.SONG_ORDER] = order }
        }
    }

    fun setSongSort(sort: String) {
        viewModelScope.launch {
            dataStore.edit { it[ConfigHolder.SONG_SORT] = sort }
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
    fun removePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repo.removePlaylist(playlistId)
        }
    }

    fun getPlaylistSongs(playlistId: Long) = repo.getSongsInPlaylist(playlistId)

    private fun comparatorFor(order: String, sort: String): Comparator<Song>{
        fun key(song: Song): String? = when (order) {
            "ARTIST" -> song.artist
            else -> song.title
        }
        val byField = Comparator<Song> { a, b ->
            compareNullable(key(a), key(b))
        }
        val byDirection = if (sort == "DESC") byField.reversed() else byField
        val nullsLast = Comparator<Song> { a, b ->
            val an = key(a) == null
            val bn = key(b) == null
            when {
                an == bn -> 0
                an -> 1
                else -> -1
            }
        }
        return nullsLast.then(byDirection).thenBy { it.songId }
    }

    private fun compareNullable(a: String?, b: String?): Int = when {
        a == null && b == null -> 0
        a == null -> 1
        b == null -> -1
        else -> a.compareTo(b, ignoreCase = true)
    }
}


class MusicListViewModelFactory(
    private val app: Application,
    private val repo: PlaylistRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MusicListViewModel(app, repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
