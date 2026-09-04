package com.neojelll.diaxtracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_presets")
data class MealPreset(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val breadUnits: Float
)
