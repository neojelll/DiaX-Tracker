package com.neojelll.diaxtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.data.MealTime
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(
    viewModel: DiaryViewModel
) {
    var bloodSugar by remember { mutableStateOf("") }
    var insulinDose by remember { mutableStateOf("") }
    var mealDescription by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedMealTime by remember { mutableStateOf(MealTime.BEFORE_MEAL) }
    var showSuccessSnackbar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showSuccessSnackbar) {
        if (showSuccessSnackbar) {
            snackbarHostState.showSnackbar("Запись сохранена!")
            showSuccessSnackbar = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Новая запись") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Время измерения",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MealTime.entries.chunked(2).forEach { row ->
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { time ->
                            FilterChip(
                                selected = selectedMealTime == time,
                                onClick = { selectedMealTime = time },
                                label = { Text(time.label, style = MaterialTheme.typography.bodySmall) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = bloodSugar,
                onValueChange = { bloodSugar = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Уровень сахара (ммоль/л)") },
                placeholder = { Text("Например: 5.6") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = insulinDose,
                onValueChange = { insulinDose = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Доза инсулина (ед.)") },
                placeholder = { Text("Например: 4") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = mealDescription,
                onValueChange = { mealDescription = it },
                label = { Text("Что ели") },
                placeholder = { Text("Опишите приём пищи...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Заметки") },
                placeholder = { Text("Дополнительные заметки...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.addEntry(
                        DiaryEntry(
                            bloodSugar = bloodSugar.toFloatOrNull(),
                            insulinDose = insulinDose.toFloatOrNull(),
                            mealDescription = mealDescription.trim(),
                            notes = notes.trim(),
                            mealTime = selectedMealTime
                        )
                    )
                    bloodSugar = ""
                    insulinDose = ""
                    mealDescription = ""
                    notes = ""
                    selectedMealTime = MealTime.BEFORE_MEAL
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
