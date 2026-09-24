package com.neojelll.diaxtracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.DiaryEntryProduct
import com.neojelll.diaxtracker.ui.components.CircleButton
import com.neojelll.diaxtracker.ui.components.FoodPicker
import com.neojelll.diaxtracker.ui.components.GlukoCard
import com.neojelll.diaxtracker.ui.components.GlukoDivider
import com.neojelll.diaxtracker.ui.components.GlukoField
import com.neojelll.diaxtracker.ui.components.GlukoTile
import com.neojelll.diaxtracker.ui.components.Kicker
import com.neojelll.diaxtracker.ui.components.LucideIcon
import com.neojelll.diaxtracker.ui.components.LucidePaths
import com.neojelll.diaxtracker.ui.components.OverlayController
import com.neojelll.diaxtracker.ui.components.PhotoTile
import com.neojelll.diaxtracker.ui.components.PrimaryButton
import com.neojelll.diaxtracker.ui.components.dashedBorder
import com.neojelll.diaxtracker.ui.components.numeric
import com.neojelll.diaxtracker.ui.components.plainClickable
import com.neojelll.diaxtracker.ui.components.toPresetOption
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoRadius
import com.neojelll.diaxtracker.ui.theme.GlukoSpacing
import com.neojelll.diaxtracker.ui.theme.GlukoType
import com.neojelll.diaxtracker.ui.theme.tabular
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun AddEntryScreen(viewModel: DiaryViewModel, overlays: OverlayController) {
    var formState by remember { mutableStateOf(EntryFormState()) }
    val sensorWarningVisible by viewModel.sensorWarningVisible.collectAsState()
    val mealPresets by viewModel.mealPresets.collectAsState()
    val entries by viewModel.entries.collectAsState()

    val todayCount = remember(entries) {
        val today = LocalDate.now()
        entries.count { it.createdAt.toLocalDate() == today }
    }
    val greetingRes = remember {
        when (LocalTime.now().hour) {
            in 5..10 -> R.string.greeting_morning
            in 11..16 -> R.string.greeting_afternoon
            in 17..22 -> R.string.greeting_evening
            else -> R.string.greeting_night
        }
    }
    val manualXeFormat = stringResource(R.string.food_manual_format)

    val pendingPreset = overlays.pendingPresetSelection
    val pendingPresetOption = pendingPreset?.toPresetOption()
    LaunchedEffect(pendingPreset) {
        if (pendingPreset != null && pendingPresetOption != null) {
            formState = applyPresetPick(formState, pendingPreset, pendingPresetOption)
            overlays.consumePendingPresetSelection()
        }
    }

    val scrollState = rememberScrollState()
    val scrollEnabled by remember { derivedStateOf { scrollState.maxValue > 0 } }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = GlukoSpacing.screenHorizontal)
            .padding(top = 18.dp)
            .verticalScroll(scrollState, enabled = scrollEnabled)
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(greetingRes), style = GlukoType.ScreenTitle)
                Spacer(Modifier.height(5.dp))
                Text(pluralStringResource(R.plurals.today_entries_count, todayCount, todayCount), style = GlukoType.Label)
            }
            NotificationButton(onClick = overlays::openNotifications)
        }
        Spacer(Modifier.height(14.dp))

        if (sensorWarningVisible) {
            SensorWarningBanner()
            Spacer(Modifier.height(GlukoSpacing.cardGap))
        }

        GlukoCard(padding = 10.dp) {
            Row(horizontalArrangement = Arrangement.spacedBy(GlukoSpacing.itemGap)) {
                PickerButton(
                    modifier = Modifier.weight(1f),
                    iconPath = LucidePaths.Calendar,
                    text = formState.date.format(DateTimeFormatter.ofPattern("d MMM")),
                    onClick = {
                        overlays.openDatePicker(formState.date) { picked -> formState = formState.copy(date = picked) }
                    }
                )
                PickerButton(
                    modifier = Modifier.weight(1f),
                    iconPath = LucidePaths.Clock,
                    text = formState.time.format(DateTimeFormatter.ofPattern("HH:mm")),
                    onClick = {
                        overlays.openTimePicker(formState.time) { picked -> formState = formState.copy(time = picked) }
                    }
                )
            }
        }
        Spacer(Modifier.height(GlukoSpacing.cardGap))

        GlukoCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                LucideIcon(LucidePaths.Droplet, 14.dp, GlukoColors.TextLabel, strokeWidth = 1.7f)
                Spacer(Modifier.width(7.dp))
                Text(stringResource(R.string.sugar_level_label), style = GlukoType.Label)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                GlukoField(
                    value = formState.bloodSugar,
                    onValueChange = { formState = formState.copy(bloodSugar = numeric(it)) },
                    placeholder = "0.0",
                    modifier = Modifier.width(92.dp),
                    textStyle = GlukoType.DisplaySugar.tabular,
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
                    background = GlukoColors.Surface,
                    padding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    cursorBrush = SolidColor(GlukoColors.CursorSoft)
                )
                Text(stringResource(R.string.mmol_unit), style = GlukoType.Label)
            }

            Spacer(Modifier.height(13.dp))
            GlukoDivider()
            Spacer(Modifier.height(13.dp))

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
        }
        Spacer(Modifier.height(GlukoSpacing.cardGap))

        GlukoCard {
            Kicker(stringResource(R.string.food_label))
            Spacer(Modifier.height(8.dp))
            FoodPicker(
                value = formState.foodLabel,
                expanded = formState.foodExpanded,
                presets = mealPresets.map { it.toPresetOption() },
                manualXe = formState.manualXe,
                placeholder = stringResource(R.string.food_placeholder_home),
                onToggle = { formState = formState.copy(foodExpanded = !formState.foodExpanded) },
                onPick = { option ->
                    val preset = mealPresets.find { it.preset.id == option.id }
                    if (preset != null) formState = applyPresetPick(formState, preset, option)
                },
                onManualXeChange = { formState = updateManualXe(formState, it, manualXeFormat) },
                onManualXeDone = { formState = finishManualXe(formState) },
                onCreatePreset = { overlays.openPresetEdit(null) }
            )
        }
        Spacer(Modifier.height(GlukoSpacing.cardGap))

        GlukoCard {
            Row(
                Modifier
                    .fillMaxWidth()
                    .plainClickable { formState = formState.copy(detailsExpanded = !formState.detailsExpanded) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.details_toggle_title), style = GlukoType.Body)
                    Spacer(Modifier.height(2.dp))
                    Text(stringResource(R.string.details_toggle_hint), style = GlukoType.Hint)
                }
                CircleButton(26.dp, GlukoColors.Tile) {
                    LucideIcon(
                        if (formState.detailsExpanded) LucidePaths.ChevronUp else LucidePaths.ChevronDown,
                        13.dp, strokeWidth = 2.2f
                    )
                }
            }
            AnimatedVisibility(formState.detailsExpanded, enter = fadeIn(), exit = fadeOut()) {
                Column(Modifier.padding(top = 13.dp)) {
                    GlukoField(
                        value = formState.notes,
                        onValueChange = { formState = formState.copy(notes = it.take(NOTES_MAX_LENGTH)) },
                        placeholder = stringResource(R.string.comment_placeholder),
                        singleLine = false,
                        minLines = 3
                    )
                    Spacer(Modifier.height(9.dp))
                    val photoPicker = rememberPhotoPicker(
                        photoPath = formState.photoPath,
                        onPhotoChanged = { formState = formState.copy(photoPath = it) },
                        overlays = overlays
                    )
                    PhotoTile(
                        photoPath = formState.photoPath,
                        onOpenPicker = photoPicker.open,
                        onRemove = photoPicker.remove
                    )
                }
            }
        }
        Spacer(Modifier.height(GlukoSpacing.cardGap))

        SaveEntryButton(
            enabled = formState.isFillable,
            onSave = {
                viewModel.addEntry(
                    bloodSugar = formState.bloodSugar.toFloatOrNull(),
                    breadUnits = formState.breadUnits.toFloatOrNull(),
                    mealLabel = formState.mealLabel,
                    mealProducts = formState.mealProducts.mapIndexed { index, product ->
                        DiaryEntryProduct(
                            diaryEntryId = 0,
                            name = product.name,
                            breadUnits = product.breadUnits.toFloatOrNull() ?: 0f,
                            sortOrder = index
                        )
                    },
                    shortInsulinDose = formState.shortInsulinDose.toFloatOrNull(),
                    longInsulinDose = formState.longInsulinDose.toFloatOrNull(),
                    notes = formState.notes.trim(),
                    photoPath = formState.photoPath,
                    createdAt = LocalDateTime.of(formState.date, formState.time)
                )
                formState = EntryFormState()
            }
        )
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

@Composable
internal fun NotificationButton(onClick: () -> Unit) {
    Box {
        CircleButton(42.dp, GlukoColors.Surface, onClick) {
            LucideIcon(LucidePaths.Bell, 19.dp, strokeWidth = 1.7f)
        }
    }
}

@Composable
internal fun SensorWarningBanner() {
    GlukoCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LucideIcon(LucidePaths.AlertTriangle, 16.dp, GlukoColors.Ink, strokeWidth = 1.7f)
            Spacer(Modifier.width(10.dp))
            Text(stringResource(R.string.sensor_warning), style = GlukoType.Body)
        }
    }
}
