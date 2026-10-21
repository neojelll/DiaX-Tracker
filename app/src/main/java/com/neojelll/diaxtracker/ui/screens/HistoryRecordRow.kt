package com.neojelll.diaxtracker.ui.screens

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.CarbUnit
import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.data.SugarSource
import com.neojelll.diaxtracker.data.isMeal
import com.neojelll.diaxtracker.ui.components.InsulinIcon
import com.neojelll.diaxtracker.ui.components.InsulinKind
import com.neojelll.diaxtracker.ui.components.LocalCarbDisplay
import com.neojelll.diaxtracker.ui.components.LucideIcon
import com.neojelll.diaxtracker.ui.components.LucidePaths
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoType
import com.neojelll.diaxtracker.ui.theme.tabular

/** What a record mainly is - the word under its time. Food wins: the insulin with it is that meal's dose. */
internal enum class RecordKind(@StringRes val label: Int) {
    MEAL(R.string.record_type_meal),
    SHOT(R.string.record_type_shot),
    CHECK(R.string.record_measurement_tag)
}

internal fun DiaryEntry.recordKind(): RecordKind = when {
    isMeal() -> RecordKind.MEAL
    shortInsulinDose != null || longInsulinDose != null -> RecordKind.SHOT
    else -> RecordKind.CHECK
}

/**
 * Whether opening the record shows anything: a preset (with its composition), a photo, a note, or
 * sugar readings after the meal. Without any of these a tap goes straight to editing.
 */
internal fun DiaryEntry.hasMoreInside(hasAfterMeal: Boolean): Boolean =
    mealLabel != null || photoPath != null || notes.isNotBlank() || hasAfterMeal

/** Narrow no-break space between a number and its unit: "4 ед" stays one piece and doesn't look loose. */
private const val NUMBER_UNIT_GAP = " "

/**
 * One History record on a line: time with the record kind under it; the values as capsules (food,
 * short insulin, long insulin - only those logged); sugar in its range colour with its source under it.
 * The capsules wrap only when there's no room left (a huge font); on a phone all three fit with a
 * two-digit sugar. [expanded] null - nothing to open, no chevron; otherwise the chevron's state.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RecordRow(entry: DiaryEntry, sugarColor: Color, modifier: Modifier = Modifier, expanded: Boolean? = null) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.width(44.dp)) {
            Text(entry.createdAt.format(TIME_FORMAT), style = GlukoType.RecordTime.tabular, maxLines = 1)
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(entry.recordKind().label), style = RowCaption, maxLines = 1)
                // The chevron lives here, after the kind: the left column has room, the capsules don't.
                if (expanded != null) {
                    val turn by animateFloatAsState(if (expanded) 180f else 0f, label = "recordChevron")
                    Spacer(Modifier.width(2.dp))
                    LucideIcon(LucidePaths.ChevronDown, 10.dp, GlukoColors.Placeholder, strokeWidth = 2.4f, modifier = Modifier.rotate(turn))
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        FlowRow(
            Modifier.weight(1f).padding(top = 1.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            entry.carbsGrams?.let { FoodCapsule(it) }
            entry.shortInsulinDose?.let { InsulinCapsule(InsulinKind.SHORT, it) }
            entry.longInsulinDose?.let { InsulinCapsule(InsulinKind.LONG, it) }
        }
        Spacer(Modifier.width(8.dp))
        SugarColumn(entry, sugarColor)
    }
}

/** Sugar and, under it, where it came from: "сенсор" or "вручную". No sugar - a pale dash, no source. */
@Composable
private fun SugarColumn(entry: DiaryEntry, sugarColor: Color) {
    Column(horizontalAlignment = Alignment.End) {
        val sugar = entry.bloodSugar
        if (sugar == null) {
            Text("—", style = SugarValue.copy(color = GlukoColors.Placeholder))
        } else {
            Text("%.1f".format(sugar), style = SugarValue.copy(color = sugarColor), maxLines = 1)
            // Entries saved before the source was recorded have none - no label rather than a guess.
            val source = when (entry.sugarSource) {
                SugarSource.SENSOR -> R.string.sugar_source_sensor
                SugarSource.MANUAL -> R.string.sugar_source_manual
                null -> null
            }
            if (source != null) {
                Spacer(Modifier.height(3.dp))
                Text(stringResource(source), style = RowCaption, maxLines = 1)
            }
        }
    }
}

@Composable
private fun FoodCapsule(grams: Float) {
    val display = LocalCarbDisplay.current
    val format = stringResource(
        when (display.unit) {
            CarbUnit.XE -> R.string.bread_units_value_format
            CarbUnit.GRAMS -> R.string.carbs_grams_value_format
        }
    )
    val text = numberWithUnit(format, display.format(grams))
    Capsule(text.text) {
        LucideIcon(LucidePaths.Dish, 14.dp, GlukoColors.TextSecondary, strokeWidth = 2f)
        Spacer(Modifier.width(2.dp))
        Text(text, style = CapsuleText, maxLines = 1)
    }
}

@Composable
private fun InsulinCapsule(kind: InsulinKind, dose: Float) {
    val units = stringResource(R.string.units_short)
    val kindLabel = stringResource(if (kind == InsulinKind.SHORT) R.string.record_short_label else R.string.record_long_label)
    val text = numberWithUnit("%1\$s $units", formatAmount(dose))
    Capsule("${text.text}, $kindLabel") {
        InsulinIcon(kind, 14.dp)
        Spacer(Modifier.width(1.dp))
        Text(text, style = CapsuleText, maxLines = 1)
    }
}

/** A grey value capsule; [description] is what TalkBack reads, since the icon carries part of the meaning. */
@Composable
private fun Capsule(description: String, content: @Composable () -> Unit) {
    Row(
        Modifier
            .clip(CapsuleShape)
            .background(GlukoColors.Tile)
            .clearAndSetSemantics { contentDescription = description }
            .padding(start = 5.dp, end = 7.dp, top = 3.dp, bottom = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) { content() }
}

/** "%1$s ХЕ" + "4" -> "4 ХЕ" with the number bold and ink, the unit after a narrow no-break space. */
internal fun numberWithUnit(format: String, number: String): AnnotatedString {
    val at = format.indexOf("%1\$s")
    val prefix = if (at >= 0) format.substring(0, at) else ""
    val suffix = (if (at >= 0) format.substring(at + 4) else format).trimStart()
    return buildAnnotatedString {
        append(prefix)
        withStyle(SpanStyle(fontWeight = FontWeight.Medium, fontSize = 12.5.sp, color = GlukoColors.Ink)) { append(number) }
        if (suffix.isNotEmpty()) append(NUMBER_UNIT_GAP + suffix)
    }
}

private val TIME_FORMAT = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
private val CapsuleShape = RoundedCornerShape(9.dp)
private val CapsuleText = GlukoType.Hint.copy(fontSize = 11.5.sp, color = GlukoColors.TextSecondary).tabular
private val RowCaption = GlukoType.Unit.copy(fontSize = 10.sp, color = GlukoColors.Placeholder)
private val SugarValue = GlukoType.Value.copy(fontSize = 18.sp).tabular
