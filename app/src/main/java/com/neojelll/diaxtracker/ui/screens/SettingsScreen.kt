package com.neojelll.diaxtracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.ui.components.BackupExportRow
import com.neojelll.diaxtracker.ui.components.BackupImportRow
import com.neojelll.diaxtracker.ui.components.GlukoCard
import com.neojelll.diaxtracker.ui.components.GlukoDivider
import com.neojelll.diaxtracker.ui.components.GlukoSwitch
import com.neojelll.diaxtracker.ui.components.GlucoseRangeSection
import com.neojelll.diaxtracker.ui.components.InsulinDurationSection
import com.neojelll.diaxtracker.ui.components.LanguageSettingRow
import com.neojelll.diaxtracker.ui.components.SecondaryButton
import com.neojelll.diaxtracker.ui.theme.DangerRed
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoSpacing
import com.neojelll.diaxtracker.ui.theme.GlukoType
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel

@Composable
fun SettingsScreen(viewModel: DiaryViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val autoBackupEnabled by viewModel.autoBackupEnabled.collectAsState()
    var showDeleteAllConfirm by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = GlukoSpacing.screenHorizontal)
                .padding(top = 18.dp, bottom = GlukoSpacing.bottomInset)
        ) {
            Column(Modifier.padding(horizontal = 8.dp).padding(bottom = 14.dp)) {
                Text(stringResource(R.string.settings_title), style = GlukoType.ScreenTitle)
                Spacer(Modifier.height(5.dp))
                Text(stringResource(R.string.settings_subtitle), style = GlukoType.Label)
            }

            GlukoCard {
                Text(stringResource(R.string.language), style = GlukoType.Label)
                Spacer(Modifier.height(11.dp))
                LanguageSettingRow()
            }
            Spacer(Modifier.height(GlukoSpacing.cardGap))

            GlukoCard {
                GlucoseRangeSection(viewModel)
            }
            Spacer(Modifier.height(GlukoSpacing.cardGap))

            GlukoCard {
                InsulinDurationSection(viewModel)
            }
            Spacer(Modifier.height(GlukoSpacing.cardGap))

            GlukoCard {
                Text(stringResource(R.string.data_section_title), style = GlukoType.Label)
                Spacer(Modifier.height(12.dp))
                BackupExportRow(viewModel, snackbarHostState)
                GlukoDivider()
                BackupImportRow(snackbarHostState)
                GlukoDivider()
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.backup_toggle_title), style = GlukoType.Body)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            stringResource(if (autoBackupEnabled) R.string.backup_toggle_on else R.string.backup_toggle_off),
                            style = GlukoType.Hint
                        )
                    }
                    GlukoSwitch(autoBackupEnabled) { viewModel.setAutoBackupEnabled(!autoBackupEnabled) }
                }
            }
            Spacer(Modifier.height(GlukoSpacing.cardGap))

            GlukoCard {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.delete_all_title), style = GlukoType.Body)
                        Spacer(Modifier.height(2.dp))
                        Text(stringResource(R.string.delete_all_subtitle), style = GlukoType.Hint)
                    }
                    SecondaryButton(
                        stringResource(R.string.delete), textColor = GlukoColors.TextLabel,
                        onClick = { showDeleteAllConfirm = true }
                    )
                }
            }
        }
    }

    if (showDeleteAllConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteAllConfirm = false },
            title = { Text(stringResource(R.string.delete_all_confirm_title)) },
            text = { Text(stringResource(R.string.delete_all_confirm_text)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAllEntries()
                    showDeleteAllConfirm = false
                }) {
                    Text(stringResource(R.string.delete), color = DangerRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
