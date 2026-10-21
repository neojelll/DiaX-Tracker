package com.neojelll.diaxtracker.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.aftermeal.AfterMeal
import com.neojelll.diaxtracker.aftermeal.afterMealPoints
import com.neojelll.diaxtracker.aftermeal.buildAfterMeal
import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.data.DiaryEntryProduct
import com.neojelll.diaxtracker.data.GlucoseRange
import com.neojelll.diaxtracker.data.SugarSource
import com.neojelll.diaxtracker.data.carbsGramsOn
import com.neojelll.diaxtracker.insulin.insulinOn
import com.neojelll.diaxtracker.ui.components.AfterMealLine
import com.neojelll.diaxtracker.ui.components.CircleButton
import com.neojelll.diaxtracker.ui.components.FilterChip
import com.neojelll.diaxtracker.ui.components.FoodPicker
import com.neojelll.diaxtracker.ui.components.GlukoCard
import com.neojelll.diaxtracker.ui.components.GlukoDivider
import com.neojelll.diaxtracker.ui.components.GlukoField
import com.neojelll.diaxtracker.ui.components.GlukoSheet
import com.neojelll.diaxtracker.ui.components.InsulinIcon
import com.neojelll.diaxtracker.ui.components.InsulinKind
import com.neojelll.diaxtracker.ui.components.Kicker
import com.neojelll.diaxtracker.ui.components.LocalCarbDisplay
import com.neojelll.diaxtracker.ui.components.LocalDockClearance
import com.neojelll.diaxtracker.ui.components.LucideIcon
import com.neojelll.diaxtracker.ui.components.LucidePaths
import com.neojelll.diaxtracker.ui.components.OverlayController
import com.neojelll.diaxtracker.ui.components.PhotoButton
import com.neojelll.diaxtracker.ui.components.PrimaryButton
import com.neojelll.diaxtracker.ui.components.SecondaryButton
import com.neojelll.diaxtracker.ui.components.SheetHeader
import com.neojelll.diaxtracker.ui.components.carbsText
import com.neojelll.diaxtracker.ui.components.carbsValueFormat
import com.neojelll.diaxtracker.ui.components.numeric
import com.neojelll.diaxtracker.ui.components.plainClickable
import com.neojelll.diaxtracker.ui.components.rememberAppToasts
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(viewModel: DiaryViewModel, overlays: OverlayController) {
    val entries by viewModel.entries.collectAsState()
    val entryProducts by viewModel.entryProducts.collectAsState()
    val glucoseRange by viewModel.glucoseRange.collectAsState()
    val carbGoal by viewModel.carbGoal.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var dateFilter by remember { mutableStateOf<DateFilter>(DateFilter.All) }
    var sortDescending by remember { mutableStateOf(true) }
    // One record open at a time; opening another closes the previous one.
    var openedId by rememberSaveable { mutableStateOf<Long?>(null) }

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
    val records = remember(entries, afterMeal) { afterMeal.records(entries) }

    val filtered = remember(records, productNamesByEntry, searchQuery, dateFilter, sortDescending) {
        val byDateAndText = records.filter { entry ->
            entry.matches(dateFilter) && (
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
            top = 18.dp, bottom = GlukoSpacing.bottomInset + LocalDockClearance.current
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

        // Each day's header is pinned while its records scroll under it. Its totals are the whole
        // day's - the same sums as on the home screen - not just what the search left visible.
        rows.forEach { row ->
            when (row) {
                is HistoryRow.DayHeader -> stickyHeader(key = "day-${row.date}") {
                    DayHeaderRow(
                        date = row.date,
                        carbsGrams = carbsGramsOn(row.date, records, entryProducts),
                        insulin = insulinOn(row.date, records),
                        carbGoalGrams = carbGoal.activeGrams
                    )
                }
                is HistoryRow.Entry -> item(key = row.entry.id) {
                    val entry = row.entry
                    val sugarColor = entry.bloodSugar?.let { glucoseColor(it, glucoseRange.low, glucoseRange.high) } ?: GlukoColors.Ink
                    val caption = photoCaption(entry.createdAt)
                    val pill = afterMeal.byMeal[entry.id]
                    val opensUp = entry.hasMoreInside(hasAfterMeal = pill != null)
                    val expanded = opensUp && openedId == entry.id
                    RecordCard(
                        entry = entry,
                        sugarColor = sugarColor,
                        afterMeal = pill,
                        expanded = if (opensUp) expanded else null,
                        range = glucoseRange,
                        now = now,
                        fetchProducts = viewModel::getEntryProducts,
                        // Nothing more inside - a tap edits, as before; otherwise it opens or closes the record.
                        onTap = {
                            if (!opensUp) overlays.openRecordEdit(entry.id)
                            else openedId = if (expanded) null else entry.id
                        },
                        onEdit = { overlays.openRecordEdit(entry.id) },
                        onOpenPhoto = { overlays.openPhoto(entry.photoPath, caption) }
                    )
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
    val daysLabel = pluralStringResource(R.plurals.history_days_count, days, days)
    return "$entriesLabel · $daysLabel"
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

/**
 * A History record: the one-line [RecordRow]; tapped open ([expanded] true), the rest under it -
 * the preset and photo, sugar after the meal, the note and the edit button. [expanded] null - there
 * is nothing more inside, the card stays a single line.
 */
@Composable
private fun RecordCard(
    entry: DiaryEntry,
    sugarColor: Color,
    afterMeal: AfterMeal?,
    expanded: Boolean?,
    range: GlucoseRange,
    now: LocalDateTime,
    fetchProducts: suspend (Long) -> List<DiaryEntryProduct>,
    onTap: () -> Unit,
    onEdit: () -> Unit,
    onOpenPhoto: () -> Unit
) {
    val open = expanded == true
    val lift by animateDpAsState(if (open) 12.dp else 0.dp, label = "recordLift")
    val shape = RoundedCornerShape(GlukoRadius.record)
    GlukoCard(
        radius = GlukoRadius.record,
        padding = 0.dp,
        modifier = Modifier
            .shadow(lift, shape, ambientColor = GlukoColors.Ink.copy(alpha = .06f), spotColor = GlukoColors.Ink.copy(alpha = .2f))
            .plainClickable(onClick = onTap)
    ) {
        Column(Modifier.animateContentSize().padding(start = 14.dp, end = 14.dp, top = 11.dp, bottom = 11.dp)) {
            RecordRow(entry, sugarColor, expanded = expanded)
            if (open) OpenedRecord(entry, afterMeal, range, now, fetchProducts, onEdit, onOpenPhoto)
        }
    }
}

/** What an opened record shows: "preset | photo" (no preset - the photo full width), sugar after the meal, the note, edit. */
@Composable
private fun OpenedRecord(
    entry: DiaryEntry,
    afterMeal: AfterMeal?,
    range: GlucoseRange,
    now: LocalDateTime,
    fetchProducts: suspend (Long) -> List<DiaryEntryProduct>,
    onEdit: () -> Unit,
    onOpenPhoto: () -> Unit
) {
    var products by remember(entry.id) { mutableStateOf<List<DiaryEntryProduct>>(emptyList()) }
    LaunchedEffect(entry.id, entry.mealLabel) {
        products = if (entry.mealLabel != null) fetchProducts(entry.id) else emptyList()
    }

    Spacer(Modifier.height(11.dp))
    GlukoDivider()
    Spacer(Modifier.height(11.dp))
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        val preset = entry.mealLabel
        val photo = entry.photoPath
        if (preset != null || photo != null) {
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                if (preset != null) PresetBlock(preset, products, Modifier.weight(1f))
                if (photo != null) {
                    val size = if (preset != null) Modifier.width(96.dp).fillMaxHeight().heightIn(min = 96.dp) else Modifier.weight(1f).height(150.dp)
                    RecordPhoto(photo, size, onOpenPhoto)
                }
            }
        }
        if (afterMeal != null) {
            AfterMealLine(
                afterMealPoints(entry.bloodSugar?.toDouble(), afterMeal, entry.createdAt, now),
                rangeLow = range.low,
                rangeHigh = range.high
            )
        }
        if (entry.notes.isNotBlank()) NoteBlock(entry.notes)
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(GlukoRadius.field))
                .background(GlukoColors.Tile)
                .clickable(onClick = onEdit)
                .padding(vertical = 11.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LucideIcon(LucidePaths.Pencil, 13.dp, GlukoColors.TextSecondary, strokeWidth = 2f)
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.record_edit_button), style = GlukoType.Body.copy(color = GlukoColors.TextSecondary))
        }
    }
}

/** The preset as recorded: its name, then each product with its carbs - the entry's own copy, not the live preset. */
@Composable
private fun PresetBlock(name: String, products: List<DiaryEntryProduct>, modifier: Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(GlukoRadius.field))
            .background(GlukoColors.Tile)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LucideIcon(LucidePaths.Bookmark, 13.dp, GlukoColors.TextSecondary, strokeWidth = 2f)
            Spacer(Modifier.width(6.dp))
            Text(name, style = GlukoType.Body.copy(fontWeight = FontWeight.Medium))
        }
        if (products.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.record_composition), style = GlukoType.CardLabel)
            products.sortedBy { it.sortOrder }.forEach { product ->
                Spacer(Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Text(product.name, style = GlukoType.Body.copy(fontSize = 12.5.sp), modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(8.dp))
                    Text(carbsText(product.carbsGrams), style = GlukoType.Hint.tabular, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun RecordPhoto(photoPath: String, modifier: Modifier, onOpenPhoto: () -> Unit) {
    AsyncImage(
        model = java.io.File(photoPath),
        contentDescription = stringResource(R.string.entry_photo),
        contentScale = ContentScale.Crop,
        modifier = modifier
            .clip(RoundedCornerShape(GlukoRadius.field))
            .background(GlukoColors.PhotoStub)
            .clickable(onClick = onOpenPhoto)
    )
}

/** The note in full - an opened record has room for it. */
@Composable
private fun NoteBlock(text: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GlukoRadius.field))
            .background(GlukoColors.Tile)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        LucideIcon(LucidePaths.MessageCircle, 13.dp, GlukoColors.TextLabel, strokeWidth = 1.9f, modifier = Modifier.padding(top = 2.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = GlukoType.Note.copy(color = GlukoColors.Ink))
    }
}

@Composable
fun RecordEditSheet(viewModel: DiaryViewModel, entryId: Long, overlays: OverlayController, onDismiss: () -> Unit) {
    val entries by viewModel.entries.collectAsState()
    val entry = entries.find { it.id == entryId } ?: run { onDismiss(); return }
    val mealPresets by viewModel.mealPresets.collectAsState()
    val carbDisplay = LocalCarbDisplay.current
    val carbsFormat = carbsValueFormat()
    val manualCarbsFormat = stringResource(R.string.food_manual_format, carbsFormat)
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val toasts = rememberAppToasts()
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    val initialBloodSugarText = remember(entryId) {
        entry.bloodSugar?.let { "%.1f".format(Locale.US, it) } ?: ""
    }
    var formState by remember(entryId) {
        mutableStateOf(
            EntryFormState(
                dateTime = entry.createdAt,
                bloodSugar = initialBloodSugarText,
                carbs = entry.carbsGrams?.let { carbDisplay.format(it) } ?: "",
                foodLabel = entry.mealLabel ?: entry.carbsGrams?.let { carbsFormat.format(carbDisplay.format(it)) } ?: "",
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
                    mealProducts = products.sortedBy { it.sortOrder }.map { MealProductEntry(it.name, carbDisplay.format(it.carbsGrams)) }
                )
            }
        }
    }

    fun save() {
        val newCreatedAt = formState.dateTimeAt(entry.createdAt)
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
                    carbsGrams = carbDisplay.parse(formState.carbs),
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
                        carbsGrams = carbDisplay.parse(product.carbs) ?: 0f,
                        sortOrder = index
                    )
                }
            )
            toasts.recordEdited()
        }
        onDismiss()
    }

    val shownAt = formState.dateTimeAt(entry.createdAt)

    GlukoSheet(onDismiss, maxHeightFraction = 0.9f) {
        SheetHeader(stringResource(R.string.record_edit_title), onClose = onDismiss)

        Column(Modifier.verticalScroll(rememberScrollState())) {
            Row(horizontalArrangement = Arrangement.spacedBy(GlukoSpacing.itemGap)) {
                PickerButton(
                    Modifier.weight(1f), LucidePaths.Calendar,
                    shownAt.format(DateTimeFormatter.ofPattern("d MMM"))
                ) { overlays.openDatePicker(shownAt.toLocalDate()) { picked -> formState = formState.withDate(picked, entry.createdAt) } }
                PickerButton(
                    Modifier.weight(1f), LucidePaths.Clock,
                    shownAt.format(DateTimeFormatter.ofPattern("HH:mm"))
                ) { overlays.openTimePicker(shownAt.toLocalTime()) { picked -> formState = formState.withTime(picked, entry.createdAt) } }
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InsulinIcon(InsulinKind.SHORT, 13.dp)
                        Spacer(Modifier.width(5.dp))
                        Kicker(stringResource(R.string.short_insulin_short_label))
                    }
                    Spacer(Modifier.height(8.dp))
                    GlukoField(
                        formState.shortInsulinDose, { formState = formState.copy(shortInsulinDose = numeric(it)) }, "0",
                        textStyle = GlukoType.Body.tabular, keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done
                    )
                }
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InsulinIcon(InsulinKind.LONG, 13.dp)
                        Spacer(Modifier.width(5.dp))
                        Kicker(stringResource(R.string.long_insulin_short_label))
                    }
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
                manualCarbs = formState.manualCarbs,
                placeholder = stringResource(R.string.food_placeholder_edit),
                onToggle = { formState = formState.copy(foodExpanded = !formState.foodExpanded) },
                onPick = { option ->
                    val preset = mealPresets.find { it.preset.id == option.id }
                    if (preset != null) formState = applyPresetPick(formState, preset, option, carbDisplay)
                },
                onManualCarbsChange = { formState = updateManualCarbs(formState, it, manualCarbsFormat) },
                onManualCarbsDone = { formState = finishManualCarbs(formState) },
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
