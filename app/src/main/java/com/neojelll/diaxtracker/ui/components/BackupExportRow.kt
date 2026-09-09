package com.neojelll.diaxtracker.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.backup.BackupExporter
import com.neojelll.diaxtracker.ui.theme.TextPrimary
import com.neojelll.diaxtracker.ui.theme.card
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .card()
            .clickable { exportLauncher.launch("diax-backup-${LocalDate.now()}.zip") }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.backup_export_title),
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary
        )
        Icon(imageVector = Icons.Filled.Backup, contentDescription = null, tint = TextPrimary)
    }
}
