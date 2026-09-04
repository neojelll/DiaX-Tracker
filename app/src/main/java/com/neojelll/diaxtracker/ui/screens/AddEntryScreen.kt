package com.neojelll.diaxtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.ui.components.CollapsibleTopBar
import com.neojelll.diaxtracker.ui.components.rememberCollapsibleTopBarState
import com.neojelll.diaxtracker.ui.theme.AccentDark
import com.neojelll.diaxtracker.ui.theme.TextPrimary
import com.neojelll.diaxtracker.ui.theme.TextSecondary
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(
    viewModel: DiaryViewModel
) {
    var formState by remember { mutableStateOf(EntryFormState()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showSuccessSnackbar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val sensorAvailable by viewModel.sensorAvailable.collectAsState()
    val mealPresets by viewModel.mealPresets.collectAsState()
    val entries by viewModel.entries.collectAsState()
    val entrySavedMessage = stringResource(R.string.entry_saved_snackbar)

    val todayCount = remember(entries) {
        val today = LocalDate.now()
        entries.count { it.createdAt.toLocalDate() == today }
    }

    LaunchedEffect(showSuccessSnackbar) {
        if (showSuccessSnackbar) {
            snackbarHostState.showSnackbar(entrySavedMessage)
            showSuccessSnackbar = false
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

    val topBarState = rememberCollapsibleTopBarState(contentHeight = 68.dp)
    val scrollState = rememberScrollState()
    var canScroll by remember { mutableStateOf(false) }
    LaunchedEffect(scrollState.maxValue) {
        if (scrollState.maxValue > 0) canScroll = true
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                title = {
                    Column {
                        Text(
                            stringResource(R.string.greeting_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary
                        )
                        Text(
                            pluralStringResource(R.plurals.today_entries_count, todayCount, todayCount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
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
                if (!sensorAvailable) {
                    SensorWarningBanner()
                }

                EntryFormCard(
                    state = formState,
                    onStateChange = { formState = it },
                    onDateClick = { showDatePicker = true },
                    onTimeClick = { showTimePicker = true },
                    mealPresets = mealPresets,
                    cardTitle = stringResource(R.string.add_entry_title),
                    onReset = { formState = EntryFormState() }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (formState.isFillable) AccentDark else AccentDark.copy(alpha = 0.4f))
                        .then(
                            if (formState.isFillable) {
                                Modifier.clickable {
                                    viewModel.addEntry(
                                        bloodSugar = formState.bloodSugar.toFloatOrNull(),
                                        breadUnits = formState.breadUnits.toFloatOrNull(),
                                        shortInsulinDose = formState.shortInsulinDose.toFloatOrNull(),
                                        longInsulinDose = formState.longInsulinDose.toFloatOrNull(),
                                        notes = formState.notes.trim(),
                                        photoPath = formState.photoPath,
                                        createdAt = LocalDateTime.of(formState.date, formState.time)
                                    )
                                    formState = EntryFormState()
                                    showSuccessSnackbar = true
                                }
                            } else Modifier
                        )
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.save_entry_button),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
