package com.neojelll.diaxtracker.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

fun Modifier.card(
    shape: Shape = RoundedCornerShape(24.dp)
): Modifier = this
    .clip(shape)
    .background(CardBackground)
    .border(1.dp, CardBorder, shape)

fun Modifier.fieldBox(
    shape: Shape = RoundedCornerShape(14.dp)
): Modifier = this
    .clip(shape)
    .background(FieldBackground)
