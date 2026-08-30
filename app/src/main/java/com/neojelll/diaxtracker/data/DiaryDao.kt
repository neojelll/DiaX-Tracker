package com.neojelll.diaxtracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    @Insert
    suspend fun insert(entry: DiaryEntry)

    @Query("SELECT * FROM diary_entries ORDER BY createdAt DESC")
    fun getAllEntries(): Flow<List<DiaryEntry>>
}
