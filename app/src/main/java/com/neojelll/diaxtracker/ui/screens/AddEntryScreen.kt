package com.neojelll.diaxtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
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
import com.neojelll.diaxtracker.data.DiaryEntryProduct
import com.neojelll.diaxtracker.ui.components.CollapsibleTopBar
import com.neojelll.diaxtracker.ui.components.YesterdaySummaryCard
import com.neojelll.diaxtracker.ui.components.rememberCollapsibleTopBarState
import com.neojelll.diaxtracker.ui.theme.AccentGreen
import com.neojelll.diaxtracker.ui.theme.OnAccent
import com.neojelll.diaxtracker.ui.theme.TextPrimary
import com.neojelll.diaxtracker.ui.theme.TextSecondary
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

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

    val yesterdaySameTimeEntries = remember(entries) {
        val now = LocalDateTime.now()
        val from = now.minusHours(25)
        val to = now.minusHours(23)
        entries.filter { it.createdAt in from..to }.sortedBy { it.createdAt }
    }

    val greetingRes = remember {
        when (LocalTime.now().hour) {
            in 5..10 -> R.string.greeting_morning
            in 11..16 -> R.string.greeting_afternoon
            in 17..22 -> R.string.greeting_evening
            else -> R.string.greeting_night
        }
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

    val topBarState = rememberCollapsibleTopBarState(contentHeight = 44.dp)
    val scrollState = rememberScrollState()
    val canScroll = scrollState.maxValue > 0 || !topBarState.isFullyExpanded

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
                            stringResource(greetingRes),
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary
                        )
                        Text(
                            pluralStringResource(R.plurals.today_entries_count, todayCount, todayCount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                },
                actions = { GreetingBadge() }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(12.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                YesterdaySummaryCard(entries = yesterdaySameTimeEntries)

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
                        .height(50.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(if (formState.isFillable) AccentGreen else AccentGreen.copy(alpha = 0.4f))
                        .then(
                            if (formState.isFillable) {
                                Modifier.clickable {
                                    viewModel.addEntry(
                                        bloodSugar = formState.bloodSugar.toFloatOrNull(),
                                        breadUnits = formState.breadUnits.toFloatOrNull(),
                                        mealLabel = formState.mealLabel,
                                        mealProducts = formState.mealProducts.mapIndexed { index, product ->
                                            DiaryEntryProduct(
                                                diaryEntryId = 0,
                                                name = product.name,
                                                breadUnits = product.breadUnits.toFloatOrNull() ?: 0f,
                                                sortOrder = index
                                            )
                                        },
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
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.save_entry_button),
                        color = OnAccent,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun GreetingBadge() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(AccentGreen.copy(alpha = 0.12f))
            .border(1.dp, AccentGreen.copy(alpha = 0.28f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Schedule,
            contentDescription = null,
            tint = AccentGreen,
            modifier = Modifier.size(20.dp)
        )
    }
}
