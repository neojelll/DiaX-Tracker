package com.neojelll.diaxtracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.ui.components.CollapsibleTopBar
import com.neojelll.diaxtracker.ui.components.LanguageMenu
import com.neojelll.diaxtracker.ui.components.rememberCollapsibleTopBarState
import com.neojelll.diaxtracker.ui.theme.CardBorder
import com.neojelll.diaxtracker.ui.theme.SproutGreen
import com.neojelll.diaxtracker.ui.theme.TextPrimary
import com.neojelll.diaxtracker.ui.theme.TextSecondary
import com.neojelll.diaxtracker.ui.theme.card
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel
import java.io.File
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: DiaryViewModel,
    onEntryClick: (Long) -> Unit
) {
    val entries by viewModel.entries.collectAsState()
    val topBarState = rememberCollapsibleTopBarState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(topBarState.nestedScrollConnection)
    ) {
        CollapsibleTopBar(
            state = topBarState,
            title = { Text(stringResource(R.string.history_title), color = TextPrimary) },
            actions = { LanguageMenu() }
        )

        Box(modifier = Modifier.weight(1f)) {
            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.no_entries_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.no_entries_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(entries, key = { it.id }) { entry ->
                        val stats = entryStats(entry)
                        if (stats.size <= 1 && entry.notes.isBlank() && entry.photoPath == null) {
                            CompactEntryRow(
                                entry = entry,
                                stat = stats.firstOrNull(),
                                onClick = { onEntryClick(entry.id) }
                            )
                        } else {
                            DiaryEntryCard(
                                entry = entry,
                                stats = stats,
                                onClick = { onEntryClick(entry.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class EntryStat(
    val label: String,
    val value: String,
    val color: Color = TextPrimary
)

@Composable
private fun entryStats(entry: DiaryEntry): List<EntryStat> = buildList {
    entry.bloodSugar?.let {
        add(
            EntryStat(
                label = stringResource(R.string.sugar_label),
                value = stringResource(R.string.sugar_value_format, String.format(Locale.US, "%.1f", it)),
                color = sugarColor(it)
            )
        )
    }
    entry.breadUnits?.let {
        add(
            EntryStat(
                label = stringResource(R.string.bread_units_short_label),
                value = stringResource(R.string.bread_units_value_format, String.format(Locale.US, "%.1f", it))
            )
        )
    }
    entry.shortInsulinDose?.let {
        add(
            EntryStat(
                label = stringResource(R.string.short_insulin_short_label),
                value = stringResource(R.string.dose_value_format, it.toString())
            )
        )
    }
    entry.longInsulinDose?.let {
        add(
            EntryStat(
                label = stringResource(R.string.long_insulin_short_label),
                value = stringResource(R.string.dose_value_format, it.toString())
            )
        )
    }
}

@Composable
private fun CompactEntryRow(entry: DiaryEntry, stat: EntryStat?, onClick: () -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", locale)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .card()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = entry.createdAt.format(formatter),
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (stat != null) {
            Text(
                text = stat.value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = stat.color
            )
        }
    }
}

@Composable
private fun DiaryEntryCard(entry: DiaryEntry, stats: List<EntryStat>, onClick: () -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", locale)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .card()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.createdAt.format(formatter),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                entry.photoPath?.let { path ->
                    AsyncImage(
                        model = File(path),
                        contentDescription = stringResource(R.string.entry_photo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                }
            }

            HorizontalDivider(color = CardBorder)

            stats.chunked(2).forEach { rowStats ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowStats.forEach { stat ->
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stat.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = stat.value,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = stat.color
                            )
                        }
                    }
                }
            }

            if (entry.notes.isNotBlank()) {
                Column {
                    Text(
                        text = stringResource(R.string.comment_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = entry.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun sugarColor(value: Float) = when {
    value < 3.9f -> MaterialTheme.colorScheme.error
    value > 10.0f -> MaterialTheme.colorScheme.error
    value > 7.8f -> MaterialTheme.colorScheme.tertiary
    else -> SproutGreen
}
