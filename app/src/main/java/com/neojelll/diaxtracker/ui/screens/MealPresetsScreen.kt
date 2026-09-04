package com.neojelll.diaxtracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.MealPreset
import com.neojelll.diaxtracker.ui.components.CollapsibleTopBar
import com.neojelll.diaxtracker.ui.components.LanguageMenu
import com.neojelll.diaxtracker.ui.components.rememberCollapsibleTopBarState
import com.neojelll.diaxtracker.ui.theme.DangerRed
import com.neojelll.diaxtracker.ui.theme.TextPrimary
import com.neojelll.diaxtracker.ui.theme.TextSecondary
import com.neojelll.diaxtracker.ui.theme.card
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPresetsScreen(viewModel: DiaryViewModel) {
    val mealPresets by viewModel.mealPresets.collectAsState()
    val topBarState = rememberCollapsibleTopBarState()
    val listState = rememberLazyListState()
    val canScroll = listState.canScrollForward || listState.canScrollBackward
    var editingPreset by remember { mutableStateOf<MealPreset?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var presetPendingDelete by remember { mutableStateOf<MealPreset?>(null) }

    if (showAddDialog) {
        MealPresetEditorDialog(
            preset = null,
            onConfirm = { name, breadUnits ->
                viewModel.addMealPreset(name, breadUnits)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    editingPreset?.let { preset ->
        MealPresetEditorDialog(
            preset = preset,
            onConfirm = { name, breadUnits ->
                viewModel.updateMealPreset(preset.copy(name = name, breadUnits = breadUnits))
                editingPreset = null
            },
            onDelete = {
                editingPreset = null
                presetPendingDelete = preset
            },
            onDismiss = { editingPreset = null }
        )
    }

    presetPendingDelete?.let { preset ->
        AlertDialog(
            onDismissRequest = { presetPendingDelete = null },
            title = { Text(stringResource(R.string.delete_meal_preset_confirm_title)) },
            text = { Text(stringResource(R.string.delete_entry_confirm_text)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteMealPreset(preset)
                    presetPendingDelete = null
                }) {
                    Text(stringResource(R.string.delete), color = DangerRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { presetPendingDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (canScroll) Modifier.nestedScroll(topBarState.nestedScrollConnection) else Modifier
            )
    ) {
        CollapsibleTopBar(
            state = topBarState,
            title = { Text(stringResource(R.string.meal_presets_title), color = TextPrimary) },
            actions = {
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = stringResource(R.string.add_meal_preset),
                        tint = TextPrimary
                    )
                }
                LanguageMenu()
            }
        )

        Box(modifier = Modifier.weight(1f)) {
            if (mealPresets.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.no_meal_presets_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.no_meal_presets_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(mealPresets, key = { it.id }) { preset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .card()
                                .clickable { editingPreset = preset }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary
                            )
                            Text(
                                text = stringResource(
                                    R.string.bread_units_value_format,
                                    formatAmount(preset.breadUnits)
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MealPresetEditorDialog(
    preset: MealPreset?,
    onConfirm: (name: String, breadUnits: Float) -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(preset?.name ?: "") }
    var breadUnitsText by remember { mutableStateOf(preset?.let { formatAmount(it.breadUnits) } ?: "") }
    val breadUnits = breadUnitsText.toFloatOrNull()
    val isValid = name.isNotBlank() && breadUnits != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (preset == null) R.string.add_meal_preset else R.string.edit_meal_preset
                )
            )
        },
        text = {
            Column {
                CompactField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(R.string.meal_preset_name_label),
                    placeholder = stringResource(R.string.meal_preset_name_placeholder)
                )
                CompactField(
                    value = breadUnitsText,
                    onValueChange = { breadUnitsText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = stringResource(R.string.bread_units_label),
                    placeholder = stringResource(R.string.bread_units_placeholder),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                if (onDelete != null) {
                    TextButton(onClick = onDelete, modifier = Modifier.padding(top = 8.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = null, tint = DangerRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.delete), color = DangerRed)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid,
                onClick = { onConfirm(name.trim(), breadUnits!!) }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
