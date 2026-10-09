package com.neojelll.diaxtracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.ui.components.GlukoCard
import com.neojelll.diaxtracker.ui.components.GlukoField
import com.neojelll.diaxtracker.ui.components.LucideIcon
import com.neojelll.diaxtracker.ui.components.LucidePaths
import com.neojelll.diaxtracker.ui.components.PresetOption
import com.neojelll.diaxtracker.ui.components.carbsUnitLabel
import com.neojelll.diaxtracker.ui.components.dashedBorder
import com.neojelll.diaxtracker.ui.components.numeric
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.Ruda
import java.io.File

/*
 * Add-entry screen: the "Еда" and "Детали" blocks, from the design handoff (HomeFoodDetails.kt).
 *
 * Grey (Tile) only behind fields where numbers are typed (sugar, insulin, carbs). Picking a preset,
 * the comment and the photo are rows on white: a 34dp icon square plus a small label over the
 * value. Square: empty -> Tile with a black icon; filled -> Ink with a white icon.
 */

private val Kicker10 = TextStyle(fontFamily = Ruda, fontSize = 10.5.sp, color = GlukoColors.TextTertiary)
private val Value13 = TextStyle(fontFamily = Ruda, fontSize = 13.5.sp, fontWeight = FontWeight.Medium, color = GlukoColors.Ink)
private val Num15 = TextStyle(fontFamily = Ruda, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = GlukoColors.Ink, fontFeatureSettings = "tnum")
private val NumPlaceholder = Color(0xFFB3AEA7)

/** 34dp square, radius 11dp, 16dp icon. */
@Composable
private fun IconBadge(path: String, filled: Boolean) {
    val bg by animateColorAsState(if (filled) GlukoColors.Ink else GlukoColors.Tile, label = "badge")
    Box(
        Modifier.size(34.dp).clip(RoundedCornerShape(11.dp)).background(bg),
        contentAlignment = Alignment.Center
    ) {
        LucideIcon(path, 16.dp, if (filled) Color.White else GlukoColors.Ink, strokeWidth = 1.8f)
    }
}

/** A 46dp row: icon square, a small label over the value. */
@Composable
private fun PlateRow(
    modifier: Modifier,
    icon: String,
    filled: Boolean,
    label: String,
    value: String,
    valueMuted: Boolean,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier
            .height(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(start = 6.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(icon, filled)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = Kicker10)
            Spacer(Modifier.height(1.dp))
            Text(
                value,
                style = Value13.copy(
                    fontSize = if (trailing == null) 13.sp else 13.5.sp,
                    color = if (valueMuted) GlukoColors.TextTertiary else GlukoColors.Ink
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        trailing?.invoke()
    }
}

/**
 * "Еда": one row plus a list that unfolds inside the card.
 *
 * Left - picking a preset (on white); right - the grey 76×46dp carbs field. Picking a preset fills in
 * its carbs; typing carbs by hand drops the preset and the left side says "Без пресета". Empty -
 * "Выбрать пресет" in grey. Tapping a list row picks that preset and folds the list.
 */
@Composable
internal fun FoodRow(
    selected: PresetOption?,
    carbs: String,
    presets: List<PresetOption>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onPick: (PresetOption) -> Unit,
    onCarbsChange: (String) -> Unit,
    onCreatePreset: () -> Unit
) {
    val title = when {
        selected != null -> selected.title
        carbs.isNotBlank() -> stringResource(R.string.record_no_preset)
        else -> stringResource(R.string.food_pick_preset)
    }
    val filled = selected != null || carbs.isNotBlank()
    val chevron by animateFloatAsState(if (expanded) 180f else 0f, label = "chev")

    GlukoCard(padding = PaddingValues(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PlateRow(
                modifier = Modifier.weight(1f),
                icon = LucidePaths.Dish,
                filled = filled,
                label = stringResource(R.string.food_label),
                value = title,
                valueMuted = !filled,
                onClick = onToggle,
                trailing = {
                    LucideIcon(LucidePaths.ChevronDown, 13.dp, GlukoColors.Ink, strokeWidth = 2.2f, modifier = Modifier.rotate(chevron))
                }
            )
            Spacer(Modifier.width(6.dp))
            CarbsField(carbs, onCarbsChange)
        }

        AnimatedVisibility(expanded, enter = fadeIn(), exit = fadeOut()) {
            Column(
                Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth()
                    .shadow(
                        26.dp, RoundedCornerShape(16.dp),
                        ambientColor = GlukoColors.Ink.copy(alpha = .08f), spotColor = GlukoColors.Ink.copy(alpha = .08f)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, GlukoColors.PresetCubeBorder, RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                presets.forEach { p -> PresetMenuRow(p, p.id == selected?.id) { onPick(p) } }
                Box(Modifier.padding(horizontal = 8.dp, vertical = 4.dp).fillMaxWidth().height(1.dp).background(GlukoColors.Divider))
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onCreatePreset).padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(30.dp).dashedBorder(color = GlukoColors.BorderHover, radius = 10.dp),
                        contentAlignment = Alignment.Center
                    ) { LucideIcon(LucidePaths.Plus, 12.dp, GlukoColors.TextSecondary, strokeWidth = 2.4f) }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        stringResource(R.string.food_new_preset),
                        style = TextStyle(fontFamily = Ruda, fontSize = 12.5.sp, color = GlukoColors.TextSecondary)
                    )
                }
            }
        }
    }
}

/** Letter square (black when picked), name, products on one line, carbs in a capsule. */
@Composable
private fun PresetMenuRow(p: PresetOption, on: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (on) GlukoColors.Screen else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(start = 8.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(30.dp).clip(RoundedCornerShape(10.dp)).background(if (on) GlukoColors.Ink else GlukoColors.Tile),
            contentAlignment = Alignment.Center
        ) {
            Text(
                p.title.take(1),
                style = TextStyle(fontFamily = Ruda, fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = if (on) Color.White else GlukoColors.Ink)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(p.title, style = TextStyle(fontFamily = Ruda, fontSize = 13.sp, color = GlukoColors.Ink), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                p.items.joinToString(", ") { it.first },
                style = TextStyle(fontFamily = Ruda, fontSize = 11.sp, color = GlukoColors.TextTertiary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            p.xeLabel,
            style = TextStyle(fontFamily = Ruda, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = GlukoColors.Ink, fontFeatureSettings = "tnum"),
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(if (on) Color.White else GlukoColors.Tile)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

/** Grey 76×46dp carbs field: the unit ("ХЕ" / "г") 10.5sp over the number 15sp. */
@Composable
private fun CarbsField(value: String, onChange: (String) -> Unit) {
    Column(
        Modifier
            .width(76.dp)
            .height(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(GlukoColors.Tile)
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(carbsUnitLabel(), style = Kicker10)
        Spacer(Modifier.height(1.dp))
        BasicTextField(
            value = value,
            onValueChange = { onChange(numeric(it)) },
            textStyle = Num15,
            singleLine = true,
            cursorBrush = SolidColor(GlukoColors.Ink),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            decorationBox = { inner ->
                if (value.isEmpty()) Text("0", style = Num15.copy(color = NumPlaceholder))
                inner()
            }
        )
    }
}

/**
 * "Детали": two rows side by side, on white - "Комментарий" and "Фото".
 *
 * Comment: a tap unfolds the field under the rows (grey, 2 lines); with text the square is black and
 * the value shows the start of it. Photo: a tap opens the camera/gallery menu; once attached the
 * square is black, "Добавлено", and a grey row below with the thumbnail, "Заменить фото" and ×.
 */
@Composable
internal fun DetailsRow(
    note: String,
    noteOpen: Boolean,
    photoPath: String?,
    onToggleNote: () -> Unit,
    onNoteChange: (String) -> Unit,
    onAddPhoto: () -> Unit,
    onRemovePhoto: () -> Unit
) {
    val hasPhoto = photoPath != null
    val add = stringResource(R.string.details_add)
    GlukoCard(padding = PaddingValues(8.dp)) {
        Row {
            PlateRow(
                Modifier.weight(1f), LucidePaths.MessageCircle, note.isNotBlank() || noteOpen,
                stringResource(R.string.comment_label), note.trim().ifEmpty { add }, note.isBlank(), onToggleNote
            )
            Spacer(Modifier.width(6.dp))
            PlateRow(
                Modifier.weight(1f), LucidePaths.Camera, hasPhoto,
                stringResource(R.string.add_photo_short),
                if (hasPhoto) stringResource(R.string.details_added) else add,
                !hasPhoto,
                onClick = { if (!hasPhoto) onAddPhoto() }
            )
        }

        AnimatedVisibility(noteOpen, enter = fadeIn(), exit = fadeOut()) {
            GlukoField(
                value = note,
                onValueChange = onNoteChange,
                placeholder = stringResource(R.string.comment_placeholder),
                singleLine = false,
                minLines = 2,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        AnimatedVisibility(hasPhoto, enter = fadeIn(), exit = fadeOut()) {
            Row(
                Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(GlukoColors.Tile)
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = photoPath?.let { File(it) },
                    contentDescription = stringResource(R.string.entry_photo),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(GlukoColors.BorderDashed)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    stringResource(R.string.details_replace_photo),
                    style = TextStyle(fontFamily = Ruda, fontSize = 12.5.sp, color = GlukoColors.TextSecondary),
                    modifier = Modifier.weight(1f).clickable(onClick = onAddPhoto)
                )
                Box(
                    Modifier.size(30.dp).clip(RoundedCornerShape(999.dp)).background(Color.White).clickable(onClick = onRemovePhoto),
                    contentAlignment = Alignment.Center
                ) { LucideIcon(LucidePaths.Close, 11.dp, GlukoColors.Ink, strokeWidth = 2.4f) }
            }
        }
    }
}
