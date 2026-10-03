package com.winter.wplayer

import android.content.ContentUris
import android.content.Context
import android.media.browse.MediaBrowser
import android.provider.MediaStore
import androidx.media3.common.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Searcher(context: Context) {
    suspend public fun queryAudioList(context: Context): List<AudioItem> = withContext(Dispatchers.IO) {
        val audioList = mutableListOf<AudioItem>()

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} = 1 AND ${MediaStore.Audio.Media.DURATION} > 10000"

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION
        )

        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )

        cursor?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,id
                )
                audioList.add(
                    AudioItem(
                        id = id,
                        uri = uri,
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

    suspend fun queryMediaList(context: Context): List<MediaItem> = withContext(Dispatchers.IO) {
        val mediaItemsList = queryAudioList(context).map { MediaItem.fromUri(it.uri) }
        mediaItemsList
    }

    suspend fun queryMediaList(audioList: List<AudioItem>): List<MediaItem> = withContext(
        Dispatchers.IO) {
        val mediaItemsList = audioList.map { MediaItem.fromUri(it.uri) }
        mediaItemsList
    }
}