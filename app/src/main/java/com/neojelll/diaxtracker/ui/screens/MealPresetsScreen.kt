package com.neojelll.diaxtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.MealPresetProduct
import com.neojelll.diaxtracker.data.MealPresetWithProducts
import com.neojelll.diaxtracker.ui.components.CircleButton
import com.neojelll.diaxtracker.ui.components.GlukoCard
import com.neojelll.diaxtracker.ui.components.GlukoDivider
import com.neojelll.diaxtracker.ui.components.GlukoField
import com.neojelll.diaxtracker.ui.components.GlukoSheet
import com.neojelll.diaxtracker.ui.components.GlukoTile
import com.neojelll.diaxtracker.ui.components.Kicker
import com.neojelll.diaxtracker.ui.components.LucideIcon
import com.neojelll.diaxtracker.ui.components.LucidePaths
import com.neojelll.diaxtracker.ui.components.OverlayController
import com.neojelll.diaxtracker.ui.components.PrimaryButton
import com.neojelll.diaxtracker.ui.components.SecondaryButton
import com.neojelll.diaxtracker.ui.components.SheetHeader
import com.neojelll.diaxtracker.ui.components.XeBadge
import com.neojelll.diaxtracker.ui.components.dashedBorder
import com.neojelll.diaxtracker.ui.components.numeric
import com.neojelll.diaxtracker.ui.components.plainClickable
import com.neojelll.diaxtracker.ui.theme.DangerRed
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoRadius
import com.neojelll.diaxtracker.ui.theme.GlukoSpacing
import com.neojelll.diaxtracker.ui.theme.GlukoType
import com.neojelll.diaxtracker.ui.theme.tabular
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel

private const val MAX_PRESET_PRODUCTS = 10

@Composable
fun MealPresetsScreen(viewModel: DiaryViewModel, overlays: OverlayController) {
    val mealPresets by viewModel.mealPresets.collectAsState()

    Box(Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(
                start = GlukoSpacing.screenHorizontal, end = GlukoSpacing.screenHorizontal,
                top = 18.dp, bottom = GlukoSpacing.bottomInset
            ),
            horizontalArrangement = Arrangement.spacedBy(GlukoSpacing.cardGap),
            verticalArrangement = Arrangement.spacedBy(GlukoSpacing.cardGap)
        ) {
            item(span = { GridItemSpan(2) }) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.meal_presets_title), style = GlukoType.ScreenTitle)
                        Spacer(Modifier.height(5.dp))
                        Text(
                            pluralStringResource(R.plurals.presets_count, mealPresets.size, mealPresets.size),
                            style = GlukoType.Label
                        )
                    }
                    NotificationButton(onClick = overlays::openNotifications)
                }
            }

            items(mealPresets, key = { it.preset.id }) { preset ->
                PresetCard(preset) { overlays.openPresetDetail(preset.preset.id) }
            }

            item(span = { GridItemSpan(2) }) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .dashedBorder(radius = GlukoRadius.record)
                        .plainClickable { overlays.openPresetEdit(null) }
                        .padding(15.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LucideIcon(LucidePaths.Plus, 15.dp, GlukoColors.TextSecondary, strokeWidth = 2f)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.meal_preset_add_dashed), style = GlukoType.Body.copy(color = GlukoColors.TextSecondary))
                }
            }
        }

        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .size(56.dp)
                .clip(RoundedCornerShape(GlukoRadius.pill))
                .background(GlukoColors.Ink)
                .clickable { overlays.openPresetEdit(null) },
            contentAlignment = Alignment.Center
        ) {
            LucideIcon(LucidePaths.Plus, 22.dp, GlukoColors.Surface, strokeWidth = 2f)
        }
    }
}

@Composable
private fun PresetCard(preset: MealPresetWithProducts, onClick: () -> Unit) {
    GlukoCard(padding = 14.dp, radius = GlukoRadius.record, modifier = Modifier.plainClickable(onClick = onClick)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(preset.preset.name, style = GlukoType.Body.copy(fontSize = 14.sp), modifier = Modifier.weight(1f), maxLines = 1)
            Spacer(Modifier.width(8.dp))
            XeBadge(stringResource(R.string.bread_units_value_format, formatAmount(preset.totalBreadUnits)), GlukoColors.Tile)
        }
        Spacer(Modifier.height(10.dp))
        preset.products.sortedBy { it.sortOrder }.forEach { product ->
            Row(Modifier.fillMaxWidth().padding(vertical = 2.5.dp)) {
                Text(
                    product.name, style = GlukoType.Hint.copy(color = GlukoColors.TextLabel),
                    modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(stringResource(R.string.bread_units_value_format, formatAmount(product.breadUnits)), style = GlukoType.Hint.copy(color = GlukoColors.TextSecondary).tabular)
            }
        }
        if (preset.preset.comment.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            GlukoDivider()
            Spacer(Modifier.height(10.dp))
            Text(preset.preset.comment, style = GlukoType.CardLabel.copy(color = GlukoColors.TextTertiary), maxLines = 2)
        }
    }
}

@Composable
fun PresetDetailSheet(
    preset: MealPresetWithProducts,
    onPick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    GlukoSheet(onDismiss) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(preset.preset.name, style = GlukoType.SheetTitleLarge)
                if (preset.preset.comment.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(preset.preset.comment, style = GlukoType.Note.copy(color = GlukoColors.TextLabel))
                }
            }
            CircleButton(32.dp, GlukoColors.Tile, onDismiss) {
                LucideIcon(LucidePaths.Close, 14.dp, strokeWidth = 2.2f)
            }
        }
        Spacer(Modifier.height(16.dp))

        GlukoTile(padding = PaddingValues(horizontal = 14.dp, vertical = 13.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.preset_detail_total_carbs), style = GlukoType.Body.copy(color = GlukoColors.TextSecondary), modifier = Modifier.weight(1f))
                Text(stringResource(R.string.bread_units_value_format, formatAmount(preset.totalBreadUnits)), style = GlukoType.Value.copy(fontSize = 19.sp).tabular)
            }
        }
        Spacer(Modifier.height(16.dp))

        Kicker(stringResource(R.string.preset_detail_composition))
        Spacer(Modifier.height(4.dp))
        preset.products.sortedBy { it.sortOrder }.forEachIndexed { index, product ->
            if (index > 0) GlukoDivider()
            Row(Modifier.fillMaxWidth().padding(vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(product.name, style = GlukoType.Body, modifier = Modifier.weight(1f))
                Text(stringResource(R.string.bread_units_value_format, formatAmount(product.breadUnits)), style = GlukoType.Body.copy(color = GlukoColors.TextSecondary).tabular)
            }
        }
        Spacer(Modifier.height(16.dp))

        PrimaryButton(stringResource(R.string.preset_detail_pick), onClick = onPick)
        Spacer(Modifier.height(9.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(GlukoSpacing.itemGap)) {
            SecondaryButton(stringResource(R.string.action_edit), Modifier.weight(1f), onClick = onEdit)
            SecondaryButton(stringResource(R.string.delete), Modifier.weight(1f), textColor = GlukoColors.TextLabel, onClick = onDelete)
        }
    }
}

private data class ProductDraft(val key: Long, val name: String, val amount: String)

@Composable
fun PresetEditSheet(
    preset: MealPresetWithProducts?,
    onConfirm: (name: String, comment: String, products: List<MealPresetProduct>) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(preset?.preset?.name ?: "") }
    var comment by remember { mutableStateOf(preset?.preset?.comment ?: "") }
    var nextKey by remember { mutableStateOf(0L) }
    val products = remember {
        mutableStateListOf<ProductDraft>().apply {
            val initial = preset?.products?.sortedBy { it.sortOrder }
            if (initial.isNullOrEmpty()) {
                add(ProductDraft(nextKey++, "", ""))
            } else {
                initial.forEach { add(ProductDraft(nextKey++, it.name, formatAmount(it.breadUnits))) }
            }
        }
    }
    val total = products.sumOf { (it.amount.toFloatOrNull() ?: 0f).toDouble() }.toFloat()
    val isValid = name.isNotBlank() && products.isNotEmpty() &&
        products.all { it.name.isNotBlank() && (it.amount.isBlank() || it.amount.toFloatOrNull() != null) }

    GlukoSheet(onDismiss, maxHeightFraction = 0.88f) {
        SheetHeader(stringResource(if (preset == null) R.string.add_meal_preset else R.string.edit_meal_preset), onClose = onDismiss)

        Column(Modifier.verticalScroll(rememberScrollState())) {
            Kicker(stringResource(R.string.meal_preset_name_label))
            Spacer(Modifier.height(8.dp))
            GlukoField(name, { name = it }, stringResource(R.string.meal_preset_name_placeholder))

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth()) {
                Kicker(stringResource(R.string.meal_preset_product_name_label), Modifier.weight(1f))
                Kicker(stringResource(R.string.bread_units_short_label))
            }
            Spacer(Modifier.height(8.dp))

            products.forEachIndexed { index, draft ->
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlukoField(
                        value = draft.name,
                        onValueChange = { products[index] = draft.copy(name = it) },
                        placeholder = stringResource(R.string.meal_preset_product_name_placeholder),
                        modifier = Modifier.weight(1f)
                    )
                    GlukoField(
                        value = draft.amount,
                        onValueChange = { products[index] = draft.copy(amount = numeric(it)) },
                        placeholder = "0",
                        modifier = Modifier.width(64.dp),
                        textStyle = GlukoType.Body.tabular,
                        keyboardType = KeyboardType.Decimal
                    )
                    CircleButton(34.dp, GlukoColors.Tile, if (products.size > 1) ({ products.removeAt(index) }) else null) {
                        LucideIcon(LucidePaths.Minus, 14.dp, if (products.size > 1) GlukoColors.Ink else GlukoColors.Placeholder, strokeWidth = 2.2f)
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth()
                    .plainClickable(enabled = products.size < MAX_PRESET_PRODUCTS) { products.add(ProductDraft(nextKey++, "", "")) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleButton(26.dp, GlukoColors.Tile) {
                    LucideIcon(LucidePaths.Plus, 13.dp, strokeWidth = 2.2f)
                }
                Spacer(Modifier.width(9.dp))
                Text(stringResource(R.string.add_product), style = GlukoType.Body.copy(color = GlukoColors.TextSecondary))
            }

            Spacer(Modifier.height(14.dp))
            GlukoTile(padding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.meal_breakdown_total_label), style = GlukoType.Body.copy(color = GlukoColors.TextSecondary), modifier = Modifier.weight(1f))
                    Text(stringResource(R.string.bread_units_value_format, formatAmount(total)), style = GlukoType.Value.tabular)
                }
            }

            Spacer(Modifier.height(16.dp))
            Kicker(stringResource(R.string.comment_label))
            Spacer(Modifier.height(8.dp))
            GlukoField(comment, { comment = it }, stringResource(R.string.meal_preset_comment_placeholder), singleLine = false, minLines = 2)

            Spacer(Modifier.height(14.dp))
            SaveEntryButton(
                enabled = isValid,
                text = stringResource(R.string.save),
                showArrow = false,
                onSave = {
                    onConfirm(
                        name.trim(),
                        comment.trim(),
                        products.mapIndexed { index, draft ->
                            MealPresetProduct(mealPresetId = 0, name = draft.name.trim(), breadUnits = draft.amount.toFloatOrNull() ?: 0f, sortOrder = index)
                        }
                    )
                }
            )
        }
    }
}
