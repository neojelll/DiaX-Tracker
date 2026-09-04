package com.neojelll.diaxtracker.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.SugarSource
import com.neojelll.diaxtracker.ui.components.CollapsibleTopBar
import com.neojelll.diaxtracker.ui.components.rememberCollapsibleTopBarState
import com.neojelll.diaxtracker.ui.theme.AccentDark
import com.neojelll.diaxtracker.ui.theme.DangerRed
import com.neojelll.diaxtracker.ui.theme.TextPrimary
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
    val mealPresets by viewModel.mealPresets.collectAsState()
    val breadUnitsValueFormat = stringResource(R.string.bread_units_value_format)

    var formState by remember(entryId) {
        mutableStateOf(
            EntryFormState(
                date = entry.createdAt.toLocalDate(),
                time = entry.createdAt.toLocalTime(),
                bloodSugar = entry.bloodSugar?.let { String.format(Locale.US, "%.1f", it) } ?: "",
                breadUnits = entry.breadUnits?.let { formatAmount(it) } ?: "",
                foodLabel = entry.breadUnits?.let {
                    String.format(breadUnitsValueFormat, formatAmount(it))
                } ?: "",
                shortInsulinDose = entry.shortInsulinDose?.toString() ?: "",
                longInsulinDose = entry.longInsulinDose?.toString() ?: "",
                notes = entry.notes,
                photoPath = entry.photoPath
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
            title = { Text(stringResource(R.string.delete_entry_confirm_title)) },
            text = { Text(stringResource(R.string.delete_entry_confirm_text)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteEntry(entry)
                    showDeleteConfirm = false
                    onDone()
                }) {
                    Text(stringResource(R.string.delete), color = DangerRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    val topBarState = rememberCollapsibleTopBarState()
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .then(
                    if (scrollState.maxValue > 0) Modifier.nestedScroll(topBarState.nestedScrollConnection) else Modifier
                )
        ) {
            CollapsibleTopBar(
                state = topBarState,
                title = { Text(stringResource(R.string.edit_entry_title), color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete), tint = TextPrimary)
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EntryFormCard(
                    state = formState,
                    onStateChange = { formState = it },
                    onDateClick = { showDatePicker = true },
                    onTimeClick = { showTimePicker = true },
                    mealPresets = mealPresets
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (formState.isFillable) AccentDark else AccentDark.copy(alpha = 0.4f))
                        .then(
                            if (formState.isFillable) {
                                Modifier.clickable {
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
                                            photoPath = formState.photoPath,
                                            createdAt = LocalDateTime.of(formState.date, formState.time)
                                        )
                                    )
                                    onDone()
                                }
                            } else Modifier
                        )
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.save_changes_button),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
