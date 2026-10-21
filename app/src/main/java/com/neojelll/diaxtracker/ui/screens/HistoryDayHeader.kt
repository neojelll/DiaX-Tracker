package com.neojelll.diaxtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.CarbUnit
import com.neojelll.diaxtracker.data.carbGoalReached
import com.neojelll.diaxtracker.insulin.DayInsulin
import com.neojelll.diaxtracker.time.AppClock
import com.neojelll.diaxtracker.ui.components.InsulinIcon
import com.neojelll.diaxtracker.ui.components.InsulinKind
import com.neojelll.diaxtracker.ui.components.Kicker
import com.neojelll.diaxtracker.ui.components.LocalCarbDisplay
import com.neojelll.diaxtracker.ui.components.LucideIcon
import com.neojelll.diaxtracker.ui.components.LucidePaths
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoType
import com.neojelll.diaxtracker.ui.theme.tabular
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * A day in History: "СЕГОДНЯ —— totals". The totals are the day's carbohydrates, short and long
 * insulin - each only when there is some - in outlined capsules with the record icons, quieter than
 * the records under them. With a carbohydrate goal on and reached that day, the carbs get a tick.
 * Drawn on the screen colour: it's pinned while its day scrolls under it.
 */
@Composable
internal fun DayHeaderRow(date: LocalDate, carbsGrams: Float, insulin: DayInsulin, carbGoalGrams: Float?) {
    val locale = LocalConfiguration.current.locales[0]
    val today = AppClock.today()
    val label = when (date) {
        today -> stringResource(R.string.today_label)
        today.minusDays(1) -> stringResource(R.string.yesterday_label)
        else -> date.format(DateTimeFormatter.ofPattern("d MMMM", locale))
    }
    Column(Modifier.fillMaxWidth().background(GlukoColors.Screen).padding(top = 6.dp, bottom = 10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Kicker(label)
            Spacer(Modifier.width(8.dp))
            Box(Modifier.weight(1f).height(1.dp).background(GlukoColors.BorderDashed))
            Spacer(Modifier.width(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                if (carbsGrams > 0f) CarbsTotal(carbsGrams, reached = carbGoalGrams != null && carbGoalReached(carbsGrams, carbGoalGrams))
                if (insulin.short > 0f) InsulinTotal(InsulinKind.SHORT, insulin.short)
                insulin.long?.let { InsulinTotal(InsulinKind.LONG, it) }
            }
        }
    }
}

@Composable
private fun CarbsTotal(grams: Float, reached: Boolean) {
    val display = LocalCarbDisplay.current
    val format = stringResource(
        when (display.unit) {
            CarbUnit.XE -> R.string.bread_units_value_format
            CarbUnit.GRAMS -> R.string.carbs_grams_value_format
        }
    )
    val text = numberWithUnit(format, display.format(grams))
    val goal = if (reached) ", " + stringResource(R.string.summary_carbs_goal_reached) else ""
    TotalCapsule(text.text + goal) {
        LucideIcon(LucidePaths.Dish, 13.dp, GlukoColors.TextSecondary, strokeWidth = 2f)
        Spacer(Modifier.width(2.dp))
        Text(text, style = TotalText, maxLines = 1)
        if (reached) {
            Spacer(Modifier.width(3.dp))
            LucideIcon(LucidePaths.Check, 10.dp, GlukoColors.Ink, strokeWidth = 3f)
        }
    }
}

@Composable
private fun InsulinTotal(kind: InsulinKind, units: Float) {
    val unit = stringResource(R.string.units_short)
    val kindLabel = stringResource(if (kind == InsulinKind.SHORT) R.string.record_short_label else R.string.record_long_label)
    val text = numberWithUnit("%1\$s $unit", formatAmount(units))
    TotalCapsule("${text.text}, $kindLabel") {
        InsulinIcon(kind, 13.dp)
        Spacer(Modifier.width(1.dp))
        Text(text, style = TotalText, maxLines = 1)
    }
}

/** An outlined capsule - the day's total, a step quieter than the grey value capsules of the records. */
@Composable
private fun TotalCapsule(description: String, content: @Composable () -> Unit) {
    Row(
        Modifier
            .border(1.dp, GlukoColors.Border, TotalShape)
            .clearAndSetSemantics { contentDescription = description }
            .padding(start = 5.dp, end = 7.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) { content() }
}

private val TotalShape = RoundedCornerShape(9.dp)
private val TotalText = GlukoType.Hint.copy(fontSize = 11.sp, color = GlukoColors.TextSecondary).tabular
