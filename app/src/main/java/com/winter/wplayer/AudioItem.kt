package com.winter.wplayer

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audios")
data class AudioItem (
    @PrimaryKey val id: Long,
    val uri: Uri,
    val coverUri: Uri?,
    val title: String,
    val album: String,
    val artist: String,
    val duration: Long
)