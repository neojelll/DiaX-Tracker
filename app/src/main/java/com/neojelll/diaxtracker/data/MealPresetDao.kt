package com.neojelll.diaxtracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPresetDao {
    @Insert
    suspend fun insert(preset: MealPreset): Long

    @Update
    suspend fun update(preset: MealPreset)

    @Delete
    suspend fun delete(preset: MealPreset)

    @Query("SELECT * FROM meal_presets ORDER BY name ASC")
    fun getAllPresets(): Flow<List<MealPreset>>
}
