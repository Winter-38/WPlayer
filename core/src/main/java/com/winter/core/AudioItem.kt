package com.winter.core

import android.net.Uri

data class AudioItem (
    val id: Long,
    val uri: Uri,
    val title: String,
    val album: String,
    val artist: String,
    val duration: Long
)