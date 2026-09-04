package com.neojelll.diaxtracker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.data.MealPreset
import com.neojelll.diaxtracker.photo.PhotoStore
import com.neojelll.diaxtracker.ui.theme.OnGlass
import com.neojelll.diaxtracker.ui.theme.OnGlassMuted
import com.neojelll.diaxtracker.ui.theme.SproutGreen
import com.neojelll.diaxtracker.ui.theme.glassPanel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

internal val DividerColor = OnGlass.copy(alpha = 0.12f)

@Composable
internal fun transparentFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    focusedTextColor = OnGlass,
    unfocusedTextColor = OnGlass,
    focusedLabelColor = OnGlassMuted,
    unfocusedLabelColor = OnGlassMuted,
    focusedPlaceholderColor = OnGlassMuted,
    unfocusedPlaceholderColor = OnGlassMuted,
    cursorColor = OnGlass
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CompactField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = OnGlass),
        cursorBrush = SolidColor(OnGlass),
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        interactionSource = interactionSource
    ) { innerTextField ->
        TextFieldDefaults.DecorationBox(
            value = value,
            innerTextField = innerTextField,
            enabled = true,
            singleLine = singleLine,
            visualTransformation = VisualTransformation.None,
            interactionSource = interactionSource,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            trailingIcon = trailingIcon,
            colors = transparentFieldColors(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

internal fun formatBreadUnits(value: Float): String =
    if (value == value.toInt().toFloat()) value.toInt().toString() else value.toString()

@Composable
internal fun SensorWarningBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassPanel(RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        Text(
            text = stringResource(R.string.sensor_warning),
            style = MaterialTheme.typography.bodyMedium,
            color = OnGlass
        )
    }
}

@Composable
internal fun InsulinActiveBanner(entries: List<DiaryEntry>) {
    if (entries.isEmpty()) return

    var now by remember(entries.map { it.id }) { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(entries.map { it.id }) {
        while (true) {
            now = LocalDateTime.now()
            delay(30_000L)
        }
    }

    @Composable
    fun remainingText(entry: DiaryEntry): String {
        val remaining = Duration.between(now, entry.createdAt.plusHours(4)).let {
            if (it.isNegative) Duration.ZERO else it
        }
        val totalMinutes = remaining.toMinutes()
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) {
            stringResource(R.string.duration_hours_minutes, hours, minutes)
        } else {
            stringResource(R.string.duration_minutes, minutes)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassPanel(RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Filled.Bolt, contentDescription = null, tint = SproutGreen)
        if (entries.size == 1) {
            Text(
                text = stringResource(R.string.insulin_active_banner, remainingText(entries[0])),
                style = MaterialTheme.typography.bodyMedium,
                color = OnGlass
            )
        } else {
            Column {
                Text(
                    text = stringResource(R.string.insulin_active_banner_multi_title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnGlass
                )
                entries.forEach { entry ->
                    entry.shortInsulinDose?.let { dose ->
                        Text(
                            text = stringResource(R.string.insulin_active_dose_line, dose.toString(), remainingText(entry)),
                            style = MaterialTheme.typography.bodySmall,
                            color = OnGlass
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EntryFormCard(
    state: EntryFormState,
    onStateChange: (EntryFormState) -> Unit,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    mealPresets: List<MealPreset> = emptyList()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val oldPath = state.photoPath
            coroutineScope.launch(Dispatchers.IO) {
                val newPath = PhotoStore.savePhoto(context, uri)
                if (newPath != null) {
                    withContext(Dispatchers.Main) {
                        onStateChange(state.copy(photoPath = newPath))
                    }
                    oldPath?.let { PhotoStore.deletePhoto(it) }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassPanel(RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            DateTimeRow(
                date = state.date,
                onDateClick = onDateClick,
                time = state.time,
                onTimeClick = onTimeClick
            )
            HorizontalDivider(color = DividerColor)

            CompactField(
                value = state.bloodSugar,
                onValueChange = { onStateChange(state.copy(bloodSugar = it.filter { c -> c.isDigit() || c == '.' })) },
                label = stringResource(R.string.blood_sugar_label),
                placeholder = stringResource(R.string.blood_sugar_placeholder),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            HorizontalDivider(color = DividerColor)

            val breadUnitsFormat = stringResource(R.string.bread_units_value_format)
            FoodField(
                foodLabel = state.foodLabel,
                mealPresets = mealPresets,
                onPresetSelected = { preset ->
                    onStateChange(
                        state.copy(
                            breadUnits = formatBreadUnits(preset.breadUnits),
                            foodLabel = preset.name
                        )
                    )
                },
                onManualEntryConfirmed = { value ->
                    onStateChange(
                        state.copy(
                            breadUnits = formatBreadUnits(value),
                            foodLabel = String.format(breadUnitsFormat, formatBreadUnits(value))
                        )
                    )
                }
            )
            HorizontalDivider(color = DividerColor)

            CompactField(
                value = state.shortInsulinDose,
                onValueChange = { onStateChange(state.copy(shortInsulinDose = it.filter { c -> c.isDigit() || c == '.' })) },
                label = stringResource(R.string.short_insulin_label),
                placeholder = stringResource(R.string.short_insulin_placeholder),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            HorizontalDivider(color = DividerColor)

            CompactField(
                value = state.longInsulinDose,
                onValueChange = { onStateChange(state.copy(longInsulinDose = it.filter { c -> c.isDigit() || c == '.' })) },
                label = stringResource(R.string.long_insulin_label),
                placeholder = stringResource(R.string.long_insulin_placeholder),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            HorizontalDivider(color = DividerColor)

            CompactField(
                value = state.notes,
                onValueChange = { onStateChange(state.copy(notes = it)) },
                label = stringResource(R.string.comment_label),
                placeholder = stringResource(R.string.comment_placeholder),
                singleLine = false,
                minLines = 1,
                maxLines = 3
            )
            HorizontalDivider(color = DividerColor)

            PhotoRow(
                photoPath = state.photoPath,
                onPickPhoto = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onRemovePhoto = {
                    PhotoStore.deletePhoto(state.photoPath)
                    onStateChange(state.copy(photoPath = null))
                }
            )
        }
    }
}

@Composable
private fun FoodField(
    foodLabel: String,
    mealPresets: List<MealPreset>,
    onPresetSelected: (MealPreset) -> Unit,
    onManualEntryConfirmed: (Float) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showManualDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.food_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = OnGlassMuted
                )
                Text(
                    text = foodLabel.ifBlank { stringResource(R.string.food_placeholder) },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (foodLabel.isBlank()) OnGlassMuted else OnGlass
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = OnGlassMuted
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            mealPresets.forEach { preset ->
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(
                                R.string.meal_preset_option_format,
                                preset.name,
                                formatBreadUnits(preset.breadUnits)
                            )
                        )
                    },
                    onClick = {
                        onPresetSelected(preset)
                        expanded = false
                    }
                )
            }
            if (mealPresets.isNotEmpty()) HorizontalDivider(color = DividerColor)
            DropdownMenuItem(
                text = { Text(stringResource(R.string.enter_manually)) },
                onClick = {
                    expanded = false
                    showManualDialog = true
                }
            )
        }
    }

    if (showManualDialog) {
        ManualFoodEntryDialog(
            onConfirm = {
                onManualEntryConfirmed(it)
                showManualDialog = false
            },
            onDismiss = { showManualDialog = false }
        )
    }
}

@Composable
private fun ManualFoodEntryDialog(
    onConfirm: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var breadUnitsText by remember { mutableStateOf("") }
    val breadUnits = breadUnitsText.toFloatOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.Black.copy(alpha = 0.75f),
        titleContentColor = OnGlass,
        textContentColor = OnGlass,
        title = { Text(stringResource(R.string.enter_manually)) },
        text = {
            CompactField(
                value = breadUnitsText,
                onValueChange = { breadUnitsText = it.filter { c -> c.isDigit() || c == '.' } },
                label = stringResource(R.string.bread_units_label),
                placeholder = stringResource(R.string.bread_units_placeholder),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        },
        confirmButton = {
            TextButton(
                enabled = breadUnits != null,
                onClick = { onConfirm(breadUnits!!) }
            ) {
                Text(stringResource(R.string.save), color = OnGlass)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = OnGlass)
            }
        }
    )
}

@Composable
private fun PhotoRow(
    photoPath: String?,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (photoPath == null) Modifier.clickable(onClick = onPickPhoto) else Modifier)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (photoPath != null) {
            AsyncImage(
                model = File(photoPath),
                contentDescription = stringResource(R.string.entry_photo),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onPickPhoto)
            )
            Text(
                text = stringResource(R.string.entry_photo),
                style = MaterialTheme.typography.bodyLarge,
                color = OnGlass,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRemovePhoto, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.remove_photo), tint = OnGlassMuted)
            }
        } else {
            Icon(Icons.Filled.AddAPhoto, contentDescription = null, tint = OnGlass)
            Text(
                text = stringResource(R.string.add_photo),
                style = MaterialTheme.typography.bodyLarge,
                color = OnGlass
            )
        }
    }
}

@Composable
private fun DateTimeRow(
    date: LocalDate,
    onDateClick: () -> Unit,
    time: LocalTime,
    onTimeClick: () -> Unit
) {
    val locale = LocalConfiguration.current.locales[0]
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onDateClick)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = stringResource(R.string.entry_date_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = OnGlassMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("d MMM yyyy", locale)),
                    style = MaterialTheme.typography.titleMedium,
                    color = OnGlass,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = OnGlass, modifier = Modifier.size(20.dp))
        }

        Box(
            modifier = Modifier
                .width(1.dp)
                .height(36.dp)
                .background(DividerColor)
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onTimeClick)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = stringResource(R.string.entry_time_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = OnGlassMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.titleMedium,
                    color = OnGlass,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Filled.Schedule, contentDescription = null, tint = OnGlass, modifier = Modifier.size(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TimePickerDialog(
    initialTime: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier.glassPanel(RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(state = state)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel), color = OnGlass)
                    }
                    TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) {
                        Text(stringResource(R.string.ok), color = OnGlass)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DatePickerDialog(
    initialDate: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis <= System.currentTimeMillis()
        }
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier.glassPanel(RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DatePicker(state = state, showModeToggle = false)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel), color = OnGlass)
                    }
                    TextButton(onClick = {
                        val millis = state.selectedDateMillis
                        if (millis != null) {
                            onConfirm(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                        }
                        onDismiss()
                    }) {
                        Text(stringResource(R.string.ok), color = OnGlass)
                    }
                }
            }
        }
    }
}
