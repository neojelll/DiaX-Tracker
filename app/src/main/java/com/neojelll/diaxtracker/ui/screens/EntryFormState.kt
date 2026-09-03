package com.neojelll.diaxtracker.ui.screens

import java.time.LocalDate
import java.time.LocalTime

internal data class EntryFormState(
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now(),
    val bloodSugar: String = "",
    val breadUnits: String = "",
    val shortInsulinDose: String = "",
    val longInsulinDose: String = "",
    val notes: String = ""
) {
    val isFillable: Boolean
        get() = bloodSugar.isNotBlank() || breadUnits.isNotBlank() ||
            shortInsulinDose.isNotBlank() || longInsulinDose.isNotBlank()
}
