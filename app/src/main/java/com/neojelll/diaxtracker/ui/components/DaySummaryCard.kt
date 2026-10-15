package com.neojelll.diaxtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
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
 * The day summary above the entry form: three tiles in a row, each label 11sp -> value -> note
 * 10.5sp. Sugar sits on ink - the screen's one dark accent; carbs and insulin on white.
 */
@Composable
fun DaySummaryCard(sensorReading: LatestSensorReading?, nowMillis: Long, carbsGramsToday: Float, insulinToday: DayInsulin) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SugarTile(sensorReading, nowMillis, Modifier.weight(1.25f).fillMaxHeight())
        CarbsTile(carbsGramsToday, Modifier.weight(1f).fillMaxHeight())
        InsulinTile(insulinToday, Modifier.weight(1f).fillMaxHeight())
    }
}

/**
 * "12 / 18": short, then long muted after a slash. No total on purpose: short and long don't add
 * up to anything meaningful. Only the short number when no long was logged today.
 */
@Composable
private fun InsulinTile(insulin: DayInsulin, modifier: Modifier) {
    LightTile(modifier, stringResource(R.string.summary_insulin_label)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(formatSummaryValue(insulin.short), style = SummaryValue, maxLines = 1)
            insulin.long?.let {
                Text(" / ${formatSummaryValue(it)}", style = SummaryValue.copy(fontSize = 14.sp, color = GlukoColors.TextTertiary), maxLines = 1)
            }
        }
        Spacer(Modifier.height(6.dp))
        val note = if (insulin.long != null) R.string.summary_insulin_split else R.string.summary_insulin_short
        Text(stringResource(note), style = SummarySub, maxLines = 1)
    }
}

/** Today's carbohydrates in the unit from the settings; the unit goes into the note: "ХЕ за день". */
@Composable
private fun CarbsTile(gramsToday: Float, modifier: Modifier) {
    LightTile(modifier, stringResource(R.string.summary_carbs_label)) {
        Text(LocalCarbDisplay.current.format(gramsToday), style = SummaryValue, maxLines = 1)
        Spacer(Modifier.height(6.dp))
        Text("${carbsUnitLabel()} ${stringResource(R.string.summary_carbs_today)}", style = SummarySub, maxLines = 1)
    }
}

/**
 * On ink. Fresh: green dot and "только что". Stale: the value muted, grey dot, "N мин назад".
 * No sensor: "—" and "нет данных". Logging, not monitoring - no trend arrow.
 */
@Composable
private fun SugarTile(reading: LatestSensorReading?, nowMillis: Long, modifier: Modifier) {
    val freshness = sensorFreshness(reading, nowMillis)
    val shown = reading.takeIf { freshness != SensorFreshness.NONE }
    val fresh = freshness == SensorFreshness.FRESH
    Column(
        modifier
            .clip(RoundedCornerShape(GlukoRadius.panel))
            .background(GlukoColors.Ink)
            .padding(horizontal = 12.dp, vertical = 11.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(6.dp).clip(RoundedCornerShape(GlukoRadius.pill))
                    .background(if (fresh) GlukoColors.SensorFreshDot else GlukoColors.SensorStaleDot)
            )
            Spacer(Modifier.width(5.dp))
            Text(stringResource(R.string.summary_sugar_label), style = SummaryLabel.copy(color = GlukoColors.TextOnDark), maxLines = 1)
        }
        Spacer(Modifier.height(5.dp))
        Text(
            shown?.let { formatSummaryValue(it.mmol) } ?: "—",
            style = SummaryValue.copy(fontSize = 26.sp, lineHeight = 26.sp, color = if (fresh) GlukoColors.Surface else GlukoColors.Placeholder),
            maxLines = 1
        )
        Spacer(Modifier.height(5.dp))
        val note = when (freshness) {
            SensorFreshness.FRESH -> stringResource(R.string.summary_sensor_just_now)
            SensorFreshness.STALE -> sensorAge(nowMillis - reading!!.atMillis).let { (res, n) -> stringResource(res, n) }
            SensorFreshness.NONE -> stringResource(R.string.summary_sensor_no_data)
        }
        Text(note, style = SummarySub.copy(color = GlukoColors.TextOnDark), maxLines = 1)
    }
}

/** A white tile with the card's soft shadow: label, then [content]. */
@Composable
private fun LightTile(modifier: Modifier, label: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .shadowSoft(GlukoRadius.panel)
            .clip(RoundedCornerShape(GlukoRadius.panel))
            .background(GlukoColors.Surface)
            .padding(horizontal = 12.dp, vertical = 11.dp)
    ) {
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
