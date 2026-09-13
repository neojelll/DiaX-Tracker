package com.neojelll.diaxtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.ui.theme.BorderLight
import com.neojelll.diaxtracker.ui.theme.ButtonLabel
import com.neojelll.diaxtracker.ui.theme.FieldText
import com.neojelll.diaxtracker.ui.theme.Ink
import com.neojelll.diaxtracker.ui.theme.InkActive
import com.neojelll.diaxtracker.ui.theme.PillShape
import com.neojelll.diaxtracker.ui.theme.TextLabel

@Composable
fun PrimaryPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailingIcon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val background = if (pressed) InkActive else Ink

    Row(
        modifier
            .fillMaxWidth()
            .clip(PillShape)
            .background(background)
            .clickable(interactionSource = interactionSource, indication = null, enabled = enabled, onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, style = ButtonLabel, color = Color.White)
        if (trailingIcon != null) {
            Spacer(Modifier.width(9.dp))
            Icon(trailingIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun OutlinedPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = Ink,
    leadingIcon: ImageVector? = null
) {
    Row(
        modifier
            .clip(PillShape)
            .border(1.dp, BorderLight, PillShape)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, tint = textColor, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(7.dp))
        }
        Text(text, style = FieldText, color = textColor)
    }
}

@Composable
fun PillChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .clip(PillShape)
            .background(if (selected) Ink else Color.White)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 14.dp)
    ) {
        Text(text, fontSize = 12.sp, color = if (selected) Color.White else TextLabel)
    }
}
