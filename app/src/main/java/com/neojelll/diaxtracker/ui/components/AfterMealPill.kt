package com.neojelll.diaxtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.ui.aftermeal.AfterMeal
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoRadius
import com.neojelll.diaxtracker.ui.theme.GlukoType
import com.neojelll.diaxtracker.ui.theme.tabular
import java.util.Locale

private val SegmentShadow = Color(0x14000000)

private class Segment(val hour: Int, val value: Double?, val isPeak: Boolean)

private fun AfterMeal.segments(): List<Segment> {
    val peak = values.filterNotNull().maxOrNull()
    val peakIndex = if (peak == null) -1 else values.indexOfFirst { it == peak }
    return values.mapIndexed { i, v -> Segment(hour = i + 1, value = v, isPeak = i == peakIndex) }
}

private fun format(value: Double) = String.format(Locale.US, "%.1f", value)

/**
 * "Sugar after a meal": four segments, +1h..+4h after the meal, on the meal's history card.
 * The highest reading is the peak (white segment); a check not taken yet is "—".
 */
@Composable
fun AfterMealPill(data: AfterMeal, modifier: Modifier = Modifier) {
    val segments = data.segments()
    val peak = segments.firstOrNull { it.isPeak }?.value
    val caption = if (peak == null) stringResource(R.string.after_meal_none) else stringResource(R.string.after_meal_peak, format(peak))

    Column(modifier.fillMaxWidth()) {
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Text(stringResource(R.string.after_meal_label), style = GlukoType.CardLabel.tabular, modifier = Modifier.weight(1f))
            Text(caption, style = GlukoType.CardLabel.tabular)
        }
        Spacer(Modifier.height(7.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(GlukoRadius.pill))
                .background(GlukoColors.Tile)
                .padding(3.dp)
        ) {
            segments.forEach { SegmentCell(it, Modifier.weight(1f)) }
        }
        Spacer(Modifier.height(12.dp))
        GlukoDivider()
    }
}

@Composable
private fun SegmentCell(segment: Segment, modifier: Modifier) {
    val shape = RoundedCornerShape(GlukoRadius.pill)
    val base = if (segment.isPeak) {
        modifier.shadow(2.dp, shape, ambientColor = SegmentShadow, spotColor = SegmentShadow)
            .clip(shape).background(GlukoColors.Surface)
    } else modifier

    Row(
        base.padding(vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(stringResource(R.string.after_meal_hour, segment.hour), style = GlukoType.Unit.tabular)
        Text(
            segment.value?.let(::format) ?: "—",
            style = GlukoType.Body.copy(
                fontSize = 13.sp,
                fontWeight = if (segment.isPeak) FontWeight.Medium else FontWeight.Normal,
                color = if (segment.value != null) GlukoColors.Ink else GlukoColors.Placeholder
            ).tabular
        )
    }
}
