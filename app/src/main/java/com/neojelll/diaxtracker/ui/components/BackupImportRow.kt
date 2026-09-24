package com.neojelll.diaxtracker.ui.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.backup.BackupImporter
import kotlinx.coroutines.launch

@Composable
fun BackupImportRow(snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var pendingFileName by remember { mutableStateOf<String?>(null) }
    val invalidMessage = stringResource(R.string.backup_import_invalid_file)
    val failedMessage = stringResource(R.string.backup_import_failed)
    val successFormat = stringResource(R.string.backup_import_success)

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingUri = uri
            pendingFileName = queryDisplayName(context, uri)
        }
    }

    DataRow(
        iconPath = LucidePaths.Upload,
        title = stringResource(R.string.backup_import_title),
        subtitle = stringResource(R.string.backup_import_subtitle),
        onClick = { showSheet = true }
    )

    if (showSheet) {
        ImportSheet(
            pendingFileName = pendingFileName,
            onPickFile = { importLauncher.launch(arrayOf("application/zip")) },
            onImport = {
                pendingUri?.let { uri ->
                    showSheet = false
                    scope.launch {
                        val message = when (val result = BackupImporter.import(context, uri)) {
                            is BackupImporter.ImportResult.Success ->
                                successFormat.format(result.summary.entriesAdded, result.summary.duplicatesSkipped)
                            BackupImporter.ImportResult.InvalidFile -> invalidMessage
                            BackupImporter.ImportResult.Failure -> failedMessage
                        }
                        pendingUri = null
                        pendingFileName = null
                        snackbarHostState.showSnackbar(message)
                    }
                }
            },
            onDismiss = {
                showSheet = false
                pendingUri = null
                pendingFileName = null
            }
        )
    }
}

private fun queryDisplayName(context: Context, uri: Uri): String? = try {
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }
} catch (e: Exception) {
    null
}
