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
    /** Summed in grams; rounded only when shown. */
    val totalCarbsGrams: Float
        get() = products.sumOf { it.carbsGrams.toDouble() }.toFloat()
}
