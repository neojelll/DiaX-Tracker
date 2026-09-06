package com.neojelll.diaxtracker.ui.screens

import java.time.LocalDate
import java.time.LocalTime

internal data class MealProductEntry(val name: String, val breadUnits: String)

internal data class EntryFormState(
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now(),
    val bloodSugar: String = "",
    val breadUnits: String = "",
    val foodLabel: String = "",
    val mealLabel: String? = null,
    val mealProducts: List<MealProductEntry> = emptyList(),
    val shortInsulinDose: String = "",
    val longInsulinDose: String = "",
    val notes: String = "",
    val photoPath: String? = null
) {
    val isFillable: Boolean
        get() = bloodSugar.isNotBlank() || breadUnits.isNotBlank() ||
            shortInsulinDose.isNotBlank() || longInsulinDose.isNotBlank()
}
