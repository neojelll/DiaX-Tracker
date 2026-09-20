package com.neojelll.diaxtracker.ui.screens

import java.time.LocalDate
import java.time.LocalTime

// Guard against an accidental giant paste, not a real editorial limit - nothing else in the app
// (storage, backup, display) actually requires one.
internal const val NOTES_MAX_LENGTH = 2500

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
    val photoPath: String? = null,
    val foodExpanded: Boolean = false,
    val manualXe: String = "",
    val detailsExpanded: Boolean = false
) {
    val isFillable: Boolean
        get() = bloodSugar.isNotBlank() || breadUnits.isNotBlank() ||
            shortInsulinDose.isNotBlank() || longInsulinDose.isNotBlank()
}
