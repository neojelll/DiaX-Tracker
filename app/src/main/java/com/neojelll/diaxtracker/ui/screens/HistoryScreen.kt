package com.neojelll.diaxtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.data.DiaryEntryProduct
import com.neojelll.diaxtracker.data.GlucoseRange
import com.neojelll.diaxtracker.ui.components.CollapsibleTopBar
import com.neojelll.diaxtracker.ui.components.rememberCollapsibleTopBarState
import com.neojelll.diaxtracker.ui.theme.CardBorder
import com.neojelll.diaxtracker.ui.theme.FieldBackground
import com.neojelll.diaxtracker.ui.theme.TextPrimary
import com.neojelll.diaxtracker.ui.theme.TextSecondary
import com.neojelll.diaxtracker.ui.theme.card
import com.neojelll.diaxtracker.ui.theme.fieldBox
import com.neojelll.diaxtracker.ui.theme.glucoseColor
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs

private sealed interface HistoryRow {
    data class DayHeader(val date: LocalDate) : HistoryRow
    data class Entry(val entry: DiaryEntry) : HistoryRow
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: DiaryViewModel,
    onEntryClick: (Long) -> Unit
) {
    val entries by viewModel.entries.collectAsState()
    val glucoseRange by viewModel.glucoseRange.collectAsState()
    val topBarState = rememberCollapsibleTopBarState()
    val listState = rememberLazyListState()
    val canScroll = listState.canScrollForward || listState.canScrollBackward || !topBarState.isFullyExpanded
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val filteredEntries = remember(entries, searchQuery) {
        if (searchQuery.isBlank()) {
            entries
        } else {
            entries.filter { entry ->
                entry.notes.contains(searchQuery, ignoreCase = true) ||
                    entry.mealLabel?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    val rows = remember(filteredEntries) {
        buildList {
            filteredEntries.forEachIndexed { index, entry ->
                val entryDate = entry.createdAt.toLocalDate()
                val previousDate = filteredEntries.getOrNull(index - 1)?.createdAt?.toLocalDate()
                if (entryDate != previousDate) add(HistoryRow.DayHeader(entryDate))
                add(HistoryRow.Entry(entry))
            }
        }
    }

    val dayRowIndex = remember(rows) {
        rows.withIndex().mapNotNull { (index, row) ->
            (row as? HistoryRow.DayHeader)?.let { it.date to index }
        }.toMap()
    }

    if (showDatePicker) {
        DatePickerDialog(
            initialDate = LocalDate.now(),
            onConfirm = { pickedDate ->
                showDatePicker = false
                val nearestDate = dayRowIndex.keys.minByOrNull { abs(ChronoUnit.DAYS.between(it, pickedDate)) }
                nearestDate?.let { date ->
                    coroutineScope.launch {
                        listState.animateScrollToItem(dayRowIndex.getValue(date))
                    }
                }
            },
            onDismiss = { showDatePicker = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (canScroll) Modifier.nestedScroll(topBarState.nestedScrollConnection) else Modifier
            )
    ) {
        CollapsibleTopBar(
            state = topBarState,
            title = { Text(stringResource(R.string.history_title), color = TextPrimary) }
        )

        HistorySearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onDatePick = { showDatePicker = true }
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
            } else if (rows.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_search_results),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        rows,
                        key = { row ->
                            when (row) {
                                is HistoryRow.DayHeader -> "day-${row.date}"
                                is HistoryRow.Entry -> row.entry.id
                            }
                        }
                    ) { row ->
                        when (row) {
                            is HistoryRow.DayHeader -> DayDivider(date = row.date)
                            is HistoryRow.Entry -> {
                                val entry = row.entry
                                val stats = entryStats(entry, glucoseRange)
                                if (entry.mealLabel == null && stats.size <= 1 && entry.notes.isBlank() && entry.photoPath == null) {
                                    CompactEntryRow(
                                        entry = entry,
                                        stat = stats.firstOrNull(),
                                        onClick = { onEntryClick(entry.id) }
                                    )
                                } else {
                                    DiaryEntryCard(
                                        entry = entry,
                                        stats = stats,
                                        onClick = { onEntryClick(entry.id) },
                                        fetchMealProducts = viewModel::getEntryProducts
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorySearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onDatePick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fieldBox()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = stringResource(R.string.history_search_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                cursorBrush = SolidColor(TextPrimary),
                singleLine = true
            )
        }
        if (query.isNotEmpty()) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.clear_search),
                tint = TextSecondary,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onQueryChange("") }
            )
        }
        Icon(
            imageVector = Icons.Filled.CalendarMonth,
            contentDescription = stringResource(R.string.search_by_date),
            tint = TextSecondary,
            modifier = Modifier
                .size(18.dp)
                .clickable(onClick = onDatePick)
        )
    }
}

private data class EntryStat(
    val label: String,
    val value: String,
    val color: Color = TextPrimary
)

@Composable
private fun entryStats(entry: DiaryEntry, glucoseRange: GlucoseRange): List<EntryStat> = buildList {
    entry.bloodSugar?.let {
        add(
            EntryStat(
                label = stringResource(R.string.sugar_label),
                value = stringResource(R.string.sugar_value_format, String.format(Locale.US, "%.1f", it)),
                color = glucoseColor(it, glucoseRange.low, glucoseRange.high)
            )
        )
    }
    if (entry.mealLabel == null) {
        entry.breadUnits?.let {
            add(
                EntryStat(
                    label = stringResource(R.string.bread_units_short_label),
                    value = stringResource(R.string.bread_units_value_format, String.format(Locale.US, "%.1f", it))
                )
            )
        }
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
private fun DayDivider(date: LocalDate) {
    val locale = LocalConfiguration.current.locales[0]
    val today = LocalDate.now()
    val label = when (date) {
        today -> stringResource(R.string.today_label)
        today.minusDays(1) -> stringResource(R.string.yesterday_label)
        else -> date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", locale))
    }
    Text(
        text = label.uppercase(locale),
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
        color = TextSecondary,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    )
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
private fun DiaryEntryCard(
    entry: DiaryEntry,
    stats: List<EntryStat>,
    onClick: () -> Unit,
    fetchMealProducts: suspend (Long) -> List<DiaryEntryProduct>
) {
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

            entry.mealLabel?.let { label ->
                MealBreakdownBox(
                    entryId = entry.id,
                    mealLabel = label,
                    totalBreadUnits = entry.breadUnits ?: 0f,
                    fetchProducts = fetchMealProducts
                )
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
private fun MealBreakdownBox(
    entryId: Long,
    mealLabel: String,
    totalBreadUnits: Float,
    fetchProducts: suspend (Long) -> List<DiaryEntryProduct>
) {
    var expanded by remember(entryId) { mutableStateOf(false) }
    var products by remember(entryId) { mutableStateOf<List<DiaryEntryProduct>?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(FieldBackground)
            .clickable {
                expanded = !expanded
                if (expanded && products == null) {
                    coroutineScope.launch {
                        products = fetchProducts(entryId)
                    }
                }
            }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = mealLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.bread_units_value_format, formatAmount(totalBreadUnits)),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }

        if (expanded) {
            products?.sortedBy { it.sortOrder }?.forEach { product ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = stringResource(R.string.bread_units_value_format, formatAmount(product.breadUnits)),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
