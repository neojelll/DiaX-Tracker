package com.neojelll.diaxtracker.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.backup.BackupExporter
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel
import java.time.LocalDate
import kotlinx.coroutines.launch

@Composable
fun BackupExportRow(viewModel: DiaryViewModel, snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val successMessage = stringResource(R.string.backup_export_success)
    val failureMessage = stringResource(R.string.backup_export_failed)
    val entries by viewModel.entries.collectAsState()
    val mealPresets by viewModel.mealPresets.collectAsState()

    var showSheet by remember { mutableStateOf(false) }
    var period by remember { mutableStateOf(ExportPeriod.All) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val success = BackupExporter.export(context, uri, period.startsAt(LocalDate.now()))
            snackbarHostState.showSnackbar(if (success) successMessage else failureMessage)
        }
    }

    DataRow(
        iconPath = LucidePaths.Download,
        title = stringResource(R.string.backup_export_title),
        subtitle = stringResource(R.string.backup_export_subtitle),
        onClick = { showSheet = true }
    )

    if (showSheet) {
        val from = period.startsAt(LocalDate.now())
        val inPeriod = entries.filter { from == null || it.createdAt >= from }
        ExportSheet(
            period = period,
            recordsCount = inPeriod.size,
            photosCount = inPeriod.count { it.photoPath != null },
            presetsCount = mealPresets.size,
            onPeriod = { period = it },
            onExport = {
                showSheet = false
                exportLauncher.launch("diax-backup-${LocalDate.now()}.zip")
            },
            onDismiss = { showSheet = false }
        )
    }
}
