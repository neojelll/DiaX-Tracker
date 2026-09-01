package com.neojelll.diaxtracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.neojelll.diaxtracker.ui.theme.DeepForest
import java.time.LocalTime
import java.time.format.DateTimeFormatter

internal val DividerColor = DeepForest.copy(alpha = 0.12f)

@Composable
internal fun transparentFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent
)

@Composable
internal fun SensorWarningBanner() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Text(
                text = "Нет свежих данных с датчика — сахар нужно ввести вручную",
                style = MaterialTheme.typography.bodyMedium,
                color = DeepForest
            )
        }
    }
}

@Composable
internal fun EntryFormCard(
    selectedTime: LocalTime,
    onTimeClick: () -> Unit,
    bloodSugar: String,
    onBloodSugarChange: (String) -> Unit,
    shortInsulinDose: String,
    onShortInsulinDoseChange: (String) -> Unit,
    longInsulinDose: String,
    onLongInsulinDoseChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            TimeRow(time = selectedTime, onClick = onTimeClick)
            HorizontalDivider(color = DividerColor)

            TextField(
                value = bloodSugar,
                onValueChange = { onBloodSugarChange(it.filter { c -> c.isDigit() || c == '.' }) },
                label = { Text("Уровень сахара (ммоль/л)") },
                placeholder = { Text("Например: 5.6") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = transparentFieldColors(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            HorizontalDivider(color = DividerColor)

            TextField(
                value = shortInsulinDose,
                onValueChange = { onShortInsulinDoseChange(it.filter { c -> c.isDigit() || c == '.' }) },
                label = { Text("Короткий инсулин (ед.)") },
                placeholder = { Text("Например: 4") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = transparentFieldColors(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            HorizontalDivider(color = DividerColor)

            TextField(
                value = longInsulinDose,
                onValueChange = { onLongInsulinDoseChange(it.filter { c -> c.isDigit() || c == '.' }) },
                label = { Text("Длинный инсулин (ед.)") },
                placeholder = { Text("Например: 10") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = transparentFieldColors(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            HorizontalDivider(color = DividerColor)

            TextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("Комментарий") },
                placeholder = { Text("Дополнительный комментарий...") },
                colors = transparentFieldColors(),
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )
        }
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Время измерения",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                style = MaterialTheme.typography.headlineSmall,
                color = DeepForest
            )
        }
        Icon(Icons.Filled.Schedule, contentDescription = null, tint = DeepForest)
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
        Surface(shape = RoundedCornerShape(24.dp), color = Color.White) {
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
                        Text("Отмена")
                    }
                    TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) {
                        Text("ОК")
                    }
                }
            }
        }
    }
}
