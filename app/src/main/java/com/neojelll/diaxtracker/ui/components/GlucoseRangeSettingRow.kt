package com.neojelll.diaxtracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.ui.screens.CompactField
import com.neojelll.diaxtracker.ui.screens.formatAmount
import com.neojelll.diaxtracker.ui.theme.TextPrimary
import com.neojelll.diaxtracker.ui.theme.TextSecondary
import com.neojelll.diaxtracker.ui.theme.card
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel

private const val MIN_BOUND_MMOL = 2f
private const val MAX_BOUND_MMOL = 20f

@Composable
fun GlucoseRangeSettingRow(viewModel: DiaryViewModel) {
    val range by viewModel.glucoseRange.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .card()
            .clickable { showDialog = true }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.glucose_range_label),
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(
                R.string.glucose_range_value_format,
                formatAmount(range.low),
                formatAmount(range.high)
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }

    if (showDialog) {
        GlucoseRangeEditorDialog(
            initialLow = range.low,
            initialHigh = range.high,
            onConfirm = { low, high ->
                viewModel.setGlucoseRange(low, high)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun GlucoseRangeEditorDialog(
    initialLow: Float,
    initialHigh: Float,
    onConfirm: (low: Float, high: Float) -> Unit,
    onDismiss: () -> Unit
) {
    var lowText by remember { mutableStateOf(formatAmount(initialLow)) }
    var highText by remember { mutableStateOf(formatAmount(initialHigh)) }
    val low = lowText.toFloatOrNull()
    val high = highText.toFloatOrNull()
    val isValid = low != null && high != null &&
        low < high &&
        low >= MIN_BOUND_MMOL && high <= MAX_BOUND_MMOL

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.glucose_range_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CompactField(
                        value = lowText,
                        onValueChange = { lowText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = stringResource(R.string.glucose_range_low_label),
                        placeholder = "",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.width(120.dp)
                    )
                    CompactField(
                        value = highText,
                        onValueChange = { highText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = stringResource(R.string.glucose_range_high_label),
                        placeholder = "",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.width(120.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.glucose_range_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid,
                onClick = { onConfirm(low!!, high!!) }
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
