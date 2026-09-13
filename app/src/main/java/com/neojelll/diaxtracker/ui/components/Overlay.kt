package com.neojelll.diaxtracker.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.neojelll.diaxtracker.data.MealPresetWithProducts
import java.time.LocalDate
import java.time.LocalTime

/** Everything that can be shown on top of the four tabs. At most one is active at a time. */
sealed interface Overlay {
    data object None : Overlay
    data class DatePicker(val initial: LocalDate, val onPick: (LocalDate) -> Unit) : Overlay
    data class TimePicker(val initial: LocalTime, val onPick: (LocalTime) -> Unit) : Overlay
    data object Notifications : Overlay
    data class RecordEdit(val entryId: Long) : Overlay
    data class PresetDetail(val presetId: Long) : Overlay
    data class PresetEdit(val presetId: Long?) : Overlay
    data class Photo(val photoPath: String?, val caption: String) : Overlay
}

/** Owns the single active overlay and exposes intent-shaped open functions to screens. */
class OverlayController {
    var current: Overlay by mutableStateOf(Overlay.None)
        private set

    // Cross-tab channel: Food's "Use for entry" sets this, Home consumes it into its own form state.
    var pendingPresetSelection: MealPresetWithProducts? by mutableStateOf(null)
        private set

    fun dismiss() {
        current = Overlay.None
    }

    fun selectPresetForEntry(preset: MealPresetWithProducts) {
        pendingPresetSelection = preset
    }

    fun consumePendingPresetSelection() {
        pendingPresetSelection = null
    }

    fun openDatePicker(initial: LocalDate, onPick: (LocalDate) -> Unit) {
        current = Overlay.DatePicker(initial, onPick)
    }

    fun openTimePicker(initial: LocalTime, onPick: (LocalTime) -> Unit) {
        current = Overlay.TimePicker(initial, onPick)
    }

    fun openNotifications() {
        current = Overlay.Notifications
    }

    fun openRecordEdit(entryId: Long) {
        current = Overlay.RecordEdit(entryId)
    }

    fun openPresetDetail(presetId: Long) {
        current = Overlay.PresetDetail(presetId)
    }

    fun openPresetEdit(presetId: Long?) {
        current = Overlay.PresetEdit(presetId)
    }

    fun openPhoto(photoPath: String?, caption: String) {
        current = Overlay.Photo(photoPath, caption)
    }
}
