package com.winter.wplayer

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Searcher(context: Context) {
    val audioList = mutableListOf<AudioItem>()

    val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > 10000"

    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.ALBUM_ID,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DURATION
    )

    suspend public fun queryAudioList(context: Context): List<AudioItem> = withContext(Dispatchers.IO) {
        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            "${MediaStore.Audio.Media.DURATION} ASC"
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
                    Uri.parse("content://media/external/audio/albumart"),coverId
                )
                audioList.add(
                    AudioItem(
                        id = id,
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