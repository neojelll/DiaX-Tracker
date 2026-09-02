package com.neojelll.diaxtracker.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

val GlassBorderColor = Color.White.copy(alpha = 0.14f)
val GlassPanelColor = Color.Black.copy(alpha = 0.38f)

val GlassSheen = Brush.linearGradient(
    0f to Color.White.copy(alpha = 0.32f),
    0.3f to Color.White.copy(alpha = 0.08f),
    0.55f to Color.Transparent,
    1f to Color.Transparent
)

val OnGlass = Color.White
val OnGlassMuted = Color.White.copy(alpha = 0.7f)

fun Modifier.glassPanel(
    shape: Shape = RoundedCornerShape(24.dp)
): Modifier = this
    .clip(shape)
    .background(GlassPanelColor)
    .border(1.dp, GlassBorderColor, shape)
