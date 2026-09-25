package com.neojelll.diaxtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.data.DiaryEntryProduct
import com.neojelll.diaxtracker.data.GlucoseRange
import com.neojelll.diaxtracker.data.SugarSource
import com.neojelll.diaxtracker.ui.components.CircleButton
import com.neojelll.diaxtracker.ui.components.FilterChip
import com.neojelll.diaxtracker.ui.components.FoodPicker
import com.neojelll.diaxtracker.ui.components.GlukoCard
import com.neojelll.diaxtracker.ui.components.GlukoDivider
import com.neojelll.diaxtracker.ui.components.GlukoField
import com.neojelll.diaxtracker.ui.components.GlukoSheet
import com.neojelll.diaxtracker.ui.components.Kicker
import com.neojelll.diaxtracker.ui.components.LucideIcon
import com.neojelll.diaxtracker.ui.components.LucidePaths
import com.neojelll.diaxtracker.ui.aftermeal.AfterMeal
import com.neojelll.diaxtracker.ui.aftermeal.buildAfterMeal
import com.neojelll.diaxtracker.ui.components.AfterMealPill
import com.neojelll.diaxtracker.ui.components.OverlayController
import com.neojelll.diaxtracker.ui.components.rememberAppToasts
import com.neojelll.diaxtracker.ui.components.PhotoButton
import com.neojelll.diaxtracker.ui.components.PrimaryButton
import com.neojelll.diaxtracker.ui.components.SecondaryButton
import com.neojelll.diaxtracker.ui.components.SheetHeader
import com.neojelll.diaxtracker.ui.components.XeBadge
import com.neojelll.diaxtracker.ui.components.dashedBorder
import com.neojelll.diaxtracker.ui.components.numeric
import com.neojelll.diaxtracker.ui.components.plainClickable
import com.neojelll.diaxtracker.ui.components.shadowSoft
import com.neojelll.diaxtracker.ui.components.toPresetOption
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoRadius
import com.neojelll.diaxtracker.ui.theme.GlukoSpacing
import com.neojelll.diaxtracker.ui.theme.GlukoType
import com.neojelll.diaxtracker.ui.theme.glucoseColor
import com.neojelll.diaxtracker.ui.theme.tabular
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private sealed interface HistoryRow {
    data class DayHeader(val date: LocalDate) : HistoryRow
    data class Entry(val entry: DiaryEntry) : HistoryRow
}

private sealed interface DateFilter {
    data object All : DateFilter
    data object Yesterday : DateFilter
    data object Week : DateFilter
    data object Month : DateFilter
    data class Range(val from: LocalDate, val to: LocalDate) : DateFilter
}

private fun DiaryEntry.matches(filter: DateFilter): Boolean {
    val date = createdAt.toLocalDate()
    val today = LocalDate.now()
    return when (filter) {
        DateFilter.All -> true
        DateFilter.Yesterday -> date == today.minusDays(1)
        DateFilter.Week -> !date.isBefore(today.minusDays(6))
        DateFilter.Month -> !date.isBefore(today.minusDays(29))
        is DateFilter.Range -> !date.isBefore(filter.from) && !date.isAfter(filter.to)
    }
}

@Composable
fun HistoryScreen(viewModel: DiaryViewModel, overlays: OverlayController) {
    val entries by viewModel.entries.collectAsState()
    val entryProducts by viewModel.entryProducts.collectAsState()
    val glucoseRange by viewModel.glucoseRange.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var dateFilter by remember { mutableStateOf<DateFilter>(DateFilter.All) }
    var sortDescending by remember { mutableStateOf(true) }

    val productNamesByEntry = remember(entryProducts) {
        entryProducts.groupBy(DiaryEntryProduct::diaryEntryId) { it.name }
    }

    // A pill appears an hour after a meal, so re-evaluate as time passes, not only when entries change.
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            now = LocalDateTime.now()
        }
    }
    val afterMeal = remember(entries, now) { buildAfterMeal(entries, now) }

    val filtered = remember(entries, afterMeal, productNamesByEntry, searchQuery, dateFilter, sortDescending) {
        val byDateAndText = entries.filter { entry ->
            entry.id !in afterMeal.hidden && entry.matches(dateFilter) && (
                searchQuery.isBlank() ||
                    entry.notes.contains(searchQuery, ignoreCase = true) ||
                    entry.mealLabel?.contains(searchQuery, ignoreCase = true) == true ||
                    entry.bloodSugar?.toString()?.contains(searchQuery) == true ||
                    productNamesByEntry[entry.id]?.any { it.contains(searchQuery, ignoreCase = true) } == true
                )
        }
        if (sortDescending) byDateAndText else byDateAndText.reversed()
    }

    val rows = remember(filtered) {
        buildList {
            filtered.forEachIndexed { index, entry ->
                val entryDate = entry.createdAt.toLocalDate()
                val previousDate = filtered.getOrNull(index - 1)?.createdAt?.toLocalDate()
                if (entryDate != previousDate) add(HistoryRow.DayHeader(entryDate))
                add(HistoryRow.Entry(entry))
            }
        }
    }
    val distinctDays = remember(filtered) { filtered.map { it.createdAt.toLocalDate() }.distinct().size }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = GlukoSpacing.screenHorizontal, end = GlukoSpacing.screenHorizontal,
            top = 18.dp, bottom = GlukoSpacing.bottomInset
        )
    ) {
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.history_title), style = GlukoType.ScreenTitle)
                    Spacer(Modifier.height(5.dp))
                    Text(historySummary(filtered.size, distinctDays), style = GlukoType.Label)
                }
                NotificationButton(onClick = overlays::openNotifications)
            }
            Spacer(Modifier.height(14.dp))
        }

        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .shadowSoft(GlukoRadius.tile)
                    .clip(RoundedCornerShape(GlukoRadius.tile))
                    .background(GlukoColors.Surface)
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LucideIcon(LucidePaths.Search, 16.dp, GlukoColors.TextTertiary, strokeWidth = 1.9f)
                Spacer(Modifier.width(9.dp))
                GlukoField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = stringResource(R.string.history_search_placeholder),
                    modifier = Modifier.weight(1f),
                    textStyle = GlukoType.Body.copy(fontSize = 13.sp),
                    background = Color.Transparent,
                    padding = PaddingValues(start = 4.dp),
                    cursorBrush = SolidColor(GlukoColors.CursorSoft)
                )
                if (searchQuery.isNotEmpty()) {
                    CircleButton(22.dp, GlukoColors.Tile, { searchQuery = "" }) {
                        LucideIcon(LucidePaths.Close, 11.dp, strokeWidth = 2.4f)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        item {
            val filters = listOf(
                DateFilter.All to stringResource(R.string.history_filter_all),
                DateFilter.Yesterday to stringResource(R.string.history_filter_yesterday),
                DateFilter.Week to stringResource(R.string.history_filter_week),
                DateFilter.Month to stringResource(R.string.history_filter_month)
            )
            val rangeLabel = stringResource(R.string.history_filter_range)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                items(filters) { (filter, label) ->
                    FilterChip(label, filter == dateFilter) { dateFilter = filter }
                }
                item {
                    FilterChip(rangeLabel, dateFilter is DateFilter.Range) {
                        val today = LocalDate.now()
                        overlays.openDatePicker(today) { from ->
                            overlays.openDatePicker(from) { to ->
                                dateFilter = DateFilter.Range(minOf(from, to), maxOf(from, to))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val countLabel = if (searchQuery.isEmpty()) {
                    stringResource(R.string.history_shown_format, filtered.size)
                } else {
                    stringResource(R.string.history_matches_format, filtered.size)
                }
                Text(countLabel, style = GlukoType.Hint, modifier = Modifier.weight(1f))
                Row(Modifier.plainClickable { sortDescending = !sortDescending }, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(if (sortDescending) R.string.history_sort_desc else R.string.history_sort_asc),
                        style = GlukoType.Hint.copy(color = GlukoColors.Ink)
                    )
                    Spacer(Modifier.width(6.dp))
                    LucideIcon(LucidePaths.Sort, 13.dp, strokeWidth = 1.9f)
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        if (rows.isEmpty()) {
            item {
                Text(
                    stringResource(if (entries.isEmpty()) R.string.no_entries_title else R.string.no_search_results),
                    style = GlukoType.Hint,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        items(rows, key = { row ->
            when (row) {
                is HistoryRow.DayHeader -> "day-${row.date}"
                is HistoryRow.Entry -> row.entry.id
            }
        }) { row ->
            when (row) {
                is HistoryRow.DayHeader -> {
                    DayHeaderRow(row.date, entries.count { it.id !in afterMeal.hidden && it.createdAt.toLocalDate() == row.date && it.matches(dateFilter) })
                    Spacer(Modifier.height(10.dp))
                }
                is HistoryRow.Entry -> {
                    val entry = row.entry
                    val sugarColor = entry.bloodSugar?.let { glucoseColor(it, glucoseRange.low, glucoseRange.high) } ?: GlukoColors.Ink
                    val statCount = listOfNotNull(entry.bloodSugar, entry.shortInsulinDose, entry.longInsulinDose).size
                    val pill = afterMeal.byMeal[entry.id]
                    if (entry.mealLabel == null && statCount <= 1 && entry.notes.isBlank() && entry.photoPath == null && pill == null) {
                        CompactRecordRow(entry, sugarColor) { overlays.openRecordEdit(entry.id) }
                    } else {
                        val caption = photoCaption(entry.createdAt)
                        FullRecordCard(
                            entry = entry,
                            sugarColor = sugarColor,
                            afterMeal = pill,
                            fetchProducts = viewModel::getEntryProducts,
                            onEdit = { overlays.openRecordEdit(entry.id) },
                            onOpenComposition = { overlays.openRecordComposition(entry.id) },
                            onOpenPhoto = { overlays.openPhoto(entry.photoPath, caption) }
                        )
                    }
                    Spacer(Modifier.height(GlukoSpacing.itemGap))
                }
            }
        }

        item {
            Text(
                stringResource(R.string.history_no_earlier),
                style = GlukoType.Hint,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun historySummary(count: Int, days: Int): String {
    val entriesLabel = pluralStringResource(R.plurals.day_records_count, count, count)
    return "$entriesLabel · $days"
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
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Kicker(label)
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f).height(1.dp).background(GlukoColors.BorderDashed))
        Spacer(Modifier.width(10.dp))
        Text(pluralStringResource(R.plurals.day_records_count, count, count), style = GlukoType.Hint.tabular)
    }
}

@Composable
private fun photoCaption(createdAt: LocalDateTime): String {
    val today = LocalDate.now()
    val dayLabel = when (createdAt.toLocalDate()) {
        today -> stringResource(R.string.today_label)
        today.minusDays(1) -> stringResource(R.string.yesterday_label)
        else -> createdAt.toLocalDate().format(DateTimeFormatter.ofPattern("d MMMM"))
    }
    return "${dayLabel.lowercase()}, ${createdAt.format(DateTimeFormatter.ofPattern("HH:mm"))}"
}

@Composable
private fun CompactRecordRow(entry: DiaryEntry, sugarColor: Color, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .shadowSoft(GlukoRadius.tile)
            .clip(RoundedCornerShape(GlukoRadius.tile))
            .background(GlukoColors.Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(entry.createdAt.format(DateTimeFormatter.ofPattern("HH:mm")), style = GlukoType.RecordTime.tabular)
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.record_measurement_tag), style = GlukoType.Hint)
        Spacer(Modifier.weight(1f))
        LucideIcon(LucidePaths.Droplet, 12.dp, GlukoColors.TextLabel, strokeWidth = 1.8f)
        Spacer(Modifier.width(4.dp))
        entry.bloodSugar?.let {
            Text("%.1f".format(it), style = GlukoType.Value.copy(color = sugarColor).tabular)
            Spacer(Modifier.width(3.dp))
            Text(stringResource(R.string.mmol_unit), style = GlukoType.Unit)
        }
    }
}

// Strict, not a minimum - matching the fixed-size preset cards/cubes elsewhere (MealPresetsScreen,
// FoodPicker) - so a portrait photo can't stretch this box (and the row) taller than the preset
// tile next to it.
private val RecordMediaHeight = 88.dp

@Composable
private fun FullRecordCard(
    entry: DiaryEntry,
    sugarColor: Color,
    afterMeal: AfterMeal?,
    fetchProducts: suspend (Long) -> List<DiaryEntryProduct>,
    onEdit: () -> Unit,
    onOpenComposition: () -> Unit,
    onOpenPhoto: () -> Unit
) {
    var products by remember(entry.id) { mutableStateOf<List<DiaryEntryProduct>>(emptyList()) }
    LaunchedEffect(entry.id, entry.mealLabel) {
        products = if (entry.mealLabel != null) fetchProducts(entry.id) else emptyList()
    }

    GlukoCard(radius = GlukoRadius.record, padding = 0.dp, modifier = Modifier.plainClickable(onClick = onEdit)) {
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 15.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(entry.createdAt.format(DateTimeFormatter.ofPattern("HH:mm")), style = GlukoType.RecordTime.tabular)
                Spacer(Modifier.width(9.dp))
                Box(
                    Modifier.clip(RoundedCornerShape(GlukoRadius.pill)).background(GlukoColors.Tile).padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Text(entry.mealLabel ?: stringResource(R.string.record_no_preset), style = GlukoType.CardLabel.copy(fontSize = 11.sp), maxLines = 1)
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column {
                    Text(stringResource(R.string.record_sugar_label), style = GlukoType.CardLabel)
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(entry.bloodSugar?.let { "%.1f".format(it) } ?: "—", style = GlukoType.ValueLarge.copy(color = sugarColor).tabular)
                        Spacer(Modifier.width(3.dp))
                        Text(stringResource(R.string.mmol_unit), style = GlukoType.Unit)
                    }
                }
                Spacer(Modifier.width(14.dp))
                Box(Modifier.width(1.dp).heightIn(min = 40.dp).background(GlukoColors.Divider))
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.record_insulin_label), style = GlukoType.CardLabel)
                    Spacer(Modifier.height(3.dp))
                    Row {
                        InsulinValue(
                            entry.shortInsulinDose,
                            stringResource(R.string.record_short_label),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(10.dp))
                        InsulinValue(
                            entry.longInsulinDose,
                            stringResource(R.string.record_long_label),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (afterMeal != null) {
                AfterMealPill(afterMeal)
                Spacer(Modifier.height(13.dp))
            } else {
                Spacer(Modifier.height(13.dp))
                GlukoDivider()
                Spacer(Modifier.height(13.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(GlukoSpacing.itemGap)) {
                Column(
                    Modifier
                        .weight(1f)
                        .height(RecordMediaHeight)
                        .clip(RoundedCornerShape(GlukoRadius.tile))
                        .background(GlukoColors.Tile)
                        // Only a real preset has a recorded composition to show; anything else falls
                        // back to editing, like the rest of the card.
                        .clickable(onClick = if (entry.mealLabel != null) onOpenComposition else onEdit)
                        .padding(horizontal = 13.dp, vertical = 11.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            entry.mealLabel ?: stringResource(R.string.record_no_preset),
                            style = GlukoType.Body, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(8.dp))
                        entry.breadUnits?.let { XeBadge(stringResource(R.string.bread_units_value_format, formatAmount(it))) }
                    }
                    Text(
                        products.sortedBy { it.sortOrder }.joinToString(", ") { it.name },
                        style = GlukoType.CardLabel.copy(fontSize = 11.sp),
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    if (entry.mealLabel != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.record_composition), style = GlukoType.CardLabel.copy(color = GlukoColors.TextTertiary))
                            Spacer(Modifier.width(5.dp))
                            LucideIcon(LucidePaths.ChevronRight, 11.dp, GlukoColors.TextTertiary, strokeWidth = 2.2f)
                        }
                    }
                }

                if (entry.photoPath != null) {
                    Box(
                        Modifier
                            .width(88.dp).height(RecordMediaHeight)
                            .clip(RoundedCornerShape(GlukoRadius.tile))
                            .clickable(onClick = onOpenPhoto),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = java.io.File(entry.photoPath),
                            contentDescription = stringResource(R.string.entry_photo),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(GlukoRadius.tile))
                        )
                        Box(
                            Modifier.align(Alignment.BottomEnd).padding(6.dp).width(20.dp).heightIn(min = 20.dp)
                                .clip(RoundedCornerShape(GlukoRadius.pill)).background(GlukoColors.Surface),
                            contentAlignment = Alignment.Center
                        ) {
                            LucideIcon(LucidePaths.Expand, 11.dp, strokeWidth = 2.2f)
                        }
                    }
                } else {
                    Box(
                        Modifier.width(88.dp).height(RecordMediaHeight).dashedBorder(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.record_no_photo), style = GlukoType.CardLabel.copy(color = GlukoColors.Placeholder), textAlign = TextAlign.Center)
                    }
                }
            }

            if (entry.notes.isNotBlank()) {
                Spacer(Modifier.height(13.dp))
                GlukoDivider()
                Spacer(Modifier.height(12.dp))
                Text(entry.notes, style = GlukoType.Note, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun InsulinValue(dose: Float?, label: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.Bottom) {
        Text(dose?.let { formatAmount(it) } ?: "—", style = GlukoType.Value.tabular, maxLines = 1)
        Spacer(Modifier.width(5.dp))
        Text(label, style = GlukoType.Unit, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun RecordEditSheet(viewModel: DiaryViewModel, entryId: Long, overlays: OverlayController, onDismiss: () -> Unit) {
    val entries by viewModel.entries.collectAsState()
    val entry = entries.find { it.id == entryId } ?: run { onDismiss(); return }
    val mealPresets by viewModel.mealPresets.collectAsState()
    val breadUnitsFormat = stringResource(R.string.bread_units_value_format)
    val manualXeFormat = stringResource(R.string.food_manual_format)
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val toasts = rememberAppToasts()
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    val initialBloodSugarText = remember(entryId) {
        entry.bloodSugar?.let { "%.1f".format(Locale.US, it) } ?: ""
    }
    var formState by remember(entryId) {
        mutableStateOf(
            EntryFormState(
                date = entry.createdAt.toLocalDate(),
                time = entry.createdAt.toLocalTime(),
                bloodSugar = initialBloodSugarText,
                breadUnits = entry.breadUnits?.let { formatAmount(it) } ?: "",
                foodLabel = entry.mealLabel ?: entry.breadUnits?.let { breadUnitsFormat.format(formatAmount(it)) } ?: "",
                mealLabel = entry.mealLabel,
                shortInsulinDose = entry.shortInsulinDose?.toString() ?: "",
                longInsulinDose = entry.longInsulinDose?.toString() ?: "",
                notes = entry.notes,
                photoPath = entry.photoPath
            )
        )
    }

    LaunchedEffect(entryId) {
        if (entry.mealLabel != null) {
            val products = viewModel.getEntryProducts(entryId)
            if (products.isNotEmpty()) {
                formState = formState.copy(
                    mealProducts = products.sortedBy { it.sortOrder }.map { MealProductEntry(it.name, formatAmount(it.breadUnits)) }
                )
            }
        }
    }

    fun save() {
        val newCreatedAt = LocalDateTime.of(formState.date, formState.time)
        val typedBloodSugar = formState.bloodSugar.toFloatOrNull()
        val sugarFieldUntouched = formState.bloodSugar == initialBloodSugarText
        val timeChanged = newCreatedAt != entry.createdAt

        suspend fun resolveSugar(): Pair<Float?, SugarSource?> = when {
            entry.sugarSource == SugarSource.SENSOR && sugarFieldUntouched && timeChanged -> {
                val resynced = viewModel.sensorReadingNear(newCreatedAt)
                resynced to resynced?.let { SugarSource.SENSOR }
            }
            sugarFieldUntouched -> typedBloodSugar to entry.sugarSource
            else -> typedBloodSugar to typedBloodSugar?.let { SugarSource.MANUAL }
        }

        coroutineScope.launch {
            val (newBloodSugar, newSugarSource) = resolveSugar()
            viewModel.updateEntry(
                entry.copy(
                    bloodSugar = newBloodSugar,
                    sugarSource = newSugarSource,
                    breadUnits = formState.breadUnits.toFloatOrNull(),
                    mealLabel = formState.mealLabel,
                    shortInsulinDose = formState.shortInsulinDose.toFloatOrNull(),
                    longInsulinDose = formState.longInsulinDose.toFloatOrNull(),
                    notes = formState.notes.trim(),
                    photoPath = formState.photoPath,
                    createdAt = newCreatedAt
                ),
                formState.mealProducts.mapIndexed { index, product ->
                    DiaryEntryProduct(
                        diaryEntryId = entry.id,
                        name = product.name,
                        breadUnits = product.breadUnits.toFloatOrNull() ?: 0f,
                        sortOrder = index
                    )
                }
            )
            toasts.recordEdited()
        }
        onDismiss()
    }

    GlukoSheet(onDismiss, maxHeightFraction = 0.9f) {
        SheetHeader(stringResource(R.string.record_edit_title), onClose = onDismiss)

        Column(Modifier.verticalScroll(rememberScrollState())) {
            Row(horizontalArrangement = Arrangement.spacedBy(GlukoSpacing.itemGap)) {
                PickerButton(
                    Modifier.weight(1f), LucidePaths.Calendar,
                    formState.date.format(DateTimeFormatter.ofPattern("d MMM"))
                ) { overlays.openDatePicker(formState.date) { picked -> formState = formState.copy(date = picked) } }
                PickerButton(
                    Modifier.weight(1f), LucidePaths.Clock,
                    formState.time.format(DateTimeFormatter.ofPattern("HH:mm"))
                ) { overlays.openTimePicker(formState.time) { picked -> formState = formState.copy(time = picked) } }
            }

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LucideIcon(LucidePaths.Droplet, 14.dp, GlukoColors.TextLabel, strokeWidth = 1.7f)
                Spacer(Modifier.width(7.dp))
                Kicker(stringResource(R.string.sugar_level_label))
            }
            Spacer(Modifier.height(8.dp))
            GlukoField(
                formState.bloodSugar, { formState = formState.copy(bloodSugar = numeric(it)) }, "0.0",
                textStyle = GlukoType.Body.tabular, keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done
            )

            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(GlukoSpacing.itemGap)) {
                Column(Modifier.weight(1f)) {
                    Kicker(stringResource(R.string.short_insulin_short_label))
                    Spacer(Modifier.height(8.dp))
                    GlukoField(
                        formState.shortInsulinDose, { formState = formState.copy(shortInsulinDose = numeric(it)) }, "0",
                        textStyle = GlukoType.Body.tabular, keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done
                    )
                }
                Column(Modifier.weight(1f)) {
                    Kicker(stringResource(R.string.long_insulin_short_label))
                    Spacer(Modifier.height(8.dp))
                    GlukoField(
                        formState.longInsulinDose, { formState = formState.copy(longInsulinDose = numeric(it)) }, "0",
                        textStyle = GlukoType.Body.tabular, keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Kicker(stringResource(R.string.food_label))
            Spacer(Modifier.height(8.dp))
            FoodPicker(
                value = formState.foodLabel,
                expanded = formState.foodExpanded,
                presets = mealPresets.map { it.toPresetOption() },
                manualXe = formState.manualXe,
                placeholder = stringResource(R.string.food_placeholder_edit),
                onToggle = { formState = formState.copy(foodExpanded = !formState.foodExpanded) },
                onPick = { option ->
                    val preset = mealPresets.find { it.preset.id == option.id }
                    if (preset != null) formState = applyPresetPick(formState, preset, option)
                },
                onManualXeChange = { formState = updateManualXe(formState, it, manualXeFormat) },
                onManualXeDone = { formState = finishManualXe(formState) },
                onCreatePreset = { overlays.openPresetEdit(null) }
            )

            Spacer(Modifier.height(16.dp))
            Kicker(stringResource(R.string.comment_label))
            Spacer(Modifier.height(8.dp))
            GlukoField(
                formState.notes, { formState = formState.copy(notes = it.take(NOTES_MAX_LENGTH)) },
                stringResource(R.string.comment_placeholder), singleLine = false, minLines = 2
            )

            Spacer(Modifier.height(14.dp))
            val photoPicker = rememberPhotoPicker(
                photoPath = formState.photoPath,
                onPhotoChanged = { formState = formState.copy(photoPath = it) },
                overlays = overlays
            )
            PhotoButton(hasPhoto = formState.photoPath != null, onOpenPicker = photoPicker.open)

            Spacer(Modifier.height(14.dp))
            SaveEntryButton(enabled = formState.isFillable, onSave = ::save)
            Spacer(Modifier.height(9.dp))
            SecondaryButton(
                stringResource(R.string.delete_entry_pill), Modifier.fillMaxWidth(),
                textColor = GlukoColors.TextLabel, onClick = { showDeleteConfirm = true }
            )
        }
    }

    if (showDeleteConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_entry_confirm_title)) },
            text = { Text(stringResource(R.string.delete_entry_confirm_text)) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    viewModel.deleteEntry(entry) { undo -> toasts.recordDeleted(entry.createdAt, undo) }
                    showDeleteConfirm = false
                    onDismiss()
                }) {
                    Text(stringResource(R.string.delete), color = com.neojelll.diaxtracker.ui.theme.DangerRed)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
