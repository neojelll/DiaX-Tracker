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

    var formState by remember(entryId) {
        mutableStateOf(
            EntryFormState(
                date = entry.createdAt.toLocalDate(),
                time = entry.createdAt.toLocalTime(),
                bloodSugar = entry.bloodSugar?.let { String.format(Locale.US, "%.1f", it) } ?: "",
                breadUnits = entry.breadUnits?.toString() ?: "",
                shortInsulinDose = entry.shortInsulinDose?.toString() ?: "",
                longInsulinDose = entry.longInsulinDose?.toString() ?: "",
                notes = entry.notes
            )
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog(
            initialDate = formState.date,
            onConfirm = {
                formState = formState.copy(date = it)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showTimePicker) {
        TimePickerDialog(
            initialTime = formState.time,
            onConfirm = {
                formState = formState.copy(time = it)
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EntryFormCard(
                state = formState,
                onStateChange = { formState = it },
                onDateClick = { showDatePicker = true },
                onTimeClick = { showTimePicker = true }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassPanel(RoundedCornerShape(16.dp))
                    .clickable(enabled = formState.isFillable) {
                        val newBloodSugar = formState.bloodSugar.toFloatOrNull()
                        viewModel.updateEntry(
                            entry.copy(
                                bloodSugar = newBloodSugar,
                                sugarSource = if (newBloodSugar != entry.bloodSugar) {
                                    newBloodSugar?.let { SugarSource.MANUAL }
                                } else {
                                    entry.sugarSource
                                },
                                breadUnits = formState.breadUnits.toFloatOrNull(),
                                shortInsulinDose = formState.shortInsulinDose.toFloatOrNull(),
                                longInsulinDose = formState.longInsulinDose.toFloatOrNull(),
                                notes = formState.notes.trim(),
                                createdAt = LocalDateTime.of(formState.date, formState.time)
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
