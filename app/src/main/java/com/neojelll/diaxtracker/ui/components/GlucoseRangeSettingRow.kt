package com.neojelll.diaxtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.ui.screens.formatAmount
import com.neojelll.diaxtracker.ui.theme.CardDivider
import com.neojelll.diaxtracker.ui.theme.FieldTile
import com.neojelll.diaxtracker.ui.theme.GlucoIcons
import com.neojelll.diaxtracker.ui.theme.Ink
import com.neojelll.diaxtracker.ui.theme.TextLabel
import com.neojelll.diaxtracker.ui.theme.TextTertiary
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel
import kotlin.math.round

// A personal target can't start below the fixed clinical hypoglycemia threshold.
private const val MIN_BOUND_MMOL = 3.9f
private const val MAX_BOUND_MMOL = 15f
private const val SCALE_MIN = 3.0f
private const val SCALE_MAX = 15.0f
private const val STEP = 0.1f
private const val MIN_GAP = 0.5f

@Composable
fun GlucoseRangeSettingRow(viewModel: DiaryViewModel) {
    val range by viewModel.glucoseRange.collectAsState()

    fun setLow(newLow: Float) {
        val clamped = newLow.coerceIn(MIN_BOUND_MMOL, range.high - MIN_GAP)
        viewModel.setGlucoseRange(round(clamped * 10) / 10f, range.high)
    }
    fun setHigh(newHigh: Float) {
        val clamped = newHigh.coerceIn(range.low + MIN_GAP, MAX_BOUND_MMOL)
        viewModel.setGlucoseRange(range.low, round(clamped * 10) / 10f)
    }

    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.target_range_title), fontSize = 13.sp, color = TextLabel)
            Text(stringResource(R.string.mmol_unit), fontSize = 11.5.sp, color = TextTertiary)
        }
        Text(
            stringResource(R.string.range_value_format, formatAmount(range.low), formatAmount(range.high)),
            fontSize = 30.sp,
            fontWeight = FontWeight.Medium,
            color = Ink,
            modifier = Modifier.padding(top = 14.dp, bottom = 14.dp)
        )

        val leftFraction = ((range.low - SCALE_MIN) / (SCALE_MAX - SCALE_MIN)).coerceIn(0f, 1f)
        val rightFraction = ((range.high - SCALE_MIN) / (SCALE_MAX - SCALE_MIN)).coerceIn(0f, 1f)
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(CardDivider)
        ) {
            Box(
                Modifier
                    .offset(x = maxWidth * leftFraction)
                    .width(maxWidth * (rightFraction - leftFraction))
                    .height(6.dp)
                    .background(Ink)
            )
        }

        Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            BoundTile(
                label = stringResource(R.string.lower_bound_label),
                value = range.low,
                onDecrement = { setLow(range.low - STEP) },
                onIncrement = { setLow(range.low + STEP) },
                modifier = Modifier.weight(1f)
            )
            BoundTile(
                label = stringResource(R.string.upper_bound_label),
                value = range.high,
                onDecrement = { setHigh(range.high - STEP) },
                onIncrement = { setHigh(range.high + STEP) },
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            stringResource(R.string.range_out_of_range_hint),
            fontSize = 11.5.sp,
            color = TextTertiary,
            lineHeight = 16.sp,
            modifier = Modifier.padding(top = 11.dp)
        )
    }
}

@Composable
private fun BoundTile(
    label: String,
    value: Float,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(FieldTile)
            .padding(horizontal = 13.dp, vertical = 11.dp)
    ) {
        Text(label, fontSize = 11.sp, color = TextLabel, modifier = Modifier.padding(bottom = 7.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            StepperCircle(icon = GlucoIcons.Minus, onClick = onDecrement)
            Text(formatAmount(value), fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Ink)
            StepperCircle(icon = GlucoIcons.Plus, onClick = onIncrement)
        }
    }
}

@Composable
private fun StepperCircle(icon: ImageVector, onClick: () -> Unit) {
    Box(
        Modifier.size(28.dp).clip(CircleShape).background(Color.White).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Ink, modifier = Modifier.size(12.dp))
    }
}
