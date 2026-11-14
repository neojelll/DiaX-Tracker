package com.neojelll.diaxtracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.CarbSettingsStore
import com.neojelll.diaxtracker.data.CarbUnit
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoType
import com.neojelll.diaxtracker.ui.theme.tabular
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel
import kotlin.math.roundToInt

/** Carbohydrate unit (XE or grams) and, for XE, how many grams one is. Display only - storage stays grams. */
@Composable
fun CarbSettingSection(viewModel: DiaryViewModel) {
    val display by viewModel.carbDisplay.collectAsState()

    fun setGramsPerXe(value: Float) = viewModel.setCarbDisplay(
        display.copy(gramsPerXe = value.coerceIn(CarbSettingsStore.MIN_GRAMS_PER_XE, CarbSettingsStore.MAX_GRAMS_PER_XE))
    )

    Text(stringResource(R.string.carbs_setting_title), style = GlukoType.Label)
    Spacer(Modifier.height(11.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ChoiceTile(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.carbs_unit_xe),
            selected = display.unit == CarbUnit.XE,
            onClick = { viewModel.setCarbDisplay(display.copy(unit = CarbUnit.XE)) }
        )
        ChoiceTile(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.carbs_unit_grams),
            selected = display.unit == CarbUnit.GRAMS,
            onClick = { viewModel.setCarbDisplay(display.copy(unit = CarbUnit.GRAMS)) }
        )
    }

    // Only XE need a size; in grams there is nothing to convert.
    if (display.unit == CarbUnit.XE) {
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.carbs_grams_per_xe_label), style = GlukoType.Label, modifier = Modifier.weight(1f))
            CircleButton(28.dp, GlukoColors.Tile, { setGramsPerXe(display.gramsPerXe - CarbSettingsStore.GRAMS_PER_XE_STEP) }) {
                LucideIcon(LucidePaths.Minus, 12.dp, strokeWidth = 2.4f)
            }
            Text(
                display.gramsPerXe.roundToInt().toString(),
                style = GlukoType.ScreenTitle.copy(fontSize = 24.sp).tabular,
                modifier = Modifier.width(44.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            CircleButton(28.dp, GlukoColors.Tile, { setGramsPerXe(display.gramsPerXe + CarbSettingsStore.GRAMS_PER_XE_STEP) }) {
                LucideIcon(LucidePaths.Plus, 12.dp, strokeWidth = 2.4f)
            }
        }
    }
    Spacer(Modifier.height(11.dp))
    Text(stringResource(R.string.carbs_setting_hint), style = GlukoType.Hint)
}
