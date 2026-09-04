package com.neojelll.diaxtracker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.data.MealPreset
import com.neojelll.diaxtracker.photo.PhotoStore
import com.neojelll.diaxtracker.ui.theme.CardBorder
import com.neojelll.diaxtracker.ui.theme.SproutGreen
import com.neojelll.diaxtracker.ui.theme.TextPrimary
import com.neojelll.diaxtracker.ui.theme.TextSecondary
import com.neojelll.diaxtracker.ui.theme.card
import com.neojelll.diaxtracker.ui.theme.fieldBox
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

@Composable
internal fun transparentFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedLabelColor = TextSecondary,
    unfocusedLabelColor = TextSecondary,
    focusedPlaceholderColor = TextSecondary,
    unfocusedPlaceholderColor = TextSecondary,
    cursorColor = TextPrimary
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
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
        cursorBrush = SolidColor(TextPrimary),
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

internal fun formatAmount(value: Float): String =
    if (value == value.toInt().toFloat()) value.toInt().toString() else value.toString()

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
        color = TextSecondary
    )
}

@Composable
internal fun SensorWarningBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .card(RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        Text(
            text = stringResource(R.string.sensor_warning),
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
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
            .card(RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Filled.Bolt, contentDescription = null, tint = SproutGreen)
        if (entries.size == 1) {
            Text(
                text = stringResource(R.string.insulin_active_banner, remainingText(entries[0])),
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
        } else {
            Column {
                Text(
                    text = stringResource(R.string.insulin_active_banner_multi_title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                entries.forEach { entry ->
                    entry.shortInsulinDose?.let { dose ->
                        Text(
                            text = stringResource(R.string.insulin_active_dose_line, dose.toString(), remainingText(entry)),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
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
    mealPresets: List<MealPreset> = emptyList(),
    cardTitle: String? = null,
    onReset: (() -> Unit)? = null
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .card(RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (cardTitle != null) {
            Text(cardTitle, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        }

        DateTimeSection(
            date = state.date,
            onDateClick = onDateClick,
            time = state.time,
            onTimeClick = onTimeClick
        )

        StepperField(
            label = stringResource(R.string.blood_sugar_label),
            value = state.bloodSugar,
            onValueChange = { onStateChange(state.copy(bloodSugar = it)) },
            step = 0.1f
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StepperField(
                label = stringResource(R.string.short_insulin_short_label),
                value = state.shortInsulinDose,
                onValueChange = { onStateChange(state.copy(shortInsulinDose = it)) },
                step = 0.5f,
                modifier = Modifier.weight(1f)
            )

            StepperField(
                label = stringResource(R.string.long_insulin_short_label),
                value = state.longInsulinDose,
                onValueChange = { onStateChange(state.copy(longInsulinDose = it)) },
                step = 0.5f,
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(color = CardBorder)

        val breadUnitsFormat = stringResource(R.string.bread_units_value_format)
        FoodField(
            foodLabel = state.foodLabel,
            mealPresets = mealPresets,
            onPresetSelected = { preset ->
                onStateChange(
                    state.copy(
                        breadUnits = formatAmount(preset.breadUnits),
                        foodLabel = preset.name
                    )
                )
            },
            onManualEntryConfirmed = { value ->
                onStateChange(
                    state.copy(
                        breadUnits = formatAmount(value),
                        foodLabel = String.format(breadUnitsFormat, formatAmount(value))
                    )
                )
            }
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            FieldLabel(stringResource(R.string.comment_label))
            Spacer(modifier = Modifier.height(4.dp))
            PlainTextField(
                value = state.notes,
                onValueChange = { onStateChange(state.copy(notes = it)) },
                placeholder = stringResource(R.string.comment_placeholder),
                singleLine = false,
                minLines = 1,
                maxLines = 2
            )
        }

        ActionsRow(
            photoPath = state.photoPath,
            onPickPhoto = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onRemovePhoto = {
                PhotoStore.deletePhoto(state.photoPath)
                onStateChange(state.copy(photoPath = null))
            },
            onReset = onReset
        )
    }
}

@Composable
private fun PlainTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .fieldBox()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        if (value.isEmpty()) {
            Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
            cursorBrush = SolidColor(TextPrimary),
            keyboardOptions = keyboardOptions,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines
        )
    }
}

@Composable
private fun StepperField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    step: Float,
    modifier: Modifier = Modifier
) {
    var showManualDialog by remember { mutableStateOf(false) }

    fun adjust(delta: Float) {
        val current = value.toFloatOrNull() ?: 0f
        onValueChange(formatAmount((current + delta).coerceAtLeast(0f)))
    }

    Column(modifier = modifier.fillMaxWidth()) {
        FieldLabel(label)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StepperButton(icon = Icons.Filled.Remove, onClick = { adjust(-step) })
            Text(
                text = value.ifBlank { "—" },
                style = MaterialTheme.typography.titleMedium,
                color = if (value.isBlank()) TextSecondary else TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .clickable { showManualDialog = true }
            )
            StepperButton(icon = Icons.Filled.Add, onClick = { adjust(step) })
        }
    }

    if (showManualDialog) {
        ManualValueEntryDialog(
            title = label,
            initialValue = value,
            onConfirm = {
                onValueChange(formatAmount(it))
                showManualDialog = false
            },
            onDismiss = { showManualDialog = false }
        )
    }
}

@Composable
private fun StepperButton(icon: ImageVector, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(28.dp)
            .border(1.dp, CardBorder, CircleShape)
    ) {
        Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun ManualValueEntryDialog(
    title: String,
    initialValue: String,
    onConfirm: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }
    val parsed = text.toFloatOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            CompactField(
                value = text,
                onValueChange = { text = it.filter { c -> c.isDigit() || c == '.' } },
                label = title,
                placeholder = "",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        },
        confirmButton = {
            TextButton(enabled = parsed != null, onClick = { onConfirm(parsed!!) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
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

    Column(modifier = Modifier.fillMaxWidth()) {
        FieldLabel(stringResource(R.string.food_label))
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fieldBox()
                    .clickable { expanded = true }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = foodLabel.ifBlank { stringResource(R.string.food_placeholder) },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (foodLabel.isBlank()) TextSecondary else TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = TextSecondary
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
                                    formatAmount(preset.breadUnits)
                                )
                            )
                        },
                        onClick = {
                            onPresetSelected(preset)
                            expanded = false
                        }
                    )
                }
                if (mealPresets.isNotEmpty()) HorizontalDivider(color = CardBorder)
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.enter_manually)) },
                    onClick = {
                        expanded = false
                        showManualDialog = true
                    }
                )
            }
        }
    }

    if (showManualDialog) {
        ManualValueEntryDialog(
            title = stringResource(R.string.bread_units_label),
            initialValue = "",
            onConfirm = {
                onManualEntryConfirmed(it)
                showManualDialog = false
            },
            onDismiss = { showManualDialog = false }
        )
    }
}

@Composable
private fun ActionsRow(
    photoPath: String?,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    onReset: (() -> Unit)?
) {
    if (photoPath == null) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlineActionButton(
                icon = Icons.Filled.AddAPhoto,
                text = stringResource(R.string.add_photo_short),
                onClick = onPickPhoto,
                modifier = Modifier.weight(1f)
            )
            if (onReset != null) {
                OutlineActionButton(
                    icon = null,
                    text = stringResource(R.string.cancel),
                    onClick = onReset,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    } else {
        PhotoPreviewRow(photoPath = photoPath, onPickPhoto = onPickPhoto, onRemovePhoto = onRemovePhoto)
        if (onReset != null) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlineActionButton(
                icon = null,
                text = stringResource(R.string.cancel),
                onClick = onReset,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun OutlineActionButton(
    icon: ImageVector?,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fieldBox()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
    }
}

@Composable
private fun PhotoPreviewRow(
    photoPath: String,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fieldBox()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = File(photoPath),
            contentDescription = stringResource(R.string.entry_photo),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onPickPhoto)
        )
        Text(
            text = stringResource(R.string.entry_photo),
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onRemovePhoto, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.remove_photo), tint = TextSecondary)
        }
    }
}

@Composable
private fun DateTimeSection(
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
        DateTimeChip(
            icon = Icons.Filled.CalendarMonth,
            contentDescription = stringResource(R.string.entry_date_label),
            value = date.format(DateTimeFormatter.ofPattern("d MMM yyyy", locale)),
            onClick = onDateClick,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(20.dp)
                .background(CardBorder)
        )
        DateTimeChip(
            icon = Icons.Filled.Schedule,
            contentDescription = stringResource(R.string.entry_time_label),
            value = time.format(DateTimeFormatter.ofPattern("HH:mm")),
            onClick = onTimeClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DateTimeChip(
    icon: ImageVector,
    contentDescription: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = contentDescription, tint = TextSecondary, modifier = Modifier.size(16.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
            modifier = Modifier.card(RoundedCornerShape(24.dp))
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
                        Text(stringResource(R.string.cancel))
                    }
                    TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) {
                        Text(stringResource(R.string.ok))
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
            modifier = Modifier.card(RoundedCornerShape(24.dp))
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
                        Text(stringResource(R.string.cancel))
                    }
                    TextButton(onClick = {
                        val millis = state.selectedDateMillis
                        if (millis != null) {
                            onConfirm(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                        }
                        onDismiss()
                    }) {
                        Text(stringResource(R.string.ok))
                    }
                }
            }
        }
    }
}
