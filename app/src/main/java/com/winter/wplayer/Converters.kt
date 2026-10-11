package com.winter.wplayer

import androidx.room.TypeConverter
import android.net.Uri

class Converters {
    @TypeConverter
    fun fromUri(uri: Uri?): String? = uri?.toString()

    @TypeConverter
    fun toUri(string: String?): Uri? = string.let { Uri.parse(it) }
}