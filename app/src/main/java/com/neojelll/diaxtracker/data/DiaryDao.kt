package com.neojelll.diaxtracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    @Insert
    suspend fun insert(entry: DiaryEntry)

    @Update
    suspend fun update(entry: DiaryEntry)

    @Delete
    suspend fun delete(entry: DiaryEntry)

    @Query("SELECT * FROM diary_entries ORDER BY createdAt DESC")
    fun getAllEntries(): Flow<List<DiaryEntry>>
}
