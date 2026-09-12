package com.neojelll.diaxtracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.MealPresetWithProducts
import com.neojelll.diaxtracker.ui.screens.MealProductEntry
import com.neojelll.diaxtracker.ui.screens.formatAmount
import com.neojelll.diaxtracker.ui.theme.BorderDashed
import com.neojelll.diaxtracker.ui.theme.CardDivider
import com.neojelll.diaxtracker.ui.theme.CarouselCubeBorder
import com.neojelll.diaxtracker.ui.theme.FieldTile
import com.neojelll.diaxtracker.ui.theme.GlucoIcons
import com.neojelll.diaxtracker.ui.theme.Ink
import com.neojelll.diaxtracker.ui.theme.Kicker
import com.neojelll.diaxtracker.ui.theme.PageBackground
import com.neojelll.diaxtracker.ui.theme.PlaceholderText
import com.neojelll.diaxtracker.ui.theme.TextLabel
import com.neojelll.diaxtracker.ui.theme.TextSecondary
import com.neojelll.diaxtracker.ui.theme.TextTertiary

@Composable
fun MealFoodField(
    label: String,
    placeholder: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    mealPresets: List<MealPresetWithProducts>,
    onPresetPicked: (mealLabel: String, breadUnits: Float, products: List<MealProductEntry>) -> Unit,
    onManualEntry: (breadUnits: Float) -> Unit,
    onCreatePreset: () -> Unit
) {
    val presetLabelFormat = stringResource(R.string.meal_label_with_preset_format)

    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(FieldTile)
                .clickable { onExpandedChange(!expanded) }
                .padding(horizontal = 14.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label.ifBlank { placeholder },
                fontSize = 13.5.sp,
                color = if (label.isBlank()) TextTertiary else Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            RoundIconButton(
                icon = if (expanded) GlucoIcons.ChevronUp else GlucoIcons.ChevronDown,
                contentDescription = null,
                onClick = { onExpandedChange(!expanded) },
                size = 24.dp,
                background = Color.White
            )
        }

        AnimatedVisibility(visible = expanded, enter = fadeIn(tween(160)), exit = fadeOut(tween(160))) {
            var manualXe by remember { mutableStateOf("") }
            Column(Modifier.padding(top = 12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.presets_kicker), style = Kicker, color = TextLabel)
                    Text(
                        pluralStringResource(R.plurals.preset_count, mealPresets.size, mealPresets.size),
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                }

                MealPresetCarousel(
                    presets = mealPresets,
                    onPick = { preset ->
                        onPresetPicked(
                            presetLabelFormat.format(preset.preset.name, formatAmount(preset.totalBreadUnits)),
                            preset.totalBreadUnits,
                            preset.products.sortedBy { it.sortOrder }.map { MealProductEntry(it.name, formatAmount(it.breadUnits)) }
                        )
                        onExpandedChange(false)
                    },
                    onCreatePreset = onCreatePreset,
                    modifier = Modifier.padding(top = 9.dp)
                )

                Box(Modifier.fillMaxWidth().padding(vertical = 14.dp).height(1.dp).background(CardDivider))

                Text(
                    stringResource(R.string.xe_no_preset_kicker),
                    style = Kicker,
                    color = TextLabel,
                    modifier = Modifier.padding(bottom = 9.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(FieldTile)
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        if (manualXe.isEmpty()) {
                            Text("0", fontSize = 13.5.sp, color = PlaceholderText)
                        }
                        BasicTextField(
                            value = manualXe,
                            onValueChange = { manualXe = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                            textStyle = TextStyle(fontSize = 13.5.sp, color = Ink),
                            cursorBrush = SolidColor(Ink),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Ink)
                            .clickable {
                                manualXe.replace(',', '.').toFloatOrNull()?.let {
                                    onManualEntry(it)
                                    onExpandedChange(false)
                                    manualXe = ""
                                }
                            }
                            .padding(horizontal = 18.dp, vertical = 12.dp)
                    ) {
                        Text(stringResource(R.string.xe_enter_button), fontSize = 13.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun MealPresetCarousel(
    presets: List<MealPresetWithProducts>,
    onPick: (MealPresetWithProducts) -> Unit,
    onCreatePreset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        presets.forEach { preset ->
            PresetCube(preset = preset, onClick = { onPick(preset) })
        }
        CreatePresetCube(onClick = onCreatePreset)
    }
}

@Composable
private fun PresetCube(preset: MealPresetWithProducts, onClick: () -> Unit) {
    Column(
        Modifier
            .width(154.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(PageBackground)
            .border(1.dp, CarouselCubeBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(top = 13.dp, start = 13.dp, end = 13.dp, bottom = 12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                preset.preset.name,
                fontSize = 13.5.sp,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(Modifier.width(8.dp))
            Box(Modifier.clip(RoundedCornerShape(50)).background(Color.White).padding(horizontal = 8.dp, vertical = 3.dp)) {
                Text(
                    stringResource(R.string.bread_units_value_format, formatAmount(preset.totalBreadUnits)),
                    fontSize = 10.5.sp,
                    color = Ink
                )
            }
        }
        preset.products.sortedBy { it.sortOrder }.forEach { product ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 2.5.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(product.name, fontSize = 11.5.sp, color = TextLabel, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text(formatAmount(product.breadUnits), fontSize = 11.5.sp, color = TextSecondary)
            }
        }
        if (preset.preset.comment.isNotBlank()) {
            Text(
                preset.preset.comment,
                fontSize = 10.5.sp,
                color = TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 9.dp)
            )
        }
    }
}

@Composable
private fun CreatePresetCube(onClick: () -> Unit) {
    Column(
        Modifier
            .width(104.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, BorderDashed, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(13.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(Modifier.size(30.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
            Icon(GlucoIcons.Plus, contentDescription = null, tint = Ink, modifier = Modifier.size(15.dp))
        }
        Text(
            stringResource(R.string.create_preset_label),
            fontSize = 11.5.sp,
            color = TextLabel,
            textAlign = TextAlign.Center
        )
    }
}
