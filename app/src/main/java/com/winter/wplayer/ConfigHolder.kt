package com.winter.wplayer

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "config")

object ConfigHolder {
    val REPEAT_MODE = intPreferencesKey("repeat_mode")
    val SHUFFLE = booleanPreferencesKey("shuffle_enabled")
    val MEDIA_LOADED = booleanPreferencesKey("media_loaded")
}