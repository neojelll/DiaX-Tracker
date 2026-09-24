package com.neojelll.diaxtracker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.DiaryEntryProduct
import com.neojelll.diaxtracker.ui.components.GlukoSheet
import com.neojelll.diaxtracker.ui.components.Kicker
import com.neojelll.diaxtracker.ui.components.SheetHeader
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoType
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel
import java.time.format.DateTimeFormatter

/**
 * Read-only look at what an entry recorded for its meal: the preset name, total bread units and
 * the composition as it was when the entry was saved.
 *
 * An entry keeps its own copy (mealLabel plus DiaryEntryProduct rows) instead of linking to the
 * live preset, so this stays true even after the preset is renamed, changed or deleted - which is
 * also why there is nothing to edit here.
 */
@Composable
fun RecordCompositionSheet(viewModel: DiaryViewModel, entryId: Long, onDismiss: () -> Unit) {
    val entries by viewModel.entries.collectAsState()
    val entry = entries.find { it.id == entryId } ?: run { onDismiss(); return }
    var products by remember(entryId) { mutableStateOf<List<DiaryEntryProduct>?>(null) }
    LaunchedEffect(entryId) { products = viewModel.getEntryProducts(entryId) }

    val rows = products.orEmpty().sortedBy { it.sortOrder }.map { it.name to it.breadUnits }
    val totalBreadUnits = entry.breadUnits ?: rows.takeIf { it.isNotEmpty() }?.sumOf { it.second.toDouble() }?.toFloat()

    GlukoSheet(onDismiss, maxHeightFraction = 0.76f) {
        SheetHeader(
            title = entry.mealLabel ?: stringResource(R.string.record_no_preset),
            subtitle = entry.createdAt.format(DateTimeFormatter.ofPattern("d MMM, HH:mm")),
            onClose = onDismiss
        )

        if (totalBreadUnits != null) {
            TotalBreadUnitsTile(totalBreadUnits)
            Spacer(Modifier.height(16.dp))
        }

        Kicker(stringResource(R.string.record_composition_snapshot))
        Spacer(Modifier.height(4.dp))
        CompositionBody(rows = rows, loaded = products != null)
    }
}

@Composable
private fun ColumnScope.CompositionBody(rows: List<Pair<String, Float>>, loaded: Boolean) {
    if (!loaded) return
    if (rows.isEmpty()) {
        Text(
            stringResource(R.string.record_composition_empty),
            style = GlukoType.Note.copy(color = GlukoColors.TextLabel),
            modifier = Modifier.padding(vertical = 11.dp)
        )
    } else {
        Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
            CompositionRows(rows)
        }
    }
}
