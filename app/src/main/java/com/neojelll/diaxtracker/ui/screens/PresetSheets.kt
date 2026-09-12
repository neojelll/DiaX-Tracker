package com.neojelll.diaxtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.MealPresetProduct
import com.neojelll.diaxtracker.ui.components.BottomSheetSurface
import com.neojelll.diaxtracker.ui.components.OutlinedPillButton
import com.neojelll.diaxtracker.ui.components.PrimaryPillButton
import com.neojelll.diaxtracker.ui.components.RoundIconButton
import com.neojelll.diaxtracker.ui.components.SheetHandle
import com.neojelll.diaxtracker.ui.components.SheetHeader
import com.neojelll.diaxtracker.ui.components.SheetScrollColumn
import com.neojelll.diaxtracker.ui.theme.CardDivider
import com.neojelll.diaxtracker.ui.theme.FieldTile
import com.neojelll.diaxtracker.ui.theme.GlucoIcons
import com.neojelll.diaxtracker.ui.theme.Ink
import com.neojelll.diaxtracker.ui.theme.Kicker
import com.neojelll.diaxtracker.ui.theme.PlaceholderText
import com.neojelll.diaxtracker.ui.theme.TextLabel
import com.neojelll.diaxtracker.ui.theme.TextTertiary
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel

private const val MAX_PRESET_PRODUCTS = 10

@Composable
fun PresetDetailSheet(
    viewModel: DiaryViewModel,
    presetId: Long,
    onPickForEntry: (mealLabel: String, breadUnits: Float, products: List<MealProductEntry>) -> Unit,
    onEdit: (Long) -> Unit,
    onClose: () -> Unit
) {
    val mealPresets by viewModel.mealPresets.collectAsState()
    val preset = mealPresets.find { it.preset.id == presetId } ?: return
    val presetLabelFormat = stringResource(R.string.meal_label_with_preset_format)

    Box(Modifier.fillMaxSize()) {
        BottomSheetSurface(modifier = Modifier.align(Alignment.BottomCenter), maxHeightFraction = 0.88f) {
            SheetHandle()
            SheetHeader(title = preset.preset.name, subtitle = preset.preset.comment.ifBlank { null }, onClose = onClose)
            SheetScrollColumn {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(FieldTile).padding(horizontal = 15.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.total_carbs_label), fontSize = 13.sp, color = TextLabel)
                    Text(stringResource(R.string.bread_units_value_format, formatAmount(preset.totalBreadUnits)), fontSize = 19.sp, color = Ink)
                }

                Text(stringResource(R.string.composition_kicker), style = Kicker, color = TextLabel, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                preset.products.sortedBy { it.sortOrder }.forEach { product ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 11.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(product.name, fontSize = 13.5.sp, color = Ink)
                        Text(stringResource(R.string.bread_units_value_format, formatAmount(product.breadUnits)), fontSize = 13.sp, color = TextLabel)
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(CardDivider))
                }

                PrimaryPillButton(
                    text = stringResource(R.string.select_for_entry),
                    onClick = {
                        onPickForEntry(
                            presetLabelFormat.format(preset.preset.name, formatAmount(preset.totalBreadUnits)),
                            preset.totalBreadUnits,
                            preset.products.sortedBy { it.sortOrder }.map { MealProductEntry(it.name, formatAmount(it.breadUnits)) }
                        )
                    },
                    modifier = Modifier.padding(top = 18.dp)
                )
                Row(Modifier.fillMaxWidth().padding(top = 9.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    OutlinedPillButton(
                        text = stringResource(R.string.edit_action),
                        leadingIcon = GlucoIcons.Pencil,
                        onClick = { onEdit(presetId) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedPillButton(
                        text = stringResource(R.string.delete),
                        textColor = TextLabel,
                        onClick = {
                            viewModel.deleteMealPreset(preset.preset)
                            onClose()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private data class ProductDraft(val key: Long, val name: String, val amount: String)

@Composable
fun PresetEditorSheet(
    viewModel: DiaryViewModel,
    presetId: Long?,
    onClose: () -> Unit
) {
    val mealPresets by viewModel.mealPresets.collectAsState()
    val preset = presetId?.let { id -> mealPresets.find { it.preset.id == id } }

    var name by remember(presetId) { mutableStateOf(preset?.preset?.name ?: "") }
    var comment by remember(presetId) { mutableStateOf(preset?.preset?.comment ?: "") }
    var nextKey by remember(presetId) { mutableStateOf(0L) }
    val products = remember(presetId) {
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
    val isValid = name.isNotBlank() && products.all { it.name.isNotBlank() }

    Box(Modifier.fillMaxSize()) {
        BottomSheetSurface(modifier = Modifier.align(Alignment.BottomCenter), maxHeightFraction = 0.88f) {
            SheetHandle()
            SheetHeader(
                title = stringResource(if (preset == null) R.string.add_meal_preset else R.string.edit_meal_preset),
                onClose = onClose
            )
            SheetScrollColumn {
                Text(stringResource(R.string.meal_preset_name_kicker), style = Kicker, color = TextLabel, modifier = Modifier.padding(bottom = 8.dp))
                PlainTile(value = name, onValueChange = { name = it }, placeholder = stringResource(R.string.meal_preset_name_placeholder))

                Row(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.products_kicker), style = Kicker, color = TextLabel)
                    Text(stringResource(R.string.bread_units_short_label), fontSize = 11.sp, color = TextTertiary)
                }
                products.forEachIndexed { index, draft ->
                    Row(
                        Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PlainTile(
                            value = draft.name,
                            onValueChange = { products[index] = draft.copy(name = it) },
                            placeholder = stringResource(R.string.meal_preset_product_name_placeholder),
                            modifier = Modifier.weight(1f)
                        )
                        PlainTile(
                            value = draft.amount,
                            onValueChange = { text -> products[index] = draft.copy(amount = text.filter { c -> c.isDigit() || c == '.' || c == ',' }) },
                            placeholder = "0",
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier.width(64.dp),
                            centered = true
                        )
                        RoundIconButton(
                            icon = GlucoIcons.Close,
                            contentDescription = stringResource(R.string.remove_product),
                            onClick = { if (products.size > 1) products.removeAt(index) },
                            size = 34.dp,
                            background = Color.White
                        )
                    }
                }

                Row(
                    Modifier
                        .clickable(enabled = products.size < MAX_PRESET_PRODUCTS) {
                            products.add(ProductDraft(key = nextKey++, name = "", amount = ""))
                        }
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RoundIconButton(icon = GlucoIcons.Plus, contentDescription = null, onClick = { }, size = 26.dp)
                    Text(stringResource(R.string.add_product), fontSize = 13.sp, color = Ink, modifier = Modifier.padding(start = 8.dp))
                }

                Row(
                    Modifier.fillMaxWidth().padding(top = 16.dp).clip(RoundedCornerShape(16.dp)).background(FieldTile).padding(horizontal = 15.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(R.string.meal_breakdown_total_label), fontSize = 13.sp, color = TextLabel)
                    Text(stringResource(R.string.bread_units_value_format, formatAmount(total)), fontSize = 19.sp, color = Ink)
                }

                Text(stringResource(R.string.comment_kicker), style = Kicker, color = TextLabel, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                PlainTile(
                    value = comment,
                    onValueChange = { comment = it },
                    placeholder = stringResource(R.string.comment_placeholder_meal),
                    minLines = 2
                )

                OutlinedPillButton(
                    text = stringResource(R.string.add_photo_short),
                    leadingIcon = GlucoIcons.Camera,
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().padding(top = 11.dp)
                )

                PrimaryPillButton(
                    text = stringResource(R.string.save),
                    enabled = isValid,
                    onClick = {
                        viewModel.saveMealPreset(
                            id = preset?.preset?.id ?: 0,
                            name = name.trim(),
                            comment = comment.trim(),
                            products = products.mapIndexed { index, draft ->
                                MealPresetProduct(
                                    mealPresetId = 0,
                                    name = draft.name.trim(),
                                    breadUnits = draft.amount.toFloatOrNull() ?: 0f,
                                    sortOrder = index
                                )
                            }
                        )
                        onClose()
                    },
                    modifier = Modifier.padding(top = 9.dp)
                )
            }
        }
    }
}

@Composable
private fun PlainTile(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
    centered: Boolean = false
) {
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(FieldTile)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        contentAlignment = if (centered) Alignment.Center else Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(placeholder, fontSize = 13.5.sp, color = PlaceholderText)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontSize = 13.5.sp, color = Ink, textAlign = if (centered) TextAlign.Center else TextAlign.Start),
            cursorBrush = SolidColor(Ink),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            minLines = minLines,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
