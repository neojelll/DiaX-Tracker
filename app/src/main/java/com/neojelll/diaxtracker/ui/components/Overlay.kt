package com.neojelll.diaxtracker.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.neojelll.diaxtracker.data.MealPresetWithProducts
import java.time.LocalDate
import java.time.LocalTime

/** Everything that can be shown on top of the four tabs. */
sealed interface Overlay {
    data class DatePicker(val initial: LocalDate, val onPick: (LocalDate) -> Unit) : Overlay
    data class TimePicker(val initial: LocalTime, val onPick: (LocalTime) -> Unit) : Overlay
    data object Notifications : Overlay
    data class RecordEdit(val entryId: Long) : Overlay
    data class PresetDetail(val presetId: Long) : Overlay
    data class PresetEdit(val presetId: Long?) : Overlay
    data class Photo(val photoPath: String?, val caption: String) : Overlay
    data class PhotoActions(
        val hasPhoto: Boolean,
        val onCamera: () -> Unit,
        val onGallery: () -> Unit,
        val onRemove: () -> Unit
    ) : Overlay
}

// Stack, bottom to top. Date/time pickers and the photo action menu push on top of whatever opened them (e.g. the
// record-edit sheet), keeping it mounted with its draft state alive; everything else replaces
// the whole stack since it's always a fresh standalone sheet.
class OverlayController {
    private val backing = mutableStateListOf<Overlay>()
    val stack: List<Overlay> get() = backing

    var pendingPresetSelection: MealPresetWithProducts? by mutableStateOf(null)
        private set

    fun dismiss() {
        if (backing.isNotEmpty()) backing.removeAt(backing.lastIndex)
    }

    fun selectPresetForEntry(preset: MealPresetWithProducts) {
        pendingPresetSelection = preset
    }

    fun consumePendingPresetSelection() {
        pendingPresetSelection = null
    }

    private fun replace(overlay: Overlay) {
        backing.clear()
        backing.add(overlay)
    }

    private fun push(overlay: Overlay) {
        backing.add(overlay)
    }

    fun openDatePicker(initial: LocalDate, onPick: (LocalDate) -> Unit) {
        push(Overlay.DatePicker(initial, onPick))
    }

    fun openTimePicker(initial: LocalTime, onPick: (LocalTime) -> Unit) {
        push(Overlay.TimePicker(initial, onPick))
    }

    fun openPhotoActions(hasPhoto: Boolean, onCamera: () -> Unit, onGallery: () -> Unit, onRemove: () -> Unit) {
        push(Overlay.PhotoActions(hasPhoto, onCamera, onGallery, onRemove))
    }

    fun openNotifications() {
        replace(Overlay.Notifications)
    }

    fun openRecordEdit(entryId: Long) {
        replace(Overlay.RecordEdit(entryId))
    }

    fun openPresetDetail(presetId: Long) {
        replace(Overlay.PresetDetail(presetId))
    }

    fun openPresetEdit(presetId: Long?) {
        replace(Overlay.PresetEdit(presetId))
    }

    fun openPhoto(photoPath: String?, caption: String) {
        replace(Overlay.Photo(photoPath, caption))
    }
}
