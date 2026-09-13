package com.neojelll.diaxtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoRadius
import com.neojelll.diaxtracker.ui.theme.GlukoType

/** Choice tile used for language/period pickers: selected = black fill, white text. */
@Composable
fun ChoiceTile(modifier: Modifier, text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(GlukoRadius.field))
            .background(if (selected) GlukoColors.Ink else GlukoColors.Tile)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = GlukoType.Body.copy(color = if (selected) GlukoColors.Surface else GlukoColors.Ink),
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

/** Decorative on/off switch, 46x27dp, matching the design's toggle geometry. */
@Composable
fun GlukoSwitch(checked: Boolean, onToggle: () -> Unit) {
    Box(
        Modifier
            .size(46.dp, 27.dp)
            .clip(RoundedCornerShape(GlukoRadius.pill))
            .background(if (checked) GlukoColors.Ink else GlukoColors.BorderDashed)
            .clickable(onClick = onToggle)
            .padding(3.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(Modifier.size(21.dp).clip(RoundedCornerShape(GlukoRadius.pill)).background(GlukoColors.Surface))
    }
}

/** Row inside the "Data" settings card: icon tile, title/subtitle, trailing chevron. */
@Composable
fun DataRow(iconPath: String, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleButton(34.dp, GlukoColors.Tile) {
            LucideIcon(iconPath, 16.dp, strokeWidth = 1.8f)
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = GlukoType.Body)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = GlukoType.Hint)
        }
        LucideIcon(LucidePaths.ChevronRight, 15.dp, GlukoColors.TextTertiary, strokeWidth = 2f)
    }
}
