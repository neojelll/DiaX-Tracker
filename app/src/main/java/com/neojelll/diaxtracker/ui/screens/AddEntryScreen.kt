package com.neojelll.diaxtracker.ui.screens

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.DiaryEntryProduct
import com.neojelll.diaxtracker.data.carbsGramsOn
import com.neojelll.diaxtracker.insulin.insulinOn
import com.neojelll.diaxtracker.ui.components.LocalDockClearance
import com.neojelll.diaxtracker.ui.components.DaySummaryCard
import com.neojelll.diaxtracker.ui.components.dashedBorder
import com.neojelll.diaxtracker.ui.components.formatSummaryValue
import com.neojelll.diaxtracker.ui.components.LocalCarbDisplay
import com.neojelll.diaxtracker.ui.components.rememberAppToasts
import com.neojelll.diaxtracker.ui.components.GlukoCard
import com.neojelll.diaxtracker.ui.components.GlukoDivider
import com.neojelll.diaxtracker.ui.components.GlukoField
import com.neojelll.diaxtracker.ui.components.GlukoTile
import com.neojelll.diaxtracker.ui.components.LucideIcon
import com.neojelll.diaxtracker.ui.components.LucidePaths
import com.neojelll.diaxtracker.ui.components.OverlayController
import com.neojelll.diaxtracker.ui.components.PrimaryButton
import com.neojelll.diaxtracker.ui.components.numeric
import com.neojelll.diaxtracker.ui.components.rememberCurrentMinute
import com.neojelll.diaxtracker.ui.components.toPresetOption
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoRadius
import com.neojelll.diaxtracker.ui.theme.GlukoSpacing
import com.neojelll.diaxtracker.ui.theme.GlukoType
import com.neojelll.diaxtracker.ui.theme.tabular
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun AddEntryScreen(viewModel: DiaryViewModel, overlays: OverlayController) {
    var formState by remember { mutableStateOf(EntryFormState()) }
    val now = rememberCurrentMinute()
    val shownAt = formState.dateTimeAt(now)
    // "Сейчас" in the pickers only once something was picked - while live it would do nothing.
    val backToNow: (() -> Unit)? = if (formState.isPinned) ({ formState = formState.followingClock() }) else null
    val toasts = rememberAppToasts()
    val latestSensorReading by viewModel.latestSensorReading.collectAsState()
    val mealPresets by viewModel.mealPresets.collectAsState()
    val entries by viewModel.entries.collectAsState()

    // Both follow the clock, so they stay right while the screen sits open past an hour or midnight.
    val today = now.toLocalDate()
    val entryProducts by viewModel.entryProducts.collectAsState()
    val carbsGramsToday = remember(entries, entryProducts, today) { carbsGramsOn(today, entries, entryProducts) }
    val insulinToday = remember(entries, today) { insulinOn(today, entries) }
    val greetingRes = greetingFor(now.hour)
    val carbDisplay = LocalCarbDisplay.current
    val presetOptions = mealPresets.map { it.toPresetOption() }

    val pendingPreset = overlays.pendingPresetSelection
    val pendingPresetOption = pendingPreset?.toPresetOption()
    LaunchedEffect(pendingPreset) {
        if (pendingPreset != null && pendingPresetOption != null) {
            formState = applyPresetPick(formState, pendingPreset, pendingPresetOption, carbDisplay)
            overlays.consumePendingPresetSelection()
        }
    }

    val nowMillis = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    val scrollState = rememberScrollState()
    val scrollEnabled by remember { derivedStateOf { scrollState.maxValue > 0 } }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = GlukoSpacing.screenHorizontal)
            .padding(top = 12.dp)
            .verticalScroll(scrollState, enabled = scrollEnabled)
            .padding(bottom = LocalDockClearance.current)
    ) {
        // Header: greeting and the bell. The day summary below replaced the "N entries today" line.
        Row(
            Modifier.fillMaxWidth().padding(start = 8.dp, end = 4.dp, bottom = 10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(stringResource(greetingRes), style = GlukoType.ScreenTitle, modifier = Modifier.weight(1f))
            NotificationButton(onClick = overlays::openNotifications)
        }

        DaySummaryCard(
            latestSensorReading,
            nowMillis = nowMillis,
            carbsGramsToday = carbsGramsToday,
            insulinToday = insulinToday
        )
        Spacer(Modifier.height(8.dp))

        // The entry form is ONE card: sugar -> insulin -> food -> details -> buttons, sections split
        // by hairlines across the content width; 16dp of air before the buttons instead of a line.
        GlukoCard(padding = PaddingValues(start = 16.dp, end = 14.dp, top = 12.dp, bottom = 14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                LucideIcon(LucidePaths.Droplet, 14.dp, GlukoColors.TextLabel, strokeWidth = 1.7f)
                Spacer(Modifier.width(7.dp))
                Text(stringResource(R.string.sugar_card_label), style = GlukoType.Label, modifier = Modifier.weight(1f))
                DateTimeChip(
                    date = shownAt.format(DateTimeFormatter.ofPattern("d MMM")),
                    time = shownAt.format(DateTimeFormatter.ofPattern("HH:mm")),
                    onDate = {
                        overlays.openDatePicker(shownAt.toLocalDate(), onNow = backToNow) { picked ->
                            formState = formState.withDate(picked, LocalDateTime.now())
                        }
                    },
                    onTime = {
                        overlays.openTimePicker(shownAt.toLocalTime(), onNow = backToNow) { picked ->
                            formState = formState.withTime(picked, LocalDateTime.now())
                        }
                    }
                )
            }
            Spacer(Modifier.height(2.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                GlukoField(
                    value = formState.bloodSugar,
                    onValueChange = { formState = formState.copy(bloodSugar = numeric(it)) },
                    placeholder = "0.0",
                    modifier = Modifier.width(84.dp),
                    textStyle = GlukoType.DisplaySugar.tabular,
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
                    background = GlukoColors.Surface,
                    padding = PaddingValues(0.dp),
                    // No rounded clip: with zero padding it would crop the cursor at the corner (#80).
                    // The field is the card's own white, so the rounding wasn't visible anyway.
                    shape = RectangleShape,
                    cursorBrush = SolidColor(GlukoColors.CursorSoft)
                )
                Text(stringResource(R.string.mmol_unit), style = GlukoType.Label)
                Spacer(Modifier.weight(1f))
                sensorSugarToFill(formState, latestSensorReading, nowMillis)?.let { reading ->
                    SensorFillPill(
                        value = formatSummaryValue(reading.mmol),
                        onClick = { formState = formState.copy(bloodSugar = sugarFieldText(reading.mmol)) },
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            GlukoDivider()
            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(GlukoSpacing.itemGap)) {
                InsulinTile(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.short_insulin_short_label),
                    value = formState.shortInsulinDose,
                    onChange = { formState = formState.copy(shortInsulinDose = numeric(it)) }
                )
                InsulinTile(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.long_insulin_short_label),
                    value = formState.longInsulinDose,
                    onChange = { formState = formState.copy(longInsulinDose = numeric(it)) }
                )
            }

            Spacer(Modifier.height(12.dp))
            GlukoDivider()
            Spacer(Modifier.height(4.dp))

            FoodRow(
                selected = presetOptions.firstOrNull { it.id == formState.presetId },
                carbs = formState.carbs,
                presets = presetOptions,
                expanded = formState.foodExpanded,
                onToggle = { formState = formState.copy(foodExpanded = !formState.foodExpanded) },
                onPick = { option ->
                    val preset = mealPresets.find { it.preset.id == option.id }
                    if (preset != null) formState = applyPresetPick(formState, preset, option, carbDisplay)
                },
                onCarbsChange = { formState = typeCarbs(formState, it) },
                onCreatePreset = { overlays.openPresetEdit(null) },
                standalone = false
            )

            Spacer(Modifier.height(4.dp))
            GlukoDivider()
            Spacer(Modifier.height(4.dp))

            val photoPicker = rememberPhotoPicker(
                photoPath = formState.photoPath,
                onPhotoChanged = { formState = formState.copy(photoPath = it) },
                overlays = overlays
            )
            DetailsRow(
                note = formState.notes,
                noteOpen = formState.noteOpen,
                photoPath = formState.photoPath,
                onToggleNote = { formState = formState.copy(noteOpen = !formState.noteOpen) },
                onNoteChange = { formState = formState.copy(notes = it.take(NOTES_MAX_LENGTH)) },
                onAddPhoto = photoPicker.open,
                onRemovePhoto = photoPicker.remove,
                standalone = false
            )

            Spacer(Modifier.height(16.dp))
            FormActions(
                dirty = formState.isDirty,
                canSave = formState.isFillable,
                onReset = {
                    val previous = formState
                    formState = previous.cleared()
                    // The file stays until the undo window is over - undo brings the photo back.
                    val keepPhoto = previous.photoPath?.let { viewModel.discardFormPhoto(it) }
                    toasts.formCleared {
                        keepPhoto?.invoke()
                        formState = previous
                    }
                },
                onSave = {
                    val previous = formState
                    // The real moment of saving, not the last minute tick, while no time was picked.
                    val createdAt = formState.dateTimeAt(LocalDateTime.now())
                    viewModel.addEntry(
                        bloodSugar = formState.bloodSugar.toFloatOrNull(),
                        carbsGrams = carbDisplay.parse(formState.carbs),
                        mealLabel = formState.mealLabel,
                        mealProducts = formState.mealProducts.mapIndexed { index, product ->
                            DiaryEntryProduct(
                                diaryEntryId = 0,
                                name = product.name,
                                carbsGrams = carbDisplay.parse(product.carbs) ?: 0f,
                                sortOrder = index
                            )
                        },
                        shortInsulinDose = formState.shortInsulinDose.toFloatOrNull(),
                        longInsulinDose = formState.longInsulinDose.toFloatOrNull(),
                        notes = formState.notes.trim(),
                        photoPath = formState.photoPath,
                        createdAt = createdAt,
                        // The toast waits for the id, so its undo can always take that exact entry back.
                        onSaved = { entryId ->
                            toasts.recordSaved(createdAt) {
                                viewModel.unsaveEntry(entryId)
                                formState = previous
                            }
                        }
                    )
                    formState = EntryFormState()
                }
            )
        }
    }
}

/**
 * The form's buttons: "Сохранить запись" always there, greyed out until something worth saving is
 * entered; a round 50dp "clear" button slides in once anything is in the form.
 */
@Composable
private fun FormActions(dirty: Boolean, canSave: Boolean, onReset: () -> Unit, onSave: () -> Unit) {
    val bg by animateColorAsState(if (canSave) GlukoColors.Ink else GlukoColors.Border, label = "saveBg")
    val fg by animateColorAsState(if (canSave) GlukoColors.Surface else GlukoColors.Placeholder, label = "saveFg")
    val clearLabel = stringResource(R.string.form_clear)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        AnimatedVisibility(dirty, enter = fadeIn() + expandHorizontally(), exit = fadeOut() + shrinkHorizontally()) {
            Box(
                Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(GlukoRadius.pill))
                    .background(GlukoColors.Tile)
                    .clickable(onClickLabel = clearLabel, onClick = onReset)
                    .semantics { contentDescription = clearLabel },
                contentAlignment = Alignment.Center
            ) { LucideIcon(LucidePaths.RotateCcw, 16.dp, GlukoColors.Ink, strokeWidth = 2f) }
        }
        Row(
            Modifier
                .weight(1f)
                .height(50.dp)
                .clip(RoundedCornerShape(GlukoRadius.pill))
                .background(bg)
                .clickable(enabled = canSave, onClick = onSave),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.save_entry_button), style = GlukoType.ButtonPrimary.copy(color = fg))
            Spacer(Modifier.width(9.dp))
            LucideIcon(LucidePaths.ArrowRight, 16.dp, fg, strokeWidth = 2f)
        }
    }
}

@Composable
internal fun SaveEntryButton(
    enabled: Boolean,
    onSave: () -> Unit,
    text: String = stringResource(R.string.save_entry_button),
    showArrow: Boolean = true
) {
    if (enabled) {
        PrimaryButton(
            text = text,
            trailingIcon = if (showArrow) {
                { LucideIcon(LucidePaths.ArrowRight, 16.dp, GlukoColors.Surface) }
            } else null,
            onClick = onSave
        )
    } else {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(GlukoRadius.pill))
                .background(GlukoColors.Ink.copy(alpha = 0.35f))
                .padding(vertical = 16.dp, horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text, style = GlukoType.ButtonPrimary.copy(color = GlukoColors.Surface))
        }
    }
}

/** "6,4 с сенсора": a dashed pill beside the sugar field that puts the current sensor reading in. */
@Composable
private fun SensorFillPill(value: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(GlukoRadius.pill))
            .dashedBorder(color = GlukoColors.BorderHover, radius = GlukoRadius.pill)
            .clickable(onClick = onClick)
            .padding(start = 8.dp, end = 10.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LucideIcon(LucidePaths.CornerLeftDown, 12.dp, GlukoColors.TextSecondary, strokeWidth = 2.2f)
        Spacer(Modifier.width(5.dp))
        Text(
            stringResource(R.string.sugar_fill_from_sensor, value),
            style = GlukoType.Body.copy(fontSize = 12.sp, color = GlukoColors.TextSecondary).tabular,
            maxLines = 1
        )
    }
}

/**
 * The "📅 10 сент | 🕒 17:02" chip in the sugar card header. Two halves, each its own tap target
 * opening its own sheet. Pill on Tile, padding 3dp; halves 9x5dp, white while pressed.
 */
@Composable
private fun DateTimeChip(date: String, time: String, onDate: () -> Unit, onTime: () -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(GlukoRadius.pill))
            .background(GlukoColors.Tile)
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ChipHalf(LucidePaths.Calendar, date, onDate)
        Box(Modifier.width(1.dp).height(12.dp).background(GlukoColors.Border))
        ChipHalf(LucidePaths.Clock, time, onTime)
    }
}

@Composable
private fun ChipHalf(icon: String, text: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Row(
        Modifier
            .clip(RoundedCornerShape(GlukoRadius.pill))
            .background(if (pressed) GlukoColors.Surface else Color.Transparent)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LucideIcon(icon, 12.dp, strokeWidth = 2f)
        Spacer(Modifier.width(5.dp))
        Text(text, style = GlukoType.Body.copy(fontSize = 12.sp).tabular, maxLines = 1)
    }
}

@Composable
internal fun PickerButton(modifier: Modifier, iconPath: String, text: String, onClick: () -> Unit) {
    Row(
        modifier
            .clip(RoundedCornerShape(GlukoRadius.field))
            .background(GlukoColors.Tile)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LucideIcon(iconPath, 15.dp)
        Spacer(Modifier.width(8.dp))
        Text(text, style = GlukoType.Body.tabular, maxLines = 1)
    }
}

@Composable
private fun InsulinTile(modifier: Modifier, label: String, value: String, onChange: (String) -> Unit) {
    GlukoTile(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LucideIcon(LucidePaths.Syringe, 13.dp, GlukoColors.TextLabel, strokeWidth = 2f)
            Spacer(Modifier.width(5.dp))
            Text(label, style = GlukoType.Hint.copy(color = GlukoColors.TextLabel), maxLines = 1)
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            GlukoField(
                value = value,
                onValueChange = onChange,
                placeholder = "0",
                modifier = Modifier.weight(1f),
                textStyle = GlukoType.ValueLarge.tabular,
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done,
                background = androidx.compose.ui.graphics.Color.Transparent,
                padding = PaddingValues(start = 4.dp),
                cursorBrush = SolidColor(GlukoColors.CursorSoft)
            )
            Text(stringResource(R.string.units_short), style = GlukoType.Hint.copy(color = GlukoColors.TextLabel))
        }
    }
}

/** The bell without a round background: icon 21dp, tap area 29dp (4dp padding), pill-clipped press. */
@Composable
internal fun NotificationButton(onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(GlukoRadius.pill))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        LucideIcon(LucidePaths.Bell, 21.dp, strokeWidth = 1.8f)
    }
}

@StringRes
internal fun greetingFor(hour: Int): Int = when (hour) {
    in 5..10 -> R.string.greeting_morning
    in 11..16 -> R.string.greeting_afternoon
    in 17..22 -> R.string.greeting_evening
    else -> R.string.greeting_night
}
