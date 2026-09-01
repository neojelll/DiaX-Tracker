package com.neojelll.diaxtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.neojelll.diaxtracker.ui.theme.AppGradient
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntryScreen(
    viewModel: DiaryViewModel,
    entryId: Long,
    onDone: () -> Unit
) {
    val entries by viewModel.entries.collectAsState()
    val entry = entries.find { it.id == entryId } ?: return

    var bloodSugar by remember(entryId) { mutableStateOf(entry.bloodSugar?.toString() ?: "") }
    var shortInsulinDose by remember(entryId) { mutableStateOf(entry.shortInsulinDose?.toString() ?: "") }
    var longInsulinDose by remember(entryId) { mutableStateOf(entry.longInsulinDose?.toString() ?: "") }
    var notes by remember(entryId) { mutableStateOf(entry.notes) }
    var selectedTime by remember(entryId) { mutableStateOf(entry.createdAt.toLocalTime()) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

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

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Удалить запись?") },
            text = { Text("Это действие нельзя отменить.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteEntry(entry)
                    showDeleteConfirm = false
                    onDone()
                }) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Редактирование", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Удалить", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
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
            EntryFormCard(
                selectedTime = selectedTime,
                onTimeClick = { showTimePicker = true },
                bloodSugar = bloodSugar,
                onBloodSugarChange = { bloodSugar = it },
                shortInsulinDose = shortInsulinDose,
                onShortInsulinDoseChange = { shortInsulinDose = it },
                longInsulinDose = longInsulinDose,
                onLongInsulinDoseChange = { longInsulinDose = it },
                notes = notes,
                onNotesChange = { notes = it }
            )

            Button(
                onClick = {
                    viewModel.updateEntry(
                        entry.copy(
                            bloodSugar = bloodSugar.toFloatOrNull(),
                            shortInsulinDose = shortInsulinDose.toFloatOrNull(),
                            longInsulinDose = longInsulinDose.toFloatOrNull(),
                            notes = notes.trim(),
                            createdAt = LocalDateTime.of(entry.createdAt.toLocalDate(), selectedTime)
                        )
                    )
                    onDone()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = bloodSugar.isNotBlank() || shortInsulinDose.isNotBlank() || longInsulinDose.isNotBlank()
            ) {
                Text("Сохранить изменения")
            }
        }
    }
}
