package com.neojelll.diaxtracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.neojelll.diaxtracker.ui.theme.AccentGreen
import com.neojelll.diaxtracker.ui.theme.TextPrimary
import com.neojelll.diaxtracker.ui.theme.TextSecondary
import com.neojelll.diaxtracker.ui.theme.card
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun YesterdaySummaryCard(entries: List<DiaryEntry>) {
    if (entries.isEmpty()) return
    var dismissed by rememberSaveable(entries) { mutableStateOf(false) }
    if (dismissed) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .card()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(imageVector = Icons.Filled.History, contentDescription = null, tint = AccentGreen)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.yesterday_summary_title),
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.yesterday_summary_dismiss),
                tint = TextSecondary,
                modifier = Modifier.clickable { dismissed = true }
            )
        }
        entries.forEach { entry -> YesterdaySummaryRow(entry) }
    }
}

@Composable
private fun YesterdaySummaryRow(entry: DiaryEntry) {
    val parts = buildList {
        entry.bloodSugar?.let {
            add(stringResource(R.string.sugar_value_format, String.format(Locale.US, "%.1f", it)))
        }
        if (entry.mealLabel == null) {
            entry.breadUnits?.let {
                add(stringResource(R.string.bread_units_value_format, String.format(Locale.US, "%.1f", it)))
            }
        }
        entry.shortInsulinDose?.let {
            add(
                stringResource(R.string.short_insulin_short_label) + " " +
                    stringResource(R.string.dose_value_format, it.toString())
            )
        }
        entry.longInsulinDose?.let {
            add(
                stringResource(R.string.long_insulin_short_label) + " " +
                    stringResource(R.string.dose_value_format, it.toString())
            )
        }
    }

    Column {
        Text(
            text = entry.createdAt.format(DateTimeFormatter.ofPattern("HH:mm")),
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        if (parts.isNotEmpty()) {
            Text(
                text = parts.joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
        }
        entry.mealLabel?.let {
            Text(text = it, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        if (entry.notes.isNotBlank()) {
            Text(text = entry.notes, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}
