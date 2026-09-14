package com.neojelll.diaxtracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoSpacing
import com.neojelll.diaxtracker.ui.theme.GlukoType
import com.neojelll.diaxtracker.ui.theme.tabular
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun YesterdaySummaryCard(entries: List<DiaryEntry>) {
    if (entries.isEmpty()) return
    var dismissed by rememberSaveable(entries) { mutableStateOf(false) }
    if (dismissed) return

    GlukoCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Kicker(stringResource(R.string.yesterday_summary_title), Modifier.weight(1f))
            CircleButton(24.dp, GlukoColors.Tile, { dismissed = true }) {
                LucideIcon(LucidePaths.Close, 12.dp, strokeWidth = 2.2f)
            }
        }
        Spacer(Modifier.height(GlukoSpacing.itemGap))
        Column(verticalArrangement = Arrangement.spacedBy(GlukoSpacing.itemGap)) {
            entries.forEach { entry -> YesterdaySummaryRow(entry) }
        }
    }
    Spacer(Modifier.height(GlukoSpacing.cardGap))
}

@Composable
private fun YesterdaySummaryRow(entry: DiaryEntry) {
    val parts = buildList {
        entry.bloodSugar?.let { add(stringResource(R.string.sugar_value_format, "%.1f".format(Locale.US, it))) }
        if (entry.mealLabel == null) {
            entry.breadUnits?.let { add(stringResource(R.string.bread_units_value_format, "%.1f".format(Locale.US, it))) }
        }
        entry.shortInsulinDose?.let { add(stringResource(R.string.dose_value_format, it.toString())) }
        entry.longInsulinDose?.let { add(stringResource(R.string.dose_value_format, it.toString())) }
    }

    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(entry.createdAt.format(DateTimeFormatter.ofPattern("HH:mm")), style = GlukoType.CardLabel.tabular)
            if (parts.isNotEmpty()) {
                Text(parts.joinToString(" · "), style = GlukoType.Body.tabular)
            }
        }
        entry.mealLabel?.let { Text(it, style = GlukoType.Hint) }
        if (entry.notes.isNotBlank()) {
            Text(entry.notes, style = GlukoType.Hint)
        }
    }
}
