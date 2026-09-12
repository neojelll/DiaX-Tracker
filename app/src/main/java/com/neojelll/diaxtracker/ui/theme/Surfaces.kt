package com.neojelll.diaxtracker.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

val CardShapeLarge = RoundedCornerShape(22.dp)
val CardShapeMedium = RoundedCornerShape(20.dp)
val PlaqueShape = RoundedCornerShape(18.dp)
val TileShape = RoundedCornerShape(16.dp)
val InputShape = RoundedCornerShape(14.dp)
val CollapsedPlaqueShape = RoundedCornerShape(12.dp)
val PillShape = RoundedCornerShape(percent = 50)
val NavBarShape = RoundedCornerShape(30.dp, 30.dp, 42.dp, 42.dp)
val SheetShape = RoundedCornerShape(28.dp, 28.dp, 42.dp, 42.dp)

/** A white content card, per spec's `0 1px 2px rgba(0,0,0,.05)` resting shadow. */
fun Modifier.card(
    shape: Shape = CardShapeLarge
): Modifier = this
    .shadow(1.dp, shape, clip = false, ambientColor = ShadowColor, spotColor = ShadowColor)
    .clip(shape)
    .background(CardSurface)

/** A nested warm-gray tile: inputs, mini-tiles, date/time chips. */
fun Modifier.fieldBox(
    shape: Shape = InputShape
): Modifier = this
    .clip(shape)
    .background(FieldTile)

/** The always-white circular icon button used for chevrons, close buttons, steppers. */
fun Modifier.roundIconButton(
    background: Color = FieldTile
): Modifier = this
    .clip(CircleShape)
    .background(background)

private val ShadowColor = Color(0x0D000000)
