package com.winter.wplayer

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AudioDao {
    @Query("SELECT * FROM audios")
    suspend fun getAll(): List<AudioItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(audios: List<AudioItem>)

    @Query("DELETE FROM audios")
    suspend fun clear()
}