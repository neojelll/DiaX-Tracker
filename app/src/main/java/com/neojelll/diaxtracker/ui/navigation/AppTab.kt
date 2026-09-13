package com.neojelll.diaxtracker.ui.navigation

enum class AppTab {
    HOME, FOOD, HISTORY, SETTINGS
}

sealed interface AppOverlay {
    data class EditEntry(val entryId: Long) : AppOverlay
    data class PresetDetail(val presetId: Long) : AppOverlay
    data class PresetEditor(val presetId: Long?) : AppOverlay
    data class Photo(val photoPath: String, val caption: String) : AppOverlay
}
