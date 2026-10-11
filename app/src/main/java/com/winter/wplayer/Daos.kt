package com.winter.wplayer

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs")
    fun getAll(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE songId = :id")
    suspend fun getById(id: Long): Song?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<Song>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: Song)

    @Delete
    suspend fun delete(songs: Song)
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists")
    fun getAll(): Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playlist: Playlist): Int

    @Delete
    suspend fun delete(playlist: Playlist)
}

@Dao
interface PlaylistSongDao {
    @Query("""
        SELECT songs.* FROM songs
        INNER JOIN playlist_song_cross_ref AS ref
            ON songs.songId = ref.songId
        WHERE ref.playlistId = :playlistId
        ORDER BY ref.pos ASC
    """)
    fun getSongsInPlaylist(playlistId: Int): Flow<List<Song>>

    @Query("SELECT COALESCE(MAX(pos), -1) + 1 FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    suspend fun nextPos(playlistId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(ref: PlaylistSongCrossRef)

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun delete(playlistId: Int, songId: Long)
}