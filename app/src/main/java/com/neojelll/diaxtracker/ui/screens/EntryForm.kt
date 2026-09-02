package com.neojelll.diaxtracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.ui.theme.OnGlass
import com.neojelll.diaxtracker.ui.theme.OnGlassMuted
import com.neojelll.diaxtracker.ui.theme.SproutGreen
import com.neojelll.diaxtracker.ui.theme.glassPanel
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
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

@Composable
internal fun SensorWarningBanner(hazeState: HazeState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassPanel(hazeState, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        Text(
            text = "Нет свежих данных с датчика — сахар нужно ввести вручную",
            style = MaterialTheme.typography.bodyMedium,
            color = OnGlass
        )
    }
}

@Composable
internal fun InsulinActiveBanner(hazeState: HazeState, entry: DiaryEntry) {
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
    val timeText = if (hours > 0) "$hours ч $minutes мин" else "$minutes мин"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassPanel(hazeState, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Filled.Bolt, contentDescription = null, tint = SproutGreen)
        Text(
            text = "Короткий инсулин ещё действует — осталось $timeText",
            style = MaterialTheme.typography.bodyMedium,
            color = OnGlass
        )
    }
}

@Composable
internal fun EntryFormCard(
    hazeState: HazeState,
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassPanel(hazeState, RoundedCornerShape(24.dp))
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
                color = OnGlassMuted
            )
            Text(
                text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                style = MaterialTheme.typography.headlineSmall,
                color = OnGlass
            )
        }
        Icon(Icons.Filled.Schedule, contentDescription = null, tint = OnGlass)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TimePickerDialog(
    hazeState: HazeState,
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
            modifier = Modifier.glassPanel(hazeState, RoundedCornerShape(24.dp))
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
                        Text("Отмена", color = OnGlass)
                    }
                    TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) {
                        Text("ОК", color = OnGlass)
                    }
                }
            }
        }
    }
}
