package com.neojelll.diaxtracker.data

import android.content.Context

/** The carbohydrate display settings: unit and grams per XE. Only how grams are shown - never what's stored. */
class CarbSettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun get(): CarbDisplay = CarbDisplay(
        unit = prefs.getString(KEY_UNIT, null)?.let { runCatching { CarbUnit.valueOf(it) }.getOrNull() } ?: CarbUnit.XE,
        gramsPerXe = prefs.getFloat(KEY_GRAMS_PER_XE, DEFAULT_GRAMS_PER_XE).coerceIn(MIN_GRAMS_PER_XE, MAX_GRAMS_PER_XE)
    )

    fun save(display: CarbDisplay) {
        prefs.edit()
            .putString(KEY_UNIT, display.unit.name)
            .putFloat(KEY_GRAMS_PER_XE, display.gramsPerXe)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "carb_settings"
        private const val KEY_UNIT = "unit"
        private const val KEY_GRAMS_PER_XE = "grams_per_xe"

        // 10 and 12 g are the common values; the range leaves room for other conventions.
        const val MIN_GRAMS_PER_XE = 8f
        const val MAX_GRAMS_PER_XE = 15f
        const val GRAMS_PER_XE_STEP = 1f
    }
}
