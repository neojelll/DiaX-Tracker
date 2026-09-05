package com.neojelll.diaxtracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.MealPresetProduct
import com.neojelll.diaxtracker.data.MealPresetWithProducts
import com.neojelll.diaxtracker.ui.components.CollapsibleTopBar
import com.neojelll.diaxtracker.ui.components.rememberCollapsibleTopBarState
import com.neojelll.diaxtracker.ui.theme.CardBorder
import com.neojelll.diaxtracker.ui.theme.DangerRed
import com.neojelll.diaxtracker.ui.theme.TextPrimary
import com.neojelll.diaxtracker.ui.theme.TextSecondary
import com.neojelll.diaxtracker.ui.theme.card
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel

private const val MAX_PRESET_PRODUCTS = 10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPresetsScreen(viewModel: DiaryViewModel) {
    val mealPresets by viewModel.mealPresets.collectAsState()
    val topBarState = rememberCollapsibleTopBarState()
    val listState = rememberLazyListState()
    val canScroll = listState.canScrollForward || listState.canScrollBackward || !topBarState.isFullyExpanded
    var editingPreset by remember { mutableStateOf<MealPresetWithProducts?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var presetPendingDelete by remember { mutableStateOf<MealPresetWithProducts?>(null) }

    if (showAddDialog) {
        MealPresetEditorDialog(
            preset = null,
            onConfirm = { name, comment, products ->
                viewModel.saveMealPreset(id = 0, name = name, comment = comment, products = products)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    editingPreset?.let { preset ->
        MealPresetEditorDialog(
            preset = preset,
            onConfirm = { name, comment, products ->
                viewModel.saveMealPreset(id = preset.preset.id, name = name, comment = comment, products = products)
                editingPreset = null
            },
            onDelete = {
                editingPreset = null
                presetPendingDelete = preset
            },
            onDismiss = { editingPreset = null }
        )
    }

    presetPendingDelete?.let { preset ->
        AlertDialog(
            onDismissRequest = { presetPendingDelete = null },
            title = { Text(stringResource(R.string.delete_meal_preset_confirm_title)) },
            text = { Text(stringResource(R.string.delete_entry_confirm_text)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteMealPreset(preset.preset)
                    presetPendingDelete = null
                }) {
                    Text(stringResource(R.string.delete), color = DangerRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { presetPendingDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (canScroll) Modifier.nestedScroll(topBarState.nestedScrollConnection) else Modifier
            )
    ) {
        CollapsibleTopBar(
            state = topBarState,
            title = { Text(stringResource(R.string.meal_presets_title), color = TextPrimary) },
            actions = {
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = stringResource(R.string.add_meal_preset),
                        tint = TextPrimary
                    )
                }
            }
        )

        Box(modifier = Modifier.weight(1f)) {
            if (mealPresets.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.no_meal_presets_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.no_meal_presets_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(mealPresets, key = { it.preset.id }) { presetWithProducts ->
                        MealPresetCard(
                            presetWithProducts = presetWithProducts,
                            onClick = { editingPreset = presetWithProducts }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MealPresetCard(presetWithProducts: MealPresetWithProducts, onClick: () -> Unit) {
    val preset = presetWithProducts.preset
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .card()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = preset.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(
                    R.string.bread_units_value_format,
                    formatAmount(presetWithProducts.totalBreadUnits)
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        presetWithProducts.products.sortedBy { it.sortOrder }.forEach { product ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.bread_units_value_format, formatAmount(product.breadUnits)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        if (preset.comment.isNotBlank()) {
            Text(
                text = preset.comment,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

private data class ProductDraft(val key: Long, val name: String, val amount: String)

@Composable
private fun MealPresetEditorDialog(
    preset: MealPresetWithProducts?,
    onConfirm: (name: String, comment: String, products: List<MealPresetProduct>) -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(preset?.preset?.name ?: "") }
    var comment by remember { mutableStateOf(preset?.preset?.comment ?: "") }
    var nextKey by remember { mutableStateOf(0L) }
    val products = remember {
        mutableStateListOf<ProductDraft>().apply {
            val initial = preset?.products?.sortedBy { it.sortOrder }
            if (initial.isNullOrEmpty()) {
                add(ProductDraft(key = nextKey++, name = "", amount = ""))
            } else {
                initial.forEach { add(ProductDraft(key = nextKey++, name = it.name, amount = formatAmount(it.breadUnits))) }
            }
        }
    }

    val total = products.sumOf { (it.amount.toFloatOrNull() ?: 0f).toDouble() }.toFloat()
    val isValid = name.isNotBlank() &&
        products.isNotEmpty() &&
        products.all { it.name.isNotBlank() && it.amount.toFloatOrNull() != null }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (preset == null) R.string.add_meal_preset else R.string.edit_meal_preset
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CompactField(
                    value = name,
                    onValueChange = { name = it },
                    label = stringResource(R.string.meal_preset_name_label),
                    placeholder = stringResource(R.string.meal_preset_name_placeholder)
                )

                products.forEachIndexed { index, draft ->
                    key(draft.key) {
                        ProductDraftRow(
                            name = draft.name,
                            amount = draft.amount,
                            onNameChange = { products[index] = draft.copy(name = it) },
                            onAmountChange = { text ->
                                products[index] = draft.copy(amount = text.filter { c -> c.isDigit() || c == '.' })
                            },
                            onRemove = if (products.size > 1) {
                                { products.removeAt(index) }
                            } else null
                        )
                    }
                }

                TextButton(
                    onClick = { products.add(ProductDraft(key = nextKey++, name = "", amount = "")) },
                    enabled = products.size < MAX_PRESET_PRODUCTS
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.add_product))
                }

                HorizontalDivider(color = CardBorder)

                Text(
                    text = stringResource(R.string.meal_preset_total_format, formatAmount(total)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                CompactField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = stringResource(R.string.comment_label),
                    placeholder = stringResource(R.string.comment_placeholder),
                    singleLine = false,
                    minLines = 1,
                    maxLines = 2
                )

                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = null, tint = DangerRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.delete), color = DangerRed)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid,
                onClick = {
                    onConfirm(
                        name.trim(),
                        comment.trim(),
                        products.mapIndexed { index, draft ->
                            MealPresetProduct(
                                mealPresetId = 0,
                                name = draft.name.trim(),
                                breadUnits = draft.amount.toFloat(),
                                sortOrder = index
                            )
                        }
                    )
                }
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

@Composable
private fun ProductDraftRow(
    name: String,
    amount: String,
    onNameChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onRemove: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompactField(
            value = name,
            onValueChange = onNameChange,
            label = stringResource(R.string.meal_preset_product_name_label),
            placeholder = stringResource(R.string.meal_preset_product_name_placeholder),
            modifier = Modifier.weight(1f)
        )
        CompactField(
            value = amount,
            onValueChange = onAmountChange,
            label = stringResource(R.string.bread_units_short_label),
            placeholder = stringResource(R.string.bread_units_placeholder),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.width(90.dp)
        )
        if (onRemove != null) {
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.remove_product),
                    tint = TextSecondary
                )
            }
        } else {
            Spacer(modifier = Modifier.width(48.dp))
        }
    }
}
