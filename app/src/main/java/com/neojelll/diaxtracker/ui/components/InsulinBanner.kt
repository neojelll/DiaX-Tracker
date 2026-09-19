package com.neojelll.diaxtracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoRadius
import com.neojelll.diaxtracker.ui.theme.GlukoSpacing
import com.neojelll.diaxtracker.ui.theme.GlukoType
import com.neojelll.diaxtracker.ui.theme.tabular
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.exp

private const val TICK_MILLIS = 30_000L

// Time to peak activity for a rapid-acting analog (Humalog/NovoRapid), matching OpenAPS's own
// "rapid-acting" default. Not exposed as a setting - only the overall duration is, per request.
private const val PEAK_MINUTES = 75f

private data class InsulinOnBoard(val units: Float, val minutesLeft: Long, val fromTime: LocalDateTime, val durationMinutes: Float) {
    val progress: Float get() = (minutesLeft / durationMinutes).coerceIn(0f, 1f)
}

/**
 * Fraction of a single dose still on board `elapsedMinutes` after it was taken, using the
 * "scalable exponential" model (Maksimovic; used by Loop/AndroidAPS/OpenAPS) rather than a flat
 * cutoff: activity peaks at [peakMinutes] and both activity and IOB reach exactly zero at
 * [durationMinutes], giving a realistic decay curve instead of a cliff-edge.
 */
private fun insulinOnBoardFraction(elapsedMinutes: Float, peakMinutes: Float, durationMinutes: Float): Float {
    if (elapsedMinutes <= 0f) return 1f
    if (elapsedMinutes >= durationMinutes) return 0f
    val tau = peakMinutes * (1 - peakMinutes / durationMinutes) / (1 - 2 * peakMinutes / durationMinutes)
    val a = 2 * tau / durationMinutes
    val s = 1 / (1 - a + (1 + a) * exp(-durationMinutes / tau))
    val t = elapsedMinutes
    return (1 - s * (1 - a) * ((t * t / (tau * durationMinutes * (1 - a)) - t / tau - 1) * exp(-t / tau) + 1))
        .coerceIn(0f, 1f)
}

private fun activeInsulin(entries: List<DiaryEntry>, now: LocalDateTime, durationHours: Float): InsulinOnBoard? {
    if (entries.isEmpty()) return null
    val durationMinutes = durationHours * 60f
    val active = entries.mapNotNull { entry ->
        val dose = entry.shortInsulinDose ?: return@mapNotNull null
        val elapsed = Duration.between(entry.createdAt, now).toMinutes().toFloat()
        if (elapsed < 0f || elapsed >= durationMinutes) return@mapNotNull null
        val onBoard = dose * insulinOnBoardFraction(elapsed, PEAK_MINUTES, durationMinutes)
        Triple(entry.createdAt, onBoard, durationMinutes - elapsed)
    }
    if (active.isEmpty()) return null
    return InsulinOnBoard(
        units = active.sumOf { it.second.toDouble() }.toFloat(),
        minutesLeft = active.maxOf { it.third }.toLong(),
        fromTime = active.maxOf { it.first },
        durationMinutes = durationMinutes
    )
}

/** Insulin-on-board banner, shown in the stream on every tab. Renders nothing once decayed. */
@Composable
fun InsulinBanner(
    entries: List<DiaryEntry>,
    durationHours: Float,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    var now by remember(entries.map { it.id }) { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(entries.map { it.id }) {
        while (true) {
            now = LocalDateTime.now()
            delay(TICK_MILLIS)
        }
    }
    val iob = activeInsulin(entries, now, durationHours) ?: return

    Box(modifier.padding(horizontal = GlukoSpacing.screenHorizontal).padding(top = 8.dp)) {
        AnimatedVisibility(expanded, enter = fadeIn(), exit = fadeOut()) {
            ExpandedCard(iob, onToggle)
        }
        AnimatedVisibility(!expanded, enter = fadeIn(), exit = fadeOut()) {
            CollapsedStrip(iob, onToggle)
        }
    }
}

@Composable
private fun leftLabel(minutesLeft: Long): String {
    val hours = minutesLeft / 60
    val minutes = minutesLeft % 60
    return if (hours > 0) {
        stringResource(R.string.duration_hours_minutes, hours, minutes)
    } else {
        stringResource(R.string.duration_minutes, minutes)
    }
}

@Composable
private fun ExpandedCard(iob: InsulinOnBoard, onCollapse: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GlukoRadius.panel))
            .background(GlukoColors.Ink)
            .clickable(onClick = onCollapse)
            .padding(start = 15.dp, end = 15.dp, top = 13.dp, bottom = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircleButton(26.dp, GlukoColors.InkSoft) {
                LucideIcon(LucidePaths.Syringe, 13.dp, GlukoColors.Surface, strokeWidth = 2f)
            }
            Spacer(Modifier.width(9.dp))
            Text(
                stringResource(R.string.insulin_banner_title),
                style = GlukoType.Body.copy(color = GlukoColors.Surface),
                modifier = Modifier.weight(1f)
            )
            CircleButton(26.dp, GlukoColors.InkSoft) {
                LucideIcon(LucidePaths.ChevronUp, 12.dp, GlukoColors.Surface, strokeWidth = 2.2f)
            }
        }

        Spacer(Modifier.height(11.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                "%.1f".format(iob.units),
                style = GlukoType.DisplayInsulin.copy(color = GlukoColors.Surface).tabular
            )
            Spacer(Modifier.width(5.dp))
            Text(stringResource(R.string.insulin_units_suffix), style = GlukoType.CardLabel.copy(color = GlukoColors.TextOnDark))
        }

        Spacer(Modifier.height(10.dp))
        ProgressLine(iob.progress)
        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                stringResource(R.string.insulin_since_format, iob.fromTime.format(DateTimeFormatter.ofPattern("HH:mm"))),
                style = GlukoType.CardLabel.copy(color = GlukoColors.TextOnDark)
            )
            Text(
                stringResource(R.string.insulin_left_format, leftLabel(iob.minutesLeft)),
                style = GlukoType.CardLabel.copy(color = GlukoColors.TextOnDark)
            )
        }
    }
}

@Composable
private fun CollapsedStrip(iob: InsulinOnBoard, onExpand: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GlukoRadius.strip))
            .background(GlukoColors.Ink)
            .clickable(onClick = onExpand)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LucideIcon(LucidePaths.Syringe, 12.dp, GlukoColors.Surface, strokeWidth = 2f)
        Spacer(Modifier.width(8.dp))
        Text(
            "%.1f".format(iob.units),
            style = GlukoType.Body.copy(color = GlukoColors.Surface, fontSize = 12.sp).tabular
        )
        Spacer(Modifier.width(3.dp))
        Text(stringResource(R.string.insulin_units_suffix), style = GlukoType.Unit.copy(color = GlukoColors.TextOnDark))
        Spacer(Modifier.weight(1f))
        Text(
            stringResource(R.string.insulin_left_format, leftLabel(iob.minutesLeft)),
            style = GlukoType.CardLabel.copy(color = GlukoColors.TextOnDark)
        )
        Spacer(Modifier.width(6.dp))
        LucideIcon(LucidePaths.ChevronDown, 11.dp, GlukoColors.TextOnDark, strokeWidth = 2.2f)
    }
}

@Composable
private fun ProgressLine(progress: Float) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(RoundedCornerShape(GlukoRadius.pill))
            .background(GlukoColors.TrackOnDark)
    ) {
        Box(
            Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(GlukoRadius.pill))
                .background(GlukoColors.Surface)
        )
    }
}
