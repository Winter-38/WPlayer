package com.winter.wplayer.room

import android.net.Uri
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

class PlaylistRepository(
    private val database: AppDataBase,
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao,
    private val playlistSongDao: PlaylistSongDao
) {
    fun getAllSongs(): Flow<List<Song>> = songDao.getAll()
    fun getAllPlaylists(): Flow<List<Playlist>> = playlistDao.getAll()

    suspend fun insertSongs(songs: List<Song>) = songDao.insertAll(songs)

    suspend fun createPlaylist(name: String, coverUri: Uri?): Long =
        playlistDao.insert(Playlist(name = name, coverUri = coverUri))

    fun getSongsInPlaylist(playlistId: Long): Flow<List<Song>> =
        playlistSongDao.getSongsInPlaylist(playlistId)

    suspend fun addSongsToPlaylist(playlistId: Long, songs: List<Song>) {
        database.withTransaction {
            playlistDao.insertSongs(songs)
            val pos = playlistSongDao.nextPos(playlistId)
            val refs = songs.mapIndexed { index, song ->
                PlaylistSongCrossRef(playlistId, song.songId, pos + index)
            }
            playlistSongDao.insertCrossRefs(refs)
        }
    }

    suspend fun addSongToPlaylist(playlistId: Long, song: Song) {
        database.withTransaction {
            val pos = playlistSongDao.nextPos(playlistId)
            val ref = PlaylistSongCrossRef(playlistId, song.songId, pos)
            playlistSongDao.insertCrossRef(ref)
        }
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) =
        playlistSongDao.removeFromPlaylist(playlistId, songId)

    suspend fun removePlaylist(playlistId: Long) =
        playlistDao.deleteById(playlistId)
}