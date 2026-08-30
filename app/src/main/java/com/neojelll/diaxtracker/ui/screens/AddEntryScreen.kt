package com.neojelll.diaxtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.data.InsulinType
import com.neojelll.diaxtracker.ui.theme.AppGradient
import com.neojelll.diaxtracker.ui.theme.DeepForest
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val DividerColor = DeepForest.copy(alpha = 0.12f)

@Composable
private fun transparentFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(
    viewModel: DiaryViewModel
) {
    var bloodSugar by remember { mutableStateOf("") }
    var insulinDose by remember { mutableStateOf("") }
    var insulinType by remember { mutableStateOf<InsulinType?>(null) }
    var notes by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showSuccessSnackbar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showSuccessSnackbar) {
        if (showSuccessSnackbar) {
            snackbarHostState.showSnackbar("Запись сохранена!")
            showSuccessSnackbar = false
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            initialTime = selectedTime,
            onConfirm = {
                selectedTime = it
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Новая запись", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppGradient)
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    TimeRow(
                        time = selectedTime,
                        onClick = { showTimePicker = true }
                    )
                    HorizontalDivider(color = DividerColor)

                    TextField(
                        value = bloodSugar,
                        onValueChange = { bloodSugar = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Уровень сахара (ммоль/л)") },
                        placeholder = { Text("Например: 5.6") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = transparentFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    HorizontalDivider(color = DividerColor)

                    TextField(
                        value = insulinDose,
                        onValueChange = { insulinDose = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Доза инсулина (ед.)") },
                        placeholder = { Text("Например: 4") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = transparentFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    HorizontalDivider(color = DividerColor)

                    InsulinTypeDropdown(
                        selected = insulinType,
                        onSelected = { insulinType = it }
                    )
                    HorizontalDivider(color = DividerColor)

                    TextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Комментарий") },
                        placeholder = { Text("Дополнительный комментарий...") },
                        colors = transparentFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            }

            Button(
                onClick = {
                    viewModel.addEntry(
                        DiaryEntry(
                            bloodSugar = bloodSugar.toFloatOrNull(),
                            insulinDose = insulinDose.toFloatOrNull(),
                            insulinType = insulinType,
                            notes = notes.trim(),
                            createdAt = LocalDateTime.of(LocalDate.now(), selectedTime)
                        )
                    )
                    bloodSugar = ""
                    insulinDose = ""
                    insulinType = null
                    notes = ""
                    selectedTime = LocalTime.now()
                    showSuccessSnackbar = true
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = bloodSugar.isNotBlank() || insulinDose.isNotBlank()
            ) {
                Text("Сохранить запись")
            }
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
private fun InsulinTypeDropdown(
    selected: InsulinType?,
    onSelected: (InsulinType?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        TextField(
            value = selected?.label ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Тип инсулина") },
            placeholder = { Text("Не выбран") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = transparentFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            InsulinType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.label) },
                    onClick = {
                        onSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
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
