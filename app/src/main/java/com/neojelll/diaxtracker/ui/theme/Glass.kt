package com.neojelll.diaxtracker.ui.theme

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeChild

val GlassStyle = HazeStyle(
    backgroundColor = Color.Black,
    tint = HazeTint(Color.Black.copy(alpha = 0.45f)),
    blurRadius = 24.dp,
    noiseFactor = 0.08f
)

val GlassBorderColor = Color.White.copy(alpha = 0.14f)
val OnGlass = Color.White
val OnGlassMuted = Color.White.copy(alpha = 0.7f)

fun Modifier.glassPanel(
    hazeState: HazeState,
    shape: Shape = RoundedCornerShape(24.dp)
): Modifier = this
    .clip(shape)
    .hazeChild(state = hazeState, style = GlassStyle)
    .border(1.dp, GlassBorderColor, shape)
