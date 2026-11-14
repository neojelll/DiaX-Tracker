package com.neojelll.diaxtracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.aftermeal.AfterMealPoint
import com.neojelll.diaxtracker.aftermeal.afterMealPeak
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoRadius
import com.neojelll.diaxtracker.ui.theme.GlukoType
import com.neojelll.diaxtracker.ui.theme.Ruda
import com.neojelll.diaxtracker.ui.theme.SproutGreen
import com.neojelll.diaxtracker.ui.theme.glucoseColor
import java.time.format.DateTimeFormatter

/**
 * "Sugar after the meal" in an opened record: a quiet line - the sugar logged with the meal (pale),
 * then +1h..+4h, a number over each point, the peak a little bolder. No range band, no axis: a
 * diary entry, not a monitor. Colour only where it matters - a peak out of range gets its range colour.
 * A check not in yet shows the time it's due instead of a point.
 */
@Composable
fun AfterMealLine(points: List<AfterMealPoint>, rangeLow: Float, rangeHigh: Float, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val peak = afterMealPeak(points)
    val labels = points.map {
        if (it.hour == 0) stringResource(R.string.after_meal_before) else stringResource(R.string.after_meal_hour, it.hour)
    }
    val readable = points.filter { it.value != null }.joinToString(", ") { p ->
        "${labels[points.indexOf(p)]} ${"%.1f".format(p.value)}"
    }

    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GlukoRadius.field))
            .background(GlukoColors.Tile)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(stringResource(R.string.after_meal_title), style = GlukoType.CardLabel)
        if (points.none { it.hour > 0 && (it.value != null || it.dueAt != null) }) {
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.after_meal_none), style = GlukoType.Hint)
        } else Canvas(
            Modifier
                .fillMaxWidth()
                .height(88.dp)
                .padding(top = 4.dp)
                .semantics { contentDescription = readable }
        ) {
            drawLine(points, labels, peak, rangeLow, rangeHigh, measurer)
        }
    }
}

private val TIME = DateTimeFormatter.ofPattern("HH:mm")

private fun DrawScope.drawLine(
    points: List<AfterMealPoint>,
    labels: List<String>,
    peak: Int?,
    rangeLow: Float,
    rangeHigh: Float,
    measurer: TextMeasurer
) {
    val side = 18.dp.toPx()
    val top = 20.dp.toPx()
    val bottom = size.height - 20.dp.toPx()
    val step = (size.width - side * 2) / (points.size - 1)
    val x = { i: Int -> side + i * step }
    val values = points.mapNotNull { it.value }
    // Room around the readings, but never squeeze a normal day into a steep-looking line.
    val lo = minOf(4.0, (values.minOrNull() ?: 4.0) - 0.5)
    val hi = maxOf(11.0, (values.maxOrNull() ?: 11.0) + 0.5)
    val y = { v: Double -> (bottom - (v - lo) / (hi - lo) * (bottom - top)).toFloat() }

    val line = Path()
    points.withIndex().filter { it.value.value != null }.forEachIndexed { n, (i, p) ->
        if (n == 0) line.moveTo(x(i), y(p.value!!)) else line.lineTo(x(i), y(p.value!!))
    }
    drawPath(line, GlukoColors.Ink, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

    points.forEachIndexed { i, p ->
        val cx = x(i)
        val value = p.value
        if (value != null) {
            val isPeak = i == peak
            val before = p.hour == 0
            val tint = when {
                before -> GlukoColors.Placeholder
                isPeak -> glucoseColor(value.toFloat(), rangeLow, rangeHigh).takeIf { it != SproutGreen } ?: GlukoColors.Ink
                else -> GlukoColors.Ink
            }
            drawCircle(if (before) GlukoColors.Placeholder else GlukoColors.Ink, (if (isPeak) 3.5.dp else 2.5.dp).toPx(), Offset(cx, y(value)))
            val style = NumberStyle.copy(
                color = tint,
                fontSize = if (isPeak) 12.sp else 11.sp,
                fontWeight = if (isPeak) FontWeight.SemiBold else FontWeight.Normal
            )
            centered(measurer, "%.1f".format(value), style, cx, y(value) - 6.dp.toPx())
        } else if (p.dueAt != null) {
            centered(measurer, p.dueAt.format(TIME), DueStyle, cx, bottom - 6.dp.toPx())
        }
        centered(measurer, labels[i], LabelStyle, cx, size.height)
    }
}

/** Draws [text] centred on [cx], its bottom edge at [bottomY]. */
private fun DrawScope.centered(measurer: TextMeasurer, text: String, style: TextStyle, cx: Float, bottomY: Float) {
    val layout = measurer.measure(text, style)
    drawText(layout, topLeft = Offset(cx - layout.size.width / 2f, bottomY - layout.size.height))
}

private val NumberStyle = TextStyle(fontFamily = Ruda, fontFeatureSettings = "tnum")
private val DueStyle = TextStyle(fontFamily = Ruda, fontSize = 10.sp, color = GlukoColors.Placeholder, fontFeatureSettings = "tnum")
private val LabelStyle = TextStyle(fontFamily = Ruda, fontSize = 9.5.sp, color = GlukoColors.TextTertiary)

