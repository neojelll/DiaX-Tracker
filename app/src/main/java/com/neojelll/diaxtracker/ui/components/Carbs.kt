package com.neojelll.diaxtracker.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.res.stringResource
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.CarbDisplay
import com.neojelll.diaxtracker.data.CarbUnit

/** The carbohydrate display settings for everything under the app's root - provided in NavGraph. */
val LocalCarbDisplay = compositionLocalOf { CarbDisplay() }

/** A stored amount with its unit, as the settings say: "2.5 ХЕ" or "25 г". */
@Composable
fun carbsText(grams: Float): String =
    stringResource(carbsValueFormat(LocalCarbDisplay.current.unit), LocalCarbDisplay.current.format(grams))

/** The "%1$s ХЕ" / "%1$s г" format itself, for building text outside composition (e.g. in a remember block). */
@Composable
fun carbsValueFormat(): String = stringResource(carbsValueFormat(LocalCarbDisplay.current.unit))

/** The unit's short name next to an input field: "ХЕ" or "г". */
@Composable
fun carbsUnitLabel(): String = stringResource(
    when (LocalCarbDisplay.current.unit) {
        CarbUnit.XE -> R.string.bread_units_short_label
        CarbUnit.GRAMS -> R.string.carbs_grams_short_label
    }
)

private fun carbsValueFormat(unit: CarbUnit): Int = when (unit) {
    CarbUnit.XE -> R.string.bread_units_value_format
    CarbUnit.GRAMS -> R.string.carbs_grams_value_format
}
