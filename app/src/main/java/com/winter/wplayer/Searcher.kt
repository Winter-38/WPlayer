package com.winter.wplayer

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.net.toUri
import com.winter.wplayer.room.Song

class Searcher(context: Context) {
    val audioList = mutableListOf<Song>()

    val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > 10000"

    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.SIZE,
        MediaStore.Audio.Media.DATE_ADDED,
        MediaStore.Audio.Media.DATE_MODIFIED,
        MediaStore.Audio.Media.DURATION
    )

    suspend fun queryAudioList(context: Context): List<Song> = withContext(Dispatchers.IO) {
        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            MediaStore.Audio.Media.TITLE
        )


        cursor?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val coverCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val sizeCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateAddedCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val dateModifiedCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
            val durationCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,id
                )
                val coverId = c.getLong(coverCol)
                val coverUri = ContentUris.withAppendedId(
                    "content://media/external/audio/albumart".toUri(),coverId
                )
                val size = c.getLong(sizeCol)
                val dateAdded = c.getLong(dateAddedCol)
                val dateModified = c.getLong(dateModifiedCol)
                audioList.add(
                    Song(
                        songId = id,
                        uri = uri,
                        coverUri = coverUri,
                        title = c.getString(titleCol),
                        album = c.getString(albumCol),
                        artist = c.getString(artistCol),
                        size = size,
                        dateAdded = dateAdded,
                        dateModified = dateModified,
                        duration = c.getLong(durationCol)
                    )
                )
            }
        }
        audioList
    }


}