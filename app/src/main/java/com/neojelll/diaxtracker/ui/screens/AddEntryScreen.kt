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
import androidx.compose.ui.unit.dp
import com.neojelll.diaxtracker.ui.theme.OnGlass
import com.neojelll.diaxtracker.ui.theme.glassPanel
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel
import dev.chrisbanes.haze.HazeState
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(
    viewModel: DiaryViewModel,
    hazeState: HazeState
) {
    var bloodSugar by remember { mutableStateOf("") }
    var shortInsulinDose by remember { mutableStateOf("") }
    var longInsulinDose by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showSuccessSnackbar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val sensorAvailable by viewModel.sensorAvailable.collectAsState()

    LaunchedEffect(showSuccessSnackbar) {
        if (showSuccessSnackbar) {
            snackbarHostState.showSnackbar("Запись сохранена!")
            showSuccessSnackbar = false
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            hazeState = hazeState,
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
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!sensorAvailable) {
                SensorWarningBanner(hazeState = hazeState)
            }

            EntryFormCard(
                hazeState = hazeState,
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassPanel(hazeState, RoundedCornerShape(16.dp))
                    .clickable(
                        enabled = bloodSugar.isNotBlank() || shortInsulinDose.isNotBlank() || longInsulinDose.isNotBlank()
                    ) {
                        viewModel.addEntry(
                            bloodSugar = bloodSugar.toFloatOrNull(),
                            shortInsulinDose = shortInsulinDose.toFloatOrNull(),
                            longInsulinDose = longInsulinDose.toFloatOrNull(),
                            notes = notes.trim(),
                            createdAt = LocalDateTime.of(LocalDate.now(), selectedTime)
                        )
                        bloodSugar = ""
                        shortInsulinDose = ""
                        longInsulinDose = ""
                        notes = ""
                        selectedTime = LocalTime.now()
                        showSuccessSnackbar = true
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Сохранить запись",
                    color = OnGlass,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
