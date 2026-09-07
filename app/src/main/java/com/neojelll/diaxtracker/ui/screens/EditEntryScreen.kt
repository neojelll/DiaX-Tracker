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
import com.neojelll.diaxtracker.data.DiaryEntryProduct
import com.neojelll.diaxtracker.data.SugarSource
import com.neojelll.diaxtracker.ui.components.CollapsibleTopBar
import com.neojelll.diaxtracker.ui.components.rememberCollapsibleTopBarState
import com.neojelll.diaxtracker.ui.theme.AccentDark
import com.neojelll.diaxtracker.ui.theme.DangerRed
import com.neojelll.diaxtracker.ui.theme.TextPrimary
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel
import kotlinx.coroutines.launch
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

    val initialBloodSugarText = remember(entryId) {
        entry.bloodSugar?.let { String.format(Locale.US, "%.1f", it) } ?: ""
    }
    var formState by remember(entryId) {
        mutableStateOf(
            EntryFormState(
                date = entry.createdAt.toLocalDate(),
                time = entry.createdAt.toLocalTime(),
                bloodSugar = initialBloodSugarText,
                breadUnits = entry.breadUnits?.let { formatAmount(it) } ?: "",
                foodLabel = entry.mealLabel ?: entry.breadUnits?.let {
                    String.format(breadUnitsValueFormat, formatAmount(it))
                } ?: "",
                mealLabel = entry.mealLabel,
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

    LaunchedEffect(entryId) {
        if (entry.mealLabel != null) {
            val products = viewModel.getEntryProducts(entryId)
            if (products.isNotEmpty()) {
                formState = formState.copy(
                    mealProducts = products.sortedBy { it.sortOrder }.map {
                        MealProductEntry(name = it.name, breadUnits = formatAmount(it.breadUnits))
                    }
                )
            }
        }
    }

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
    val canScroll = scrollState.maxValue > 0 || !topBarState.isFullyExpanded
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .then(
                    if (canScroll) Modifier.nestedScroll(topBarState.nestedScrollConnection) else Modifier
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
                    .weight(1f)
                    .fillMaxWidth()
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
                                    val newCreatedAt = LocalDateTime.of(formState.date, formState.time)
                                    val typedBloodSugar = formState.bloodSugar.toFloatOrNull()
                                    val sugarFieldUntouched = formState.bloodSugar == initialBloodSugarText
                                    val timeChanged = newCreatedAt != entry.createdAt

                                    coroutineScope.launch {
                                        val newBloodSugar: Float?
                                        val newSugarSource: SugarSource?
                                        if (entry.sugarSource == SugarSource.SENSOR && sugarFieldUntouched && timeChanged) {
                                            val resynced = viewModel.sensorReadingNear(newCreatedAt)
                                            newBloodSugar = resynced
                                            newSugarSource = resynced?.let { SugarSource.SENSOR }
                                        } else if (sugarFieldUntouched) {
                                            newBloodSugar = typedBloodSugar
                                            newSugarSource = entry.sugarSource
                                        } else {
                                            newBloodSugar = typedBloodSugar
                                            newSugarSource = typedBloodSugar?.let { SugarSource.MANUAL }
                                        }

                                        viewModel.updateEntry(
                                            entry.copy(
                                                bloodSugar = newBloodSugar,
                                                sugarSource = newSugarSource,
                                                breadUnits = formState.breadUnits.toFloatOrNull(),
                                                mealLabel = formState.mealLabel,
                                                shortInsulinDose = formState.shortInsulinDose.toFloatOrNull(),
                                                longInsulinDose = formState.longInsulinDose.toFloatOrNull(),
                                                notes = formState.notes.trim(),
                                                photoPath = formState.photoPath,
                                                createdAt = newCreatedAt
                                            ),
                                            formState.mealProducts.mapIndexed { index, product ->
                                                DiaryEntryProduct(
                                                    diaryEntryId = entry.id,
                                                    name = product.name,
                                                    breadUnits = product.breadUnits.toFloatOrNull() ?: 0f,
                                                    sortOrder = index
                                                )
                                            }
                                        )
                                        onDone()
                                    }
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
            }
        }
    }
}
