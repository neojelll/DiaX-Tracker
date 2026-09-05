package com.neojelll.diaxtracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "meal_preset_products",
    foreignKeys = [
        ForeignKey(
            entity = MealPreset::class,
            parentColumns = ["id"],
            childColumns = ["mealPresetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("mealPresetId")]
)
data class MealPresetProduct(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mealPresetId: Long,
    val name: String,
    val breadUnits: Float,
    val sortOrder: Int
)
