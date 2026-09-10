package com.neojelll.diaxtracker.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.backup.BackupImporter
import com.neojelll.diaxtracker.ui.theme.DangerRed
import com.neojelll.diaxtracker.ui.theme.TextPrimary
import com.neojelll.diaxtracker.ui.theme.card
import kotlinx.coroutines.launch

@Composable
fun BackupImportRow(snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    val invalidMessage = stringResource(R.string.backup_import_invalid_file)
    val failedMessage = stringResource(R.string.backup_import_failed)

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) pendingUri = uri }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .card()
            .clickable { importLauncher.launch(arrayOf("application/zip")) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.backup_import_title),
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary
        )
        Icon(imageVector = Icons.Filled.Restore, contentDescription = null, tint = TextPrimary)
    }

    pendingUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingUri = null },
            title = { Text(stringResource(R.string.backup_import_confirm_title)) },
            text = { Text(stringResource(R.string.backup_import_confirm_text)) },
            confirmButton = {
                TextButton(onClick = {
                    pendingUri = null
                    scope.launch {
                        when (BackupImporter.import(context, uri)) {
                            BackupImporter.ImportResult.SUCCESS -> restartApp(context)
                            BackupImporter.ImportResult.INVALID_FILE ->
                                snackbarHostState.showSnackbar(invalidMessage)
                            BackupImporter.ImportResult.FAILURE ->
                                snackbarHostState.showSnackbar(failedMessage)
                        }
                    }
                }) {
                    Text(stringResource(R.string.backup_import_confirm_action), color = DangerRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingUri = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

private fun restartApp(context: Context) {
    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        ?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) }
    context.startActivity(intent)
    Runtime.getRuntime().exit(0)
}
