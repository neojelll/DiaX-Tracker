package com.neojelll.diaxtracker.ui.components

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
import com.neojelll.diaxtracker.data.InsulinSettingsStore
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoType
import com.neojelll.diaxtracker.ui.theme.tabular
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel

private const val STEP_HOURS = 0.5f

/** How long a rapid-acting dose is considered on board - feeds the decay curve in InsulinBanner. */
@Composable
fun InsulinDurationSection(viewModel: DiaryViewModel) {
    val hours by viewModel.insulinDurationHours.collectAsState()

    fun setHours(value: Float) = viewModel.setInsulinDurationHours(
        value.coerceIn(InsulinSettingsStore.MIN_DURATION_HOURS, InsulinSettingsStore.MAX_DURATION_HOURS)
    )

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Text(stringResource(R.string.insulin_duration_label), style = GlukoType.Label, modifier = Modifier.weight(1f))
        Text(stringResource(R.string.hours_unit), style = GlukoType.Hint)
    }
    Spacer(Modifier.height(13.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(R.string.insulin_duration_value_format, hours),
            style = GlukoType.ScreenTitle.copy(fontSize = 30.sp).tabular,
            modifier = Modifier.weight(1f)
        )
        CircleButton(28.dp, GlukoColors.Tile, { setHours(hours - STEP_HOURS) }) {
            LucideIcon(LucidePaths.Minus, 12.dp, strokeWidth = 2.4f)
        }
        Spacer(Modifier.width(9.dp))
        CircleButton(28.dp, GlukoColors.Tile, { setHours(hours + STEP_HOURS) }) {
            LucideIcon(LucidePaths.Plus, 12.dp, strokeWidth = 2.4f)
        }
    }
    Spacer(Modifier.height(11.dp))
    Text(stringResource(R.string.insulin_duration_hint), style = GlukoType.Hint)
}
