package com.neojelll.diaxtracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoRadius
import com.neojelll.diaxtracker.ui.theme.GlukoType
import com.neojelll.diaxtracker.ui.theme.tabular
import java.time.LocalDate
import java.time.LocalDateTime

/** How far back an export reaches; same windows as the History date filters. */
enum class ExportPeriod(val labelRes: Int, val fileTag: String) {
    Week(R.string.export_period_week, "week"),
    Month(R.string.export_period_month, "month"),
    All(R.string.export_period_all, "all")
}

/** Start of the period counted from [today] (its first day at 00:00), or null for everything. */
fun ExportPeriod.startsAt(today: LocalDate): LocalDateTime? = when (this) {
    ExportPeriod.Week -> today.minusDays(6).atStartOfDay()
    ExportPeriod.Month -> today.minusDays(29).atStartOfDay()
    ExportPeriod.All -> null
}

/** Parameter sheet shown before handing off to the system "create document" picker. */
@Composable
fun ExportSheet(
    period: ExportPeriod,
    recordsCount: Int,
    photosCount: Int,
    presetsCount: Int,
    onPeriod: (ExportPeriod) -> Unit,
    onExport: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        GlukoSheet(onDismiss) {
            SheetHeader(
                stringResource(R.string.backup_export_sheet_title),
                stringResource(R.string.backup_export_sheet_subtitle),
                onDismiss
            )

            Kicker(stringResource(R.string.backup_export_period_label))
            Spacer(Modifier.height(9.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExportPeriod.entries.forEach { p ->
                    ChoiceTile(Modifier.weight(1f), stringResource(p.labelRes), p == period) { onPeriod(p) }
                }
            }

            Spacer(Modifier.height(16.dp))
            Kicker(stringResource(R.string.backup_export_contents_label))
            Spacer(Modifier.height(9.dp))
            GlukoTile(padding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)) {
                ArchiveRow(stringResource(R.string.backup_export_contents_records), recordsCount)
                GlukoDivider(color = GlukoColors.Border)
                ArchiveRow(stringResource(R.string.backup_export_contents_photos), photosCount)
                GlukoDivider(color = GlukoColors.Border)
                ArchiveRow(stringResource(R.string.backup_export_contents_presets), presetsCount)
            }

            Spacer(Modifier.height(18.dp))
            PrimaryButton(stringResource(R.string.backup_export_confirm_action), onClick = onExport)
        }
    }
}

@Composable
private fun ArchiveRow(name: String, count: Int) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, style = GlukoType.Note.copy(color = GlukoColors.TextSecondary), modifier = Modifier.weight(1f))
        Text(stringResource(R.string.backup_export_contents_count, count), style = GlukoType.Hint.tabular)
    }
}

/** Parameter sheet shown before handing off to the system "open document" picker. */
@Composable
fun ImportSheet(
    pendingFileName: String?,
    archiveSummary: String?,
    onPickFile: () -> Unit,
    onImport: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        GlukoSheet(onDismiss) {
            SheetHeader(
                stringResource(R.string.backup_import_sheet_title),
                stringResource(R.string.backup_import_sheet_subtitle),
                onDismiss
            )

            Column(
                Modifier
                    .fillMaxWidth()
                    .dashedBorder(radius = GlukoRadius.panel)
                    .plainClickable(onClick = onPickFile)
                    .padding(horizontal = 16.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LucideIcon(LucidePaths.File, 26.dp, GlukoColors.TextLabel, strokeWidth = 1.6f)
                Spacer(Modifier.height(9.dp))
                Text(pendingFileName ?: stringResource(R.string.backup_import_pick_file), style = GlukoType.Body)
                if (archiveSummary != null) {
                    Spacer(Modifier.height(5.dp))
                    Text(archiveSummary, style = GlukoType.Hint.tabular, textAlign = TextAlign.Center)
                }
                if (pendingFileName == null) {
                    Spacer(Modifier.height(9.dp))
                    Text(
                        stringResource(R.string.backup_import_pick_hint),
                        style = GlukoType.Hint,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (pendingFileName != null) {
                Spacer(Modifier.height(11.dp))
                GlukoTile(padding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
                    Text(stringResource(R.string.backup_import_confirm_text), style = GlukoType.Note.copy(color = GlukoColors.TextSecondary))
                }
                Spacer(Modifier.height(14.dp))
                PrimaryButton(stringResource(R.string.backup_import_confirm_action), onClick = onImport)
            }
        }
    }
}
