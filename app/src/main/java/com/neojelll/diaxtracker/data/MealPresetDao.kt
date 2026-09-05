package com.neojelll.diaxtracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class MealPresetDao {
    @Insert
    abstract suspend fun insert(preset: MealPreset): Long

    @Update
    abstract suspend fun update(preset: MealPreset)

    @Delete
    abstract suspend fun delete(preset: MealPreset)

    @Insert
    abstract suspend fun insertProducts(products: List<MealPresetProduct>)

    @Query("DELETE FROM meal_preset_products WHERE mealPresetId = :presetId")
    abstract suspend fun deleteProductsForPreset(presetId: Long)

    @Transaction
    @Query("SELECT * FROM meal_presets ORDER BY name ASC")
    abstract fun getAllPresetsWithProducts(): Flow<List<MealPresetWithProducts>>

    @Transaction
    open suspend fun upsertPresetWithProducts(preset: MealPreset, products: List<MealPresetProduct>): Long {
        val presetId = if (preset.id == 0L) insert(preset) else preset.id.also { update(preset) }
        deleteProductsForPreset(presetId)
        insertProducts(products.map { it.copy(mealPresetId = presetId) })
        return presetId
    }
}
