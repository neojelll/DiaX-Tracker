package com.neojelll.diaxtracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.MealPresetWithProducts
import com.neojelll.diaxtracker.ui.screens.formatAmount
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoRadius
import com.neojelll.diaxtracker.ui.theme.GlukoType
import com.neojelll.diaxtracker.ui.theme.tabular

/** Display-only shaping of a meal preset for the carousel/dropdown; the DB model is unchanged. */
data class PresetOption(val id: Long, val title: String, val xeLabel: String, val items: List<Pair<String, String>>, val comment: String)

@Composable
fun MealPresetWithProducts.toPresetOption(): PresetOption {
    val unitFormat = stringResource(R.string.bread_units_value_format)
    return PresetOption(
        id = preset.id,
        title = preset.name,
        xeLabel = unitFormat.format(formatAmount(totalBreadUnits)),
        items = products.sortedBy { it.sortOrder }.map { it.name to unitFormat.format(formatAmount(it.breadUnits)) },
        comment = preset.comment
    )
}

/**
 * Meal picker used both on the entry form and inside the record-edit sheet: closed row that
 * expands into a preset carousel plus a manual bread-units entry.
 */
@Composable
fun FoodPicker(
    value: String,
    expanded: Boolean,
    presets: List<PresetOption>,
    manualXe: String,
    placeholder: String,
    onToggle: () -> Unit,
    onPick: (PresetOption) -> Unit,
    onManualXeChange: (String) -> Unit,
    onManualXeApply: () -> Unit,
    onCreatePreset: () -> Unit
) {
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(GlukoRadius.field))
                .background(GlukoColors.Tile)
                .clickable(onClick = onToggle)
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                value.ifEmpty { placeholder },
                style = GlukoType.Body.copy(color = if (value.isEmpty()) GlukoColors.TextTertiary else GlukoColors.Ink),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            CircleButton(24.dp, GlukoColors.Surface) {
                LucideIcon(if (expanded) LucidePaths.ChevronUp else LucidePaths.ChevronDown, 13.dp, strokeWidth = 2.2f)
            }
        }

        AnimatedVisibility(expanded, enter = fadeIn(), exit = fadeOut()) {
            Column(Modifier.padding(top = 11.dp)) {
                Row(Modifier.fillMaxWidth()) {
                    Kicker(stringResource(R.string.food_picker_presets_kicker), Modifier.weight(1f))
                    Text(
                        pluralStringResource(R.plurals.presets_count, presets.size, presets.size),
                        style = GlukoType.Hint
                    )
                }
                Spacer(Modifier.height(9.dp))

                LazyRow(state = rememberLazyListState(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    items(presets) { preset -> PresetCube(preset) { onPick(preset) } }
                    item { CreatePresetCube(onCreatePreset) }
                }

                Spacer(Modifier.height(14.dp))
                GlukoDivider()
                Spacer(Modifier.height(12.dp))

                Kicker(stringResource(R.string.food_picker_manual_kicker))
                Spacer(Modifier.height(9.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlukoField(
                        value = manualXe,
                        onValueChange = { onManualXeChange(numeric(it)) },
                        placeholder = "0",
                        modifier = Modifier.weight(1f),
                        textStyle = GlukoType.Body.tabular,
                        keyboardType = KeyboardType.Decimal,
                        padding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                    )
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(GlukoRadius.field))
                            .background(GlukoColors.Ink)
                            .clickable(onClick = onManualXeApply)
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.food_picker_apply), style = GlukoType.Body.copy(color = GlukoColors.Surface))
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetCube(preset: PresetOption, onClick: () -> Unit) {
    Column(
        Modifier
            .width(154.dp)
            .clip(RoundedCornerShape(GlukoRadius.panel))
            .background(GlukoColors.Screen)
            .border(1.dp, GlukoColors.PresetCubeBorder, RoundedCornerShape(GlukoRadius.panel))
            .clickable(onClick = onClick)
            .padding(start = 13.dp, end = 13.dp, top = 13.dp, bottom = 12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(preset.title, style = GlukoType.Body, modifier = Modifier.weight(1f), maxLines = 1)
            Spacer(Modifier.width(8.dp))
            XeBadge(preset.xeLabel)
        }
        Spacer(Modifier.height(10.dp))
        preset.items.forEach { (name, xe) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 2.5.dp)) {
                Text(
                    name,
                    style = GlukoType.Hint.copy(color = GlukoColors.TextLabel),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(xe, style = GlukoType.Hint.copy(color = GlukoColors.TextSecondary).tabular)
            }
        }
        if (preset.comment.isNotBlank()) {
            Spacer(Modifier.height(9.dp))
            Text(preset.comment, style = GlukoType.CardLabel.copy(color = GlukoColors.TextTertiary), maxLines = 2)
        }
    }
}

@Composable
private fun CreatePresetCube(onClick: () -> Unit) {
    Column(
        Modifier
            .width(104.dp)
            .heightIn(min = 120.dp)
            .dashedBorder(radius = GlukoRadius.panel)
            .clickable(onClick = onClick)
            .padding(13.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircleButton(30.dp, GlukoColors.Surface) {
            LucideIcon(LucidePaths.Plus, 14.dp, strokeWidth = 2.2f)
        }
        Spacer(Modifier.height(9.dp))
        Text(
            stringResource(R.string.food_picker_create_preset),
            style = GlukoType.CardLabel.copy(color = GlukoColors.TextSecondary),
            textAlign = TextAlign.Center
        )
    }
}
