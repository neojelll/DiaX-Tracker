package com.neojelll.diaxtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.insulin.DayInsulin
import com.neojelll.diaxtracker.sensor.LatestSensorReading
import com.neojelll.diaxtracker.sensor.SensorFreshness
import com.neojelll.diaxtracker.sensor.sensorFreshness
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoRadius
import com.neojelll.diaxtracker.ui.theme.Ruda

/**
 * The day summary above the entry form: one white card, columns split by vertical hairlines, each
 * reading label 11sp -> value -> note 10.5sp: sugar, today's carbohydrates, today's insulin.
 */
@Composable
fun DaySummaryCard(sensorReading: LatestSensorReading?, nowMillis: Long, carbsGramsToday: Float, insulinToday: DayInsulin) {
    GlukoCard(padding = PaddingValues(start = 14.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            SugarColumn(sensorReading, nowMillis, Modifier.weight(1f).padding(end = 10.dp))
            VerticalHairline()
            CarbsColumn(carbsGramsToday, Modifier.weight(1f).padding(horizontal = 10.dp))
            VerticalHairline()
            InsulinColumn(insulinToday, Modifier.weight(1f).padding(start = 10.dp))
        }
    }
}

/**
 * Two lines, label left and number right, a hairline between them. No total on purpose: short and
 * long don't add up to anything meaningful. The long line hides when none was logged today.
 */
@Composable
private fun InsulinColumn(insulin: DayInsulin, modifier: Modifier) {
    SummaryColumn(modifier, stringResource(R.string.summary_insulin_label)) {
        InsulinLine(stringResource(R.string.summary_insulin_short), formatSummaryValue(insulin.short))
        insulin.long?.let {
            Spacer(Modifier.height(5.dp))
            GlukoDivider()
            Spacer(Modifier.height(5.dp))
            InsulinLine(stringResource(R.string.summary_insulin_long), formatSummaryValue(it))
        }
    }
}

@Composable
private fun InsulinLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Text(label, style = SummarySub, modifier = Modifier.weight(1f), maxLines = 1)
        Text(value, style = SummaryValue.copy(fontSize = 15.sp))
    }
}

/** Today's carbohydrates in the unit from the settings: number, the unit small beside it, "за день" under. */
@Composable
private fun CarbsColumn(gramsToday: Float, modifier: Modifier) {
    SummaryColumn(modifier, stringResource(R.string.summary_carbs_label)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(LocalCarbDisplay.current.format(gramsToday), style = SummaryValue)
            Spacer(Modifier.width(2.dp))
            Text(carbsUnitLabel(), style = SummarySub)
        }
        Spacer(Modifier.height(6.dp))
        Text(stringResource(R.string.summary_carbs_today), style = SummarySub)
    }
}

@Composable
private fun VerticalHairline() {
    Box(Modifier.width(1.dp).fillMaxHeight().background(GlukoColors.Divider))
}

/**
 * Fresh: green dot and "только что". Stale: the value greyed, grey dot, "N мин назад".
 * No sensor: "—" and "нет данных".
 */
@Composable
private fun SugarColumn(reading: LatestSensorReading?, nowMillis: Long, modifier: Modifier) {
    val freshness = sensorFreshness(reading, nowMillis)
    val shown = reading.takeIf { freshness != SensorFreshness.NONE }
    SummaryColumn(modifier, stringResource(R.string.summary_sugar_label)) {
        Text(
            shown?.let { formatSummaryValue(it.mmol) } ?: "—",
            style = SummaryValue.copy(color = if (freshness == SensorFreshness.STALE) GlukoColors.Placeholder else GlukoColors.Ink)
        )
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            val fresh = freshness == SensorFreshness.FRESH
            Box(
                Modifier.size(6.dp).clip(RoundedCornerShape(GlukoRadius.pill))
                    .background(if (fresh) GlukoColors.SensorFreshDot else GlukoColors.SensorStaleDot)
            )
            Spacer(Modifier.width(4.dp))
            val note = when (freshness) {
                SensorFreshness.FRESH -> stringResource(R.string.summary_sensor_just_now)
                SensorFreshness.STALE -> sensorAge(nowMillis - reading!!.atMillis).let { (res, n) -> stringResource(res, n) }
                SensorFreshness.NONE -> stringResource(R.string.summary_sensor_no_data)
            }
            Text(
                note,
                style = SummarySub.copy(color = if (fresh) GlukoColors.SensorFreshText else GlukoColors.TextTertiary),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SummaryColumn(modifier: Modifier, label: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier) {
        Text(label, style = SummaryLabel, maxLines = 1)
        Spacer(Modifier.height(5.dp))
        content()
    }
}

/** "N мин / ч / дн. назад": minutes under an hour, hours under a day, then days (stale lasts up to 3). */
internal fun sensorAge(ageMillis: Long): Pair<Int, Long> {
    val minutes = ageMillis / 60_000
    return when {
        minutes < 60 -> R.string.summary_sensor_minutes_ago to minutes
        minutes < 24 * 60 -> R.string.summary_sensor_hours_ago to minutes / 60
        else -> R.string.summary_sensor_days_ago to minutes / (24 * 60)
    }
}

/** One decimal, without a trailing ",0": 6,4 / 7. */
internal fun formatSummaryValue(value: Float): String =
    if (value % 1f == 0f) value.toInt().toString() else "%.1f".format(value)

private val SummaryLabel = TextStyle(fontFamily = Ruda, fontSize = 11.sp, color = GlukoColors.TextLabel)
private val SummarySub = TextStyle(fontFamily = Ruda, fontSize = 10.5.sp, color = GlukoColors.TextTertiary)
private val SummaryValue = TextStyle(
    fontFamily = Ruda, fontWeight = FontWeight.Medium, fontSize = 21.sp,
    letterSpacing = (-0.02).em, lineHeight = 21.sp, fontFeatureSettings = "tnum", color = GlukoColors.Ink
)
