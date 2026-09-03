package com.neojelll.diaxtracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.SugarSource
import com.neojelll.diaxtracker.ui.components.LanguageMenu
import com.neojelll.diaxtracker.ui.theme.OnGlass
import com.neojelll.diaxtracker.ui.theme.glassPanel
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel
import java.time.LocalDateTime
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntryScreen(
    viewModel: DiaryViewModel,
    entryId: Long,
    onDone: () -> Unit
) {
    val entries by viewModel.entries.collectAsState()
    val entry = entries.find { it.id == entryId } ?: return

    var bloodSugar by remember(entryId) {
        mutableStateOf(entry.bloodSugar?.let { String.format(Locale.US, "%.1f", it) } ?: "")
    }
    var breadUnits by remember(entryId) { mutableStateOf(entry.breadUnits?.toString() ?: "") }
    var shortInsulinDose by remember(entryId) { mutableStateOf(entry.shortInsulinDose?.toString() ?: "") }
    var longInsulinDose by remember(entryId) { mutableStateOf(entry.longInsulinDose?.toString() ?: "") }
    var notes by remember(entryId) { mutableStateOf(entry.notes) }
    var selectedDate by remember(entryId) { mutableStateOf(entry.createdAt.toLocalDate()) }
    var selectedTime by remember(entryId) { mutableStateOf(entry.createdAt.toLocalTime()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog(
            initialDate = selectedDate,
            onConfirm = {
                selectedDate = it
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
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

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Color.Black.copy(alpha = 0.75f),
            titleContentColor = OnGlass,
            textContentColor = OnGlass,
            title = { Text(stringResource(R.string.delete_entry_confirm_title)) },
            text = { Text(stringResource(R.string.delete_entry_confirm_text)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteEntry(entry)
                    showDeleteConfirm = false
                    onDone()
                }) {
                    Text(stringResource(R.string.delete), color = OnGlass)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel), color = OnGlass)
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_entry_title), color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete), tint = Color.White)
                    }
                    LanguageMenu()
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
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EntryFormCard(
                selectedDate = selectedDate,
                onDateClick = { showDatePicker = true },
                selectedTime = selectedTime,
                onTimeClick = { showTimePicker = true },
                bloodSugar = bloodSugar,
                onBloodSugarChange = { bloodSugar = it },
                breadUnits = breadUnits,
                onBreadUnitsChange = { breadUnits = it },
                shortInsulinDose = shortInsulinDose,
                onShortInsulinDoseChange = { shortInsulinDose = it },
                longInsulinDose = longInsulinDose,
                onLongInsulinDoseChange = { longInsulinDose = it },
                notes = notes,
                onNotesChange = { notes = it }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassPanel(RoundedCornerShape(16.dp))
                    .clickable(
                        enabled = bloodSugar.isNotBlank() || breadUnits.isNotBlank() || shortInsulinDose.isNotBlank() || longInsulinDose.isNotBlank()
                    ) {
                        val newBloodSugar = bloodSugar.toFloatOrNull()
                        viewModel.updateEntry(
                            entry.copy(
                                bloodSugar = newBloodSugar,
                                sugarSource = if (newBloodSugar != entry.bloodSugar) {
                                    newBloodSugar?.let { SugarSource.MANUAL }
                                } else {
                                    entry.sugarSource
                                },
                                breadUnits = breadUnits.toFloatOrNull(),
                                shortInsulinDose = shortInsulinDose.toFloatOrNull(),
                                longInsulinDose = longInsulinDose.toFloatOrNull(),
                                notes = notes.trim(),
                                createdAt = LocalDateTime.of(selectedDate, selectedTime)
                            )
                        )
                        onDone()
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.save_changes_button),
                    color = OnGlass,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}
