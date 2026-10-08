package com.neojelll.diaxtracker.ui.screens

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

// Guard against an accidental giant paste, not a real editorial limit - nothing else in the app
// (storage, backup, display) actually requires one.
internal const val NOTES_MAX_LENGTH = 2500

internal data class MealProductEntry(val name: String, val breadUnits: String)

internal data class EntryFormState(
    /** The moment picked for the entry; null until the person picks one - the form follows the clock till then. */
    val dateTime: LocalDateTime? = null,
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

    /** What the form shows and saves: the picked moment, or [now] while none has been picked. */
    fun dateTimeAt(now: LocalDateTime): LocalDateTime = dateTime ?: now

    /** Picking the date pins the moment - the time it showed at that point stays as it was. */
    fun withDate(date: LocalDate, now: LocalDateTime): EntryFormState =
        copy(dateTime = LocalDateTime.of(date, dateTimeAt(now).toLocalTime()))

    /** Picking the time pins the moment - the date it showed at that point stays as it was. */
    fun withTime(time: LocalTime, now: LocalDateTime): EntryFormState =
        copy(dateTime = LocalDateTime.of(dateTimeAt(now).toLocalDate(), time))
}
