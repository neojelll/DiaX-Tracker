package com.neojelll.diaxtracker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.neojelll.diaxtracker.backup.BackupFolder
import com.neojelll.diaxtracker.ui.components.rememberAppToasts
import com.neojelll.diaxtracker.data.AutoBackupState
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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
private fun autoBackupStatus(state: AutoBackupState): String = when {
    state.lastAttemptFailed -> stringResource(R.string.backup_toggle_failed)
    state.lastSuccessMillis == 0L -> stringResource(R.string.backup_toggle_never)
    else -> stringResource(
        R.string.backup_toggle_last,
        Instant.ofEpochMilli(state.lastSuccessMillis).atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("d MMM, HH:mm"))
    )
}

@Composable
fun SettingsScreen(viewModel: DiaryViewModel) {
    val toasts = rememberAppToasts()
    val autoBackup by viewModel.autoBackup.collectAsState()
    val folderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null && viewModel.enableAutoBackup(uri)) toasts.backup(true, BackupFolder.label(uri))
    }
    var showDeleteAllConfirm by remember { mutableStateOf(false) }

    Scaffold(
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
                BackupExportRow(viewModel)
                GlukoDivider()
                BackupImportRow()
                GlukoDivider()
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.backup_toggle_title), style = GlukoType.Body)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            if (autoBackup.enabled) {
                                stringResource(R.string.backup_toggle_on, autoBackup.folderUri?.let { BackupFolder.label(Uri.parse(it)) }.orEmpty())
                            } else {
                                stringResource(R.string.backup_toggle_off)
                            },
                            style = GlukoType.Hint
                        )
                        if (autoBackup.enabled) {
                            Text(autoBackupStatus(autoBackup), style = GlukoType.Hint)
                        }
                    }
                    GlukoSwitch(autoBackup.enabled) {
                        // Enabling always goes through the folder picker (it opens on the folder chosen
                        // before, so keeping it is one tap); disabling just stops the schedule.
                        if (autoBackup.enabled) {
                            viewModel.disableAutoBackup()
                            toasts.backup(false)
                        } else {
                            folderLauncher.launch(null)
                        }
                    }
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
                    viewModel.deleteAllEntries { undo -> toasts.allDeleted(undo) }
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
