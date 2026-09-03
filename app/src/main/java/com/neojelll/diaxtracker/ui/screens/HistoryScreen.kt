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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.ui.components.LanguageMenu
import com.neojelll.diaxtracker.ui.theme.OnGlass
import com.neojelll.diaxtracker.ui.theme.OnGlassMuted
import com.neojelll.diaxtracker.ui.theme.SproutGreen
import com.neojelll.diaxtracker.ui.theme.glassPanel
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: DiaryViewModel,
    onEntryClick: (Long) -> Unit
) {
    val entries by viewModel.entries.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title), color = Color.White) },
                actions = { LanguageMenu() },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.no_entries_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.no_entries_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
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
                        DiaryEntryCard(
                            entry = entry,
                            onClick = { onEntryClick(entry.id) }
                        )
                    }
                }
            }
        }
    }
}

private data class EntryStat(
    val label: String,
    val value: String,
    val color: Color = OnGlass
)

@Composable
private fun DiaryEntryCard(entry: DiaryEntry, onClick: () -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", locale)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassPanel(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = entry.createdAt.format(formatter),
                style = MaterialTheme.typography.labelMedium,
                color = OnGlassMuted
            )

            HorizontalDivider(color = OnGlass.copy(alpha = 0.12f))

            val stats = buildList {
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
                                color = OnGlassMuted,
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
                        color = OnGlassMuted
                    )
                    Text(
                        text = entry.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnGlass
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
