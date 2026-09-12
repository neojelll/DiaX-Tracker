package com.neojelll.diaxtracker.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.backup.BackupExporter
import com.neojelll.diaxtracker.ui.theme.GlucoIcons
import java.time.LocalDate
import kotlinx.coroutines.launch

@Composable
fun BackupExportRow(snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val successMessage = stringResource(R.string.backup_export_success)
    val failureMessage = stringResource(R.string.backup_export_failed)

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val success = BackupExporter.export(context, uri)
            snackbarHostState.showSnackbar(if (success) successMessage else failureMessage)
        }
    }

    DataSettingsRow(
        icon = GlucoIcons.DownloadTray,
        title = stringResource(R.string.export_records_title),
        subtitle = stringResource(R.string.export_records_subtitle),
        modifier = Modifier.clickable { exportLauncher.launch("diax-backup-${LocalDate.now()}.zip") }
    )
}
