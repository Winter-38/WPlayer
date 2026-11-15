package com.winter.wplayer

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.net.toUri
import javax.annotation.meta.When

class Searcher(context: Context) {
    val audioList = mutableListOf<Song>()

    val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > 10000"

    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DURATION
    )

    suspend fun queryAudioList(context: Context, order: String, sort: String): List<Song> = withContext(Dispatchers.IO) {
        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            when (order) {
                "TITLE" -> MediaStore.Audio.Media.TITLE
                "DATE_ADDED" -> MediaStore.Audio.Media.DATE_ADDED
                "DATE_MODIFIED" -> MediaStore.Audio.Media.DATE_MODIFIED
                "SIZE" -> MediaStore.Audio.Media.SIZE
                "ARTIST" -> MediaStore.Audio.Media.ARTIST
                else -> MediaStore.Audio.Media.TITLE
            } + when (sort) {
                "ASC" -> " ASC"
                "DESC" -> " DESC"
                else -> " ASC"
            }
        )


        cursor?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val coverCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
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
                audioList.add(
                    Song(
                        songId = id,
                        uri = uri,
                        coverUri = coverUri,
                        title = c.getString(titleCol),
                        album = c.getString(albumCol),
                        artist = c.getString(artistCol),
                        duration = c.getLong(durationCol)
                    )
                )
            }
        }
        audioList
    }


}