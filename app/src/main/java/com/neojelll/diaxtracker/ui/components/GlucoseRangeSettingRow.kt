package com.neojelll.diaxtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoRadius
import com.neojelll.diaxtracker.ui.theme.GlukoSpacing
import com.neojelll.diaxtracker.ui.theme.GlukoType
import com.neojelll.diaxtracker.ui.theme.tabular
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel

// Real validation bounds (also used for the visual scale below), matching the manual-entry
// dialog these replace: a personal target can't cross the fixed clinical thresholds.
private const val MIN_BOUND_MMOL = 3.9f
private const val MAX_BOUND_MMOL = 20f
private const val STEP_MMOL = 0.1f
private const val MIN_GAP_MMOL = 0.5f

@Composable
fun GlucoseRangeSection(viewModel: DiaryViewModel) {
    val range by viewModel.glucoseRange.collectAsState()

    fun setLow(v: Float) = viewModel.setGlucoseRange(
        v.coerceIn(MIN_BOUND_MMOL, range.high - MIN_GAP_MMOL), range.high
    )
    fun setHigh(v: Float) = viewModel.setGlucoseRange(
        range.low, v.coerceIn(range.low + MIN_GAP_MMOL, MAX_BOUND_MMOL)
    )

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Text(stringResource(R.string.glucose_range_label), style = GlukoType.Label, modifier = Modifier.weight(1f))
        Text(stringResource(R.string.mmol_unit), style = GlukoType.Hint)
    }
    Spacer(Modifier.height(13.dp))
    Text(
        "%.1f — %.1f".format(range.low, range.high),
        style = GlukoType.ScreenTitle.copy(fontSize = 30.sp).tabular
    )
    Spacer(Modifier.height(14.dp))
    RangeLine(range.low, range.high)
    Spacer(Modifier.height(16.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(GlukoSpacing.itemGap)) {
        BoundTile(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.glucose_range_low_label),
            value = range.low,
            onMinus = { setLow(range.low - STEP_MMOL) },
            onPlus = { setLow(range.low + STEP_MMOL) }
        )
        BoundTile(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.glucose_range_high_label),
            value = range.high,
            onMinus = { setHigh(range.high - STEP_MMOL) },
            onPlus = { setHigh(range.high + STEP_MMOL) }
        )
    }
    Spacer(Modifier.height(11.dp))
    Text(stringResource(R.string.glucose_range_hint), style = GlukoType.Hint)
}

@Composable
private fun RangeLine(low: Float, high: Float) {
    val span = MAX_BOUND_MMOL - MIN_BOUND_MMOL
    val start = ((low - MIN_BOUND_MMOL) / span).coerceIn(0f, 1f)
    val end = ((high - MIN_BOUND_MMOL) / span).coerceIn(0f, 1f)

    Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(GlukoRadius.pill)).background(GlukoColors.Divider)) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val width = maxWidth
            Box(
                Modifier
                    .padding(start = width * start)
                    .width(width * (end - start))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(GlukoRadius.pill))
                    .background(GlukoColors.Ink)
            )
        }
    }
}

@Composable
private fun BoundTile(modifier: Modifier, label: String, value: Float, onMinus: () -> Unit, onPlus: () -> Unit) {
    GlukoTile(modifier) {
        Text(label, style = GlukoType.CardLabel.copy(fontSize = 11.sp))
        Spacer(Modifier.height(7.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            CircleButton(28.dp, GlukoColors.Surface, onMinus) {
                LucideIcon(LucidePaths.Minus, 12.dp, strokeWidth = 2.4f)
            }
            Text(
                "%.1f".format(value),
                style = GlukoType.ValueSmall.copy(fontSize = 16.sp).tabular,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            CircleButton(28.dp, GlukoColors.Surface, onPlus) {
                LucideIcon(LucidePaths.Plus, 12.dp, strokeWidth = 2.4f)
            }
        }
    }
}
