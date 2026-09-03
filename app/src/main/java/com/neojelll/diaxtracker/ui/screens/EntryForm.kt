package com.neojelll.diaxtracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.ui.theme.OnGlass
import com.neojelll.diaxtracker.ui.theme.OnGlassMuted
import com.neojelll.diaxtracker.ui.theme.SproutGreen
import com.neojelll.diaxtracker.ui.theme.glassPanel
import kotlinx.coroutines.delay
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
private fun CompactField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE
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
            colors = transparentFieldColors(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

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
internal fun InsulinActiveBanner(entry: DiaryEntry) {
    var now by remember(entry.id) { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(entry.id) {
        while (true) {
            now = LocalDateTime.now()
            delay(30_000L)
        }
    }

    val remaining = Duration.between(now, entry.createdAt.plusHours(4)).let {
        if (it.isNegative) Duration.ZERO else it
    }
    val totalMinutes = remaining.toMinutes()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    val timeText = if (hours > 0) {
        stringResource(R.string.duration_hours_minutes, hours, minutes)
    } else {
        stringResource(R.string.duration_minutes, minutes)
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
        Text(
            text = stringResource(R.string.insulin_active_banner, timeText),
            style = MaterialTheme.typography.bodyMedium,
            color = OnGlass
        )
    }
}

@Composable
internal fun EntryFormCard(
    selectedDate: LocalDate,
    onDateClick: () -> Unit,
    selectedTime: LocalTime,
    onTimeClick: () -> Unit,
    bloodSugar: String,
    onBloodSugarChange: (String) -> Unit,
    breadUnits: String,
    onBreadUnitsChange: (String) -> Unit,
    shortInsulinDose: String,
    onShortInsulinDoseChange: (String) -> Unit,
    longInsulinDose: String,
    onLongInsulinDoseChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassPanel(RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            DateRow(date = selectedDate, onClick = onDateClick)
            HorizontalDivider(color = DividerColor)

            TimeRow(time = selectedTime, onClick = onTimeClick)
            HorizontalDivider(color = DividerColor)

            CompactField(
                value = bloodSugar,
                onValueChange = { onBloodSugarChange(it.filter { c -> c.isDigit() || c == '.' }) },
                label = stringResource(R.string.blood_sugar_label),
                placeholder = stringResource(R.string.blood_sugar_placeholder),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            HorizontalDivider(color = DividerColor)

            CompactField(
                value = breadUnits,
                onValueChange = { onBreadUnitsChange(it.filter { c -> c.isDigit() || c == '.' }) },
                label = stringResource(R.string.bread_units_label),
                placeholder = stringResource(R.string.bread_units_placeholder),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            HorizontalDivider(color = DividerColor)

            CompactField(
                value = shortInsulinDose,
                onValueChange = { onShortInsulinDoseChange(it.filter { c -> c.isDigit() || c == '.' }) },
                label = stringResource(R.string.short_insulin_label),
                placeholder = stringResource(R.string.short_insulin_placeholder),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            HorizontalDivider(color = DividerColor)

            CompactField(
                value = longInsulinDose,
                onValueChange = { onLongInsulinDoseChange(it.filter { c -> c.isDigit() || c == '.' }) },
                label = stringResource(R.string.long_insulin_label),
                placeholder = stringResource(R.string.long_insulin_placeholder),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            HorizontalDivider(color = DividerColor)

            CompactField(
                value = notes,
                onValueChange = onNotesChange,
                label = stringResource(R.string.comment_label),
                placeholder = stringResource(R.string.comment_placeholder),
                singleLine = false,
                minLines = 1,
                maxLines = 3
            )
        }
    }
}

@Composable
private fun DateRow(
    date: LocalDate,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val locale = LocalConfiguration.current.locales[0]
        Column {
            Text(
                text = stringResource(R.string.entry_date_label),
                style = MaterialTheme.typography.labelMedium,
                color = OnGlassMuted
            )
            Text(
                text = date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", locale)),
                style = MaterialTheme.typography.titleLarge,
                color = OnGlass
            )
        }
        Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = OnGlass)
    }
}

@Composable
private fun TimeRow(
    time: LocalTime,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(R.string.entry_time_label),
                style = MaterialTheme.typography.labelMedium,
                color = OnGlassMuted
            )
            Text(
                text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                style = MaterialTheme.typography.titleLarge,
                color = OnGlass
            )
        }
        Icon(Icons.Filled.Schedule, contentDescription = null, tint = OnGlass)
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
