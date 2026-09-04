package com.neojelll.diaxtracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.ui.components.CollapsibleTopBar
import com.neojelll.diaxtracker.ui.components.LanguageMenu
import com.neojelll.diaxtracker.ui.components.rememberCollapsibleTopBarState
import com.neojelll.diaxtracker.ui.theme.OnGlass
import com.neojelll.diaxtracker.ui.theme.glassPanel
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel
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
    val entrySavedMessage = stringResource(R.string.entry_saved_snackbar)

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

    val topBarState = rememberCollapsibleTopBarState()

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .nestedScroll(topBarState.nestedScrollConnection)
        ) {
            CollapsibleTopBar(
                state = topBarState,
                title = { Text(stringResource(R.string.add_entry_title), color = Color.White) },
                actions = { LanguageMenu() }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!sensorAvailable) {
                    SensorWarningBanner()
                }

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
                        .glassPanel(RoundedCornerShape(16.dp))
                        .clickable(enabled = formState.isFillable) {
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
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.save_entry_button),
                        color = OnGlass,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(96.dp))
            }
        }
    }
}
