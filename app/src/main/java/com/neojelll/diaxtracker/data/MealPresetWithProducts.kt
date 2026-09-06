package com.neojelll.diaxtracker.data

import androidx.room.Embedded
import androidx.room.Relation

data class MealPresetWithProducts(
    @Embedded val preset: MealPreset,
    @Relation(
        parentColumn = "id",
        entityColumn = "mealPresetId"
    )
    val products: List<MealPresetProduct>
) {
    val totalBreadUnits: Float
        get() = products.sumOf { it.breadUnits.toDouble() }.toFloat()
}
