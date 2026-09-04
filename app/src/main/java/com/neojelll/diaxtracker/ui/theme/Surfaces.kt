package com.neojelll.diaxtracker.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

fun Modifier.card(
    shape: Shape = RoundedCornerShape(12.dp)
): Modifier = this
    .shadow(
        elevation = 3.dp,
        shape = shape,
        ambientColor = Color.Black.copy(alpha = 0.03f),
        spotColor = Color.Black.copy(alpha = 0.03f)
    )
    .clip(shape)
    .background(CardBackground)

fun Modifier.fieldBox(
    shape: Shape = RoundedCornerShape(8.dp)
): Modifier = this
    .clip(shape)
    .background(FieldBackground)
    .border(1.dp, CardBorder, shape)
