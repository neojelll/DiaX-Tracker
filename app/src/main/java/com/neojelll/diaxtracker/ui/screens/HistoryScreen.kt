package com.neojelll.diaxtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.ui.theme.AppGradient
import com.neojelll.diaxtracker.ui.theme.DeepForest
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: DiaryViewModel
) {
    val entries by viewModel.entries.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("История записей", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppGradient)
                .padding(padding)
        ) {
            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Записей пока нет",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Добавьте первую запись",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(entries, key = { it.id }) { entry ->
                        DiaryEntryCard(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun DiaryEntryCard(entry: DiaryEntry) {
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = entry.createdAt.format(formatter),
                style = MaterialTheme.typography.labelMedium,
                color = DeepForest
            )

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                entry.bloodSugar?.let {
                    Column {
                        Text(
                            text = "Сахар",
                            style = MaterialTheme.typography.labelSmall,
                            color = DeepForest
                        )
                        Text(
                            text = "$it ммоль/л",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = sugarColor(it)
                        )
                    }
                }
                entry.insulinDose?.let {
                    Column {
                        Text(
                            text = "Инсулин",
                            style = MaterialTheme.typography.labelSmall,
                            color = DeepForest
                        )
                        Text(
                            text = "$it ед." + (entry.insulinType?.let { type -> " · ${type.label}" } ?: ""),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DeepForest
                        )
                    }
                }
            }

            if (entry.notes.isNotBlank()) {
                Column {
                    Text(
                        text = "Комментарий",
                        style = MaterialTheme.typography.labelSmall,
                        color = DeepForest
                    )
                    Text(
                        text = entry.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DeepForest
                    )
                }
            }
        }
    }
}

@Composable
private fun sugarColor(value: Float) = when {
    value < 3.9f -> MaterialTheme.colorScheme.error
    value > 10.0f -> MaterialTheme.colorScheme.error
    value > 7.8f -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.primary
}
