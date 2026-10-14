package com.neojelll.diaxtracker.ui.screens

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

// Guard against an accidental giant paste, not a real editorial limit - nothing else in the app
// (storage, backup, display) actually requires one.
internal const val NOTES_MAX_LENGTH = 2500

/** [carbs]: the product's carbohydrates as typed/shown, in the display unit (CarbDisplay); grams on save. */
internal data class MealProductEntry(val name: String, val carbs: String)

internal data class EntryFormState(
    /** The moment picked for the entry; null until the person picks one - the form follows the clock till then. */
    val dateTime: LocalDateTime? = null,
    /** The moment the form showed when something was first entered - from then on it stops following the clock. */
    val heldAt: LocalDateTime? = null,
    val bloodSugar: String = "",
    /** Carbohydrates as typed/shown, in the display unit (CarbDisplay) - converted to grams on save. */
    val carbs: String = "",
    val foodLabel: String = "",
    val mealLabel: String? = null,
    val mealProducts: List<MealProductEntry> = emptyList(),
    val shortInsulinDose: String = "",
    val longInsulinDose: String = "",
    val notes: String = "",
    val photoPath: String? = null,
    val foodExpanded: Boolean = false,
    val manualCarbs: String = "",
    /** The preset picked on the add-entry screen; null when none, or once carbs were typed by hand. */
    val presetId: Long? = null,
    /** Whether the comment field is unfolded in the details block. */
    val noteOpen: Boolean = false
) {
    val isFillable: Boolean
        get() = bloodSugar.isNotBlank() || carbs.isNotBlank() ||
            shortInsulinDose.isNotBlank() || longInsulinDose.isNotBlank()

    /** Something was entered - the "clear" button shows. */
    val isDirty: Boolean
        get() = isFillable || presetId != null || mealLabel != null || notes.isNotBlank() || photoPath != null

    /** An empty form that keeps the picked date/time, if any. */
    fun cleared(): EntryFormState = EntryFormState(dateTime = dateTime)

    /** Whether the moment stays put - picked, or held since something was entered - rather than following the clock. */
    val isPinned: Boolean
        get() = dateTime != null || heldAt != null

    /** Back to [now]: drops a picked moment; a filled form is held at [now], an empty one follows the clock again. */
    fun followingClock(now: LocalDateTime): EntryFormState =
        copy(dateTime = null, heldAt = if (isDirty) now else null)

    /** Holds the moment shown ([now]) once something is entered, lets go once the form is empty again. */
    fun held(now: LocalDateTime): EntryFormState = when {
        !isDirty -> copy(heldAt = null)
        dateTime == null && heldAt == null -> copy(heldAt = now)
        else -> this
    }

    /** What the form shows and saves: the picked moment, the held one, or [now] while neither is there. */
    fun dateTimeAt(now: LocalDateTime): LocalDateTime = dateTime ?: heldAt ?: now

    /** Picking the date pins the moment - the time it showed at that point stays as it was. */
    fun withDate(date: LocalDate, now: LocalDateTime): EntryFormState =
        copy(dateTime = LocalDateTime.of(date, dateTimeAt(now).toLocalTime()))

    /** Picking the time pins the moment - the date it showed at that point stays as it was. */
    fun withTime(time: LocalTime, now: LocalDateTime): EntryFormState =
        copy(dateTime = LocalDateTime.of(dateTimeAt(now).toLocalDate(), time))
}
