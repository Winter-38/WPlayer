package com.winter.wplayer

import android.net.Uri
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song (
    @PrimaryKey val songId: Long,
    val uri: Uri,
    val coverUri: Uri?,
    val title: String,
    val album: String,
    val artist: String,
    val duration: Long
)

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val playlistId: Int = 0,
    val name: String,
    val coverUri: Uri?
)

@Entity(
    tableName = "playlist_song_cross_ref",
    primaryKeys = ["playlistId", "songId"],
    foreignKeys = [
        ForeignKey(
            entity = Playlist::class,
            parentColumns = ["playlistId"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Song::class,
            parentColumns = ["songId"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE

        )
    ],
    indices = [Index("songId")]
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: Long,
    val pos: Int
)