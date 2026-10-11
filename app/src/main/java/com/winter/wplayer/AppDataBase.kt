package com.winter.wplayer

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [AudioItem::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDataBase: RoomDatabase() {
    abstract fun audioDao(): AudioDao

    companion object {
        @Volatile private var INSTANCE: AppDataBase? = null

        fun get(context: Context): AppDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "music.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
