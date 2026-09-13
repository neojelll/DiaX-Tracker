package com.neojelll.diaxtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.data.GlucoseRange
import com.neojelll.diaxtracker.ui.components.DateSheet
import com.neojelll.diaxtracker.ui.components.NotificationBellButton
import com.neojelll.diaxtracker.ui.components.PillChip
import com.neojelll.diaxtracker.ui.components.SheetBackdrop
import com.neojelll.diaxtracker.ui.theme.BorderDashed
import com.neojelll.diaxtracker.ui.theme.CardDivider
import com.neojelll.diaxtracker.ui.theme.FieldTile
import com.neojelll.diaxtracker.ui.theme.GlucoIcons
import com.neojelll.diaxtracker.ui.theme.Ink
import com.neojelll.diaxtracker.ui.theme.Kicker
import com.neojelll.diaxtracker.ui.theme.PhotoPlaceholder
import com.neojelll.diaxtracker.ui.theme.PlaceholderText
import com.neojelll.diaxtracker.ui.theme.ScreenTitle
import com.neojelll.diaxtracker.ui.theme.TextLabel
import com.neojelll.diaxtracker.ui.theme.TextSecondary
import com.neojelll.diaxtracker.ui.theme.TextTertiary
import com.neojelll.diaxtracker.ui.theme.card
import com.neojelll.diaxtracker.ui.theme.glucoseColor
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private sealed interface DateFilter {
    data object All : DateFilter
    data object Today : DateFilter
    data object Week : DateFilter
    data object Month : DateFilter
    data class Range(val from: LocalDate, val to: LocalDate) : DateFilter
}

private sealed interface HistoryRow {
    data class DayHeader(val date: LocalDate, val count: Int) : HistoryRow
    data class Entry(val entry: DiaryEntry) : HistoryRow
}

@Composable
fun HistoryScreen(
    viewModel: DiaryViewModel,
    onEditEntry: (Long) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenPresetDetail: (Long) -> Unit,
    onOpenPhoto: (path: String, caption: String) -> Unit
) {
    val entries by viewModel.entries.collectAsState()
    val glucoseRange by viewModel.glucoseRange.collectAsState()
    val mealPresets by viewModel.mealPresets.collectAsState()
    var query by remember { mutableStateOf("") }
    var dateFilter by remember { mutableStateOf<DateFilter>(DateFilter.All) }
    var sortDesc by remember { mutableStateOf(true) }
    var showRangeFromPicker by remember { mutableStateOf(false) }
    var pendingRangeFrom by remember { mutableStateOf<LocalDate?>(null) }
    var showRangeToPicker by remember { mutableStateOf(false) }

    val today = LocalDate.now()
    val filteredEntries = remember(entries, query, dateFilter, sortDesc) {
        var list = entries.toList()
        list = when (val filter = dateFilter) {
            DateFilter.All -> list
            DateFilter.Today -> list.filter { it.createdAt.toLocalDate() == today }
            DateFilter.Week -> list.filter { it.createdAt.toLocalDate() >= today.minusDays(6) }
            DateFilter.Month -> list.filter { it.createdAt.toLocalDate() >= today.minusDays(29) }
            is DateFilter.Range -> list.filter { it.createdAt.toLocalDate() in filter.from..filter.to }
        }
        if (query.isNotBlank()) {
            list = list.filter { entry ->
                entry.notes.contains(query, ignoreCase = true) ||
                    entry.mealLabel?.contains(query, ignoreCase = true) == true ||
                    entry.bloodSugar?.toString()?.contains(query) == true
            }
        }
        list = list.sortedBy { it.createdAt }
        if (sortDesc) list.reversed() else list
    }

    val rows = remember(filteredEntries) {
        buildList {
            var currentDate: LocalDate? = null
            var dayEntries = 0
            val grouped = filteredEntries.groupBy { it.createdAt.toLocalDate() }
            filteredEntries.forEachIndexed { index, entry ->
                val entryDate = entry.createdAt.toLocalDate()
                if (entryDate != currentDate) {
                    currentDate = entryDate
                    dayEntries = grouped[entryDate]?.size ?: 0
                    add(HistoryRow.DayHeader(entryDate, dayEntries))
                }
                add(HistoryRow.Entry(entry))
            }
        }
    }

    if (showRangeFromPicker) {
        SheetBackdrop(onDismiss = { showRangeFromPicker = false })
        DateSheet(
            initialDate = today,
            onConfirm = {
                pendingRangeFrom = it
                showRangeFromPicker = false
                showRangeToPicker = true
            },
            onDismiss = { showRangeFromPicker = false }
        )
    }
    if (showRangeToPicker) {
        SheetBackdrop(onDismiss = { showRangeToPicker = false })
        DateSheet(
            initialDate = today,
            onConfirm = { to ->
                val from = pendingRangeFrom ?: to
                dateFilter = DateFilter.Range(minOf(from, to), maxOf(from, to))
                showRangeToPicker = false
            },
            onDismiss = { showRangeToPicker = false }
        )
    }

    Column(Modifier.fillMaxSize().padding(top = 18.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(stringResource(R.string.history_title), style = ScreenTitle, color = Ink)
                Text(historySummary(filteredEntries.size, filteredEntries), fontSize = 13.sp, color = TextLabel, modifier = Modifier.padding(top = 5.dp))
            }
            NotificationBellButton(onClick = onOpenNotifications)
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(GlucoIcons.Search, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(16.dp))
            Box(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                if (query.isEmpty()) {
                    Text(stringResource(R.string.history_search_placeholder), fontSize = 13.sp, color = PlaceholderText)
                }
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = Ink),
                    cursorBrush = SolidColor(Ink),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotEmpty()) {
                Box(
                    Modifier.size(22.dp).clip(androidx.compose.foundation.shape.CircleShape).background(FieldTile).clickable { query = "" },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(GlucoIcons.Close, contentDescription = stringResource(R.string.clear_search), tint = Ink, modifier = Modifier.size(11.dp))
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 14.dp).padding(top = 10.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            PillChip(stringResource(R.string.date_filter_all), dateFilter == DateFilter.All, { dateFilter = DateFilter.All })
            PillChip(stringResource(R.string.today_label), dateFilter == DateFilter.Today, { dateFilter = DateFilter.Today })
            PillChip(stringResource(R.string.date_filter_week), dateFilter == DateFilter.Week, { dateFilter = DateFilter.Week })
            PillChip(stringResource(R.string.date_filter_month), dateFilter == DateFilter.Month, { dateFilter = DateFilter.Month })
            PillChip(
                stringResource(R.string.date_filter_period),
                dateFilter is DateFilter.Range,
                { pendingRangeFrom = null; showRangeFromPicker = true }
            )
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                if (query.isBlank()) stringResource(R.string.history_shown_count, filteredEntries.size)
                else stringResource(R.string.history_match_count, filteredEntries.size),
                fontSize = 11.5.sp,
                color = TextTertiary
            )
            Row(
                Modifier.clickable { sortDesc = !sortDesc },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(if (sortDesc) R.string.sort_newest else R.string.sort_oldest),
                    fontSize = 11.5.sp,
                    color = Ink,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Icon(GlucoIcons.SortArrows, contentDescription = null, tint = Ink, modifier = Modifier.size(13.dp))
            }
        }

        if (entries.isEmpty()) {
            Column(Modifier.fillMaxWidth().padding(top = 60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.no_entries_title), fontSize = 15.sp, color = Ink)
                Text(stringResource(R.string.no_entries_subtitle), fontSize = 13.sp, color = TextLabel, modifier = Modifier.padding(top = 8.dp))
            }
        } else if (rows.isEmpty()) {
            Text(
                stringResource(R.string.no_search_results),
                fontSize = 13.sp,
                color = TextLabel,
                modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                items(rows, key = { row -> when (row) { is HistoryRow.DayHeader -> "day-${row.date}"; is HistoryRow.Entry -> row.entry.id } }) { row ->
                    when (row) {
                        is HistoryRow.DayHeader -> DayHeaderRow(row.date, row.count)
                        is HistoryRow.Entry -> {
                            val entry = row.entry
                            if (entry.mealLabel == null && entry.shortInsulinDose == null && entry.longInsulinDose == null &&
                                entry.notes.isBlank() && entry.photoPath == null
                            ) {
                                CompactRecordRow(entry = entry, glucoseRange = glucoseRange, onClick = { onEditEntry(entry.id) })
                            } else {
                                FullRecordCard(
                                    entry = entry,
                                    glucoseRange = glucoseRange,
                                    mealPresets = mealPresets,
                                    onEdit = { onEditEntry(entry.id) },
                                    onOpenPreset = onOpenPresetDetail,
                                    onOpenPhoto = onOpenPhoto
                                )
                            }
                        }
                    }
                }
                item {
                    Text(
                        stringResource(R.string.no_earlier_records),
                        fontSize = 11.5.sp,
                        color = TextTertiary,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun historySummary(count: Int, entries: List<DiaryEntry>): String {
    val days = entries.map { it.createdAt.toLocalDate() }.distinct().size.coerceAtLeast(if (count > 0) 1 else 0)
    val entriesLabel = androidx.compose.ui.res.pluralStringResource(R.plurals.entries_plural, count, count)
    val daysLabel = androidx.compose.ui.res.pluralStringResource(R.plurals.days_plural, days, days)
    return stringResource(R.string.history_summary_format, entriesLabel, daysLabel)
}

@Composable
private fun mealTagFor(entry: DiaryEntry): String {
    val hour = entry.createdAt.hour
    return when {
        entry.mealLabel == null && entry.shortInsulinDose == null && entry.longInsulinDose == null &&
            entry.notes.isBlank() && entry.photoPath == null -> stringResource(R.string.tag_measurement)
        hour in 5..10 -> stringResource(R.string.tag_breakfast)
        hour in 11..15 -> stringResource(R.string.tag_lunch)
        hour in 16..21 -> stringResource(R.string.tag_dinner)
        else -> stringResource(R.string.tag_snack)
    }
}

@Composable
private fun DayHeaderRow(date: LocalDate, count: Int) {
    val locale = LocalConfiguration.current.locales[0]
    val today = LocalDate.now()
    val label = when (date) {
        today -> stringResource(R.string.today_label)
        today.minusDays(1) -> stringResource(R.string.yesterday_label)
        else -> date.format(DateTimeFormatter.ofPattern("d MMMM", locale))
    }
    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label.uppercase(locale), style = Kicker, color = TextLabel)
        Box(Modifier.weight(1f).padding(horizontal = 10.dp).height(1.dp).background(BorderDashed))
        Text(stringResource(R.string.day_group_count_format, count), fontSize = 11.sp, color = TextTertiary)
    }
}

@Composable
private fun CompactRecordRow(entry: DiaryEntry, glucoseRange: GlucoseRange, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .card(com.neojelll.diaxtracker.ui.theme.TileShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(entry.createdAt.format(DateTimeFormatter.ofPattern("HH:mm")), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Ink)
            Text(mealTagFor(entry), fontSize = 11.sp, color = TextTertiary, modifier = Modifier.padding(start = 8.dp))
        }
        entry.bloodSugar?.let {
            Row(verticalAlignment = Alignment.Bottom) {
                Icon(GlucoIcons.Droplet, contentDescription = null, tint = TextLabel, modifier = Modifier.size(12.dp))
                Text(
                    String.format(Locale.US, "%.1f", it),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = glucoseColor(it, glucoseRange.low, glucoseRange.high),
                    modifier = Modifier.padding(start = 4.dp)
                )
                Text(stringResource(R.string.mmol_unit), fontSize = 10.sp, color = TextLabel, modifier = Modifier.padding(start = 4.dp))
            }
        }
    }
}

@Composable
private fun FullRecordCard(
    entry: DiaryEntry,
    glucoseRange: GlucoseRange,
    mealPresets: List<com.neojelll.diaxtracker.data.MealPresetWithProducts>,
    onEdit: () -> Unit,
    onOpenPreset: (Long) -> Unit,
    onOpenPhoto: (path: String, caption: String) -> Unit
) {
    Column(Modifier.fillMaxWidth().card(com.neojelll.diaxtracker.ui.theme.CardShapeMedium).padding(horizontal = 16.dp, vertical = 15.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(entry.createdAt.format(DateTimeFormatter.ofPattern("HH:mm")), fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Ink)
                Box(Modifier.padding(start = 9.dp).clip(RoundedCornerShape(50)).background(FieldTile).padding(horizontal = 9.dp, vertical = 3.dp)) {
                    Text(mealTagFor(entry), fontSize = 11.sp, color = TextLabel)
                }
            }
            Box(
                Modifier.size(30.dp).clip(androidx.compose.foundation.shape.CircleShape).background(FieldTile).clickable(onClick = onEdit),
                contentAlignment = Alignment.Center
            ) {
                Icon(GlucoIcons.Pencil, contentDescription = stringResource(R.string.edit_action), tint = Ink, modifier = Modifier.size(14.dp))
            }
        }

        Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 13.dp)) {
            Column(Modifier.weight(1f, fill = false)) {
                Text(stringResource(R.string.sugar_stat_label), fontSize = 10.5.sp, color = TextLabel)
                Row(Modifier.padding(top = 3.dp), verticalAlignment = Alignment.Bottom) {
                    Text(
                        entry.bloodSugar?.let { String.format(Locale.US, "%.1f", it) } ?: "—",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = entry.bloodSugar?.let { glucoseColor(it, glucoseRange.low, glucoseRange.high) } ?: Ink
                    )
                    Text(stringResource(R.string.mmol_unit), fontSize = 9.5.sp, color = TextLabel, modifier = Modifier.padding(start = 3.dp))
                }
            }
            Row(
                Modifier.weight(1f).borderStart(1.dp, CardDivider).padding(start = 14.dp)
            ) {
                Column {
                    Text(stringResource(R.string.insulin_column_kicker), fontSize = 10.5.sp, color = TextLabel)
                    Row(Modifier.padding(top = 3.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        DoseValue(entry.shortInsulinDose, stringResource(R.string.short_insulin_stat_label))
                        DoseValue(entry.longInsulinDose, stringResource(R.string.long_insulin_stat_label))
                    }
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(CardDivider).padding(bottom = 13.dp))

        Row(Modifier.fillMaxWidth().padding(top = 13.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            if (entry.mealLabel != null) {
                val matchedPresetId = remember(entry.mealLabel, mealPresets) {
                    mealPresets.find { entry.mealLabel!!.startsWith(it.preset.name + " ·") }?.preset?.id
                }
                PresetMiniTile(
                    entry = entry,
                    onClick = matchedPresetId?.let { id -> { onOpenPreset(id) } },
                    modifier = Modifier.weight(1f)
                )
            } else {
                Box(Modifier.weight(1f))
            }
            if (entry.photoPath != null) {
                Box(
                    Modifier
                        .width(88.dp)
                        .height(88.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PhotoPlaceholder)
                        .clickable {
                            onOpenPhoto(entry.photoPath!!, entry.createdAt.format(DateTimeFormatter.ofPattern("d MMMM, HH:mm")))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = File(entry.photoPath!!),
                        contentDescription = stringResource(R.string.entry_photo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                    )
                }
            } else {
                Box(
                    Modifier.width(88.dp).height(88.dp).clip(RoundedCornerShape(16.dp))
                        .background(Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.no_photo_label), fontSize = 10.5.sp, color = PlaceholderText, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }

        if (entry.notes.isNotBlank()) {
            Box(Modifier.fillMaxWidth().padding(top = 13.dp).height(1.dp).background(CardDivider))
            Text(entry.notes, fontSize = 12.5.sp, color = TextSecondary, lineHeight = 18.sp, modifier = Modifier.padding(top = 12.dp))
        }
    }
}

@Composable
private fun DoseValue(value: Float?, label: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(value?.let { formatAmount(it) } ?: "—", fontSize = 17.sp, fontWeight = FontWeight.Medium, color = Ink)
        Text(label, fontSize = 9.5.sp, color = TextLabel, modifier = Modifier.padding(start = 5.dp))
    }
}

@Composable
private fun PresetMiniTile(entry: DiaryEntry, onClick: (() -> Unit)?, modifier: Modifier = Modifier) {
    Column(
        modifier
            .height(88.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(FieldTile)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(entry.mealLabel.orEmpty(), fontSize = 13.5.sp, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
            entry.breadUnits?.let {
                Box(Modifier.clip(RoundedCornerShape(50)).background(Color.White).padding(horizontal = 8.dp, vertical = 3.dp)) {
                    Text(stringResource(R.string.bread_units_value_format, formatAmount(it)), fontSize = 10.5.sp, color = Ink)
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.composition_label), fontSize = 10.5.sp, color = TextTertiary)
            Icon(GlucoIcons.ChevronRight, contentDescription = null, tint = TextTertiary, modifier = Modifier.padding(start = 4.dp).size(11.dp))
        }
    }
}

private fun Modifier.borderStart(width: Dp, color: Color): Modifier = drawBehind {
    drawLine(color, Offset(0f, 0f), Offset(0f, size.height), strokeWidth = width.toPx())
}
