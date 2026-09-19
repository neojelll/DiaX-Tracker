package com.neojelll.diaxtracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoRadius
import com.neojelll.diaxtracker.ui.theme.GlukoSpacing
import com.neojelll.diaxtracker.ui.theme.GlukoType
import com.neojelll.diaxtracker.ui.theme.tabular

/** Soft shadow used by all white surfaces: barely-there elevation, not a lift. */
fun Modifier.shadowSoft(radius: Dp) = this.shadow(
    elevation = 1.dp,
    shape = RoundedCornerShape(radius),
    ambientColor = Color(0x0D000000),
    spotColor = Color(0x0D000000)
)

/** Primary white surface used throughout the app: cards, navbar, sheets. */
@Composable
fun GlukoCard(
    modifier: Modifier = Modifier,
    padding: Dp = GlukoSpacing.cardPadding,
    radius: Dp = GlukoRadius.card,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadowSoft(radius)
            .clip(RoundedCornerShape(radius))
            .background(GlukoColors.Surface)
            .padding(padding),
        content = content
    )
}

/** Nested tile inside a card: fields and small blocks. */
@Composable
fun GlukoTile(
    modifier: Modifier = Modifier,
    radius: Dp = GlukoRadius.tile,
    padding: PaddingValues = PaddingValues(horizontal = 13.dp, vertical = 11.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(radius)
    Column(
        modifier = modifier
            .clip(shape)
            .background(GlukoColors.Tile)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(padding),
        content = content
    )
}

@Composable
fun Kicker(text: String, modifier: Modifier = Modifier) {
    Text(text.uppercase(), style = GlukoType.Kicker, modifier = modifier)
}

@Composable
fun GlukoDivider(modifier: Modifier = Modifier, color: Color = GlukoColors.Divider) {
    Box(modifier.fillMaxWidth().height(1.dp).background(color))
}

/** Black full-width pill button, the app's primary action style. */
@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GlukoRadius.pill))
            .background(GlukoColors.Ink)
            .clickable(
                interactionSource = interaction,
                indication = rememberRipple(color = Color.White),
                onClick = onClick
            )
            .padding(vertical = 16.dp, horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, style = GlukoType.ButtonPrimary.copy(color = GlukoColors.Surface))
        if (trailingIcon != null) {
            Spacer(Modifier.width(8.dp))
            trailingIcon()
        }
    }
}

/** White outlined pill button, the app's secondary action style. */
@Composable
fun SecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = GlukoColors.Ink,
    leadingIcon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(GlukoRadius.pill))
            .background(GlukoColors.Surface)
            .border(BorderStroke(1.dp, GlukoColors.Border), RoundedCornerShape(GlukoRadius.pill))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(Modifier.width(7.dp))
        }
        Text(text, style = GlukoType.Body.copy(color = textColor).copy(fontSize = 13.sp))
    }
}

/** Field with no border/focus ring by design — only the system caret shows focus. */
@Composable
fun GlukoField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = GlukoType.Body,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    onDone: (() -> Unit)? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    background: Color = GlukoColors.Tile,
    padding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 13.dp)
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GlukoRadius.field))
            .background(background)
            .padding(padding),
        textStyle = textStyle,
        singleLine = singleLine,
        minLines = minLines,
        cursorBrush = SolidColor(GlukoColors.Ink),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = {
            keyboardController?.hide()
            focusManager.clearFocus()
            onDone?.invoke()
        }),
        decorationBox = { inner ->
            if (value.isEmpty()) {
                Text(placeholder, style = textStyle.copy(color = GlukoColors.Placeholder))
            }
            inner()
        }
    )
}

/** Filters free text input down to digits, dot and comma (decimal separators). */
fun numeric(input: String): String = input.filter { it.isDigit() || it == '.' || it == ',' }

// Bare rows/cards with no clip+background of their own would show Android's default ripple
// as an uncontained gray rectangle; this app's flat design has no press-state visuals at all.
@Composable
fun Modifier.plainClickable(enabled: Boolean = true, onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    return this.clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
}

/** Dashed border used for "create" affordances and empty-photo placeholders. */
fun Modifier.dashedBorder(
    color: Color = GlukoColors.BorderDashed,
    radius: Dp = GlukoRadius.tile,
    width: Dp = 1.dp
) = this.drawBehind {
    val stroke = Stroke(
        width = width.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f), 0f)
    )
    drawRoundRect(
        color = color,
        topLeft = Offset(width.toPx() / 2, width.toPx() / 2),
        size = size.copy(width = size.width - width.toPx(), height = size.height - width.toPx()),
        cornerRadius = CornerRadius(radius.toPx()),
        style = stroke
    )
}

@Composable
fun CircleButton(
    size: Dp,
    background: Color = GlukoColors.Tile,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(GlukoRadius.pill))
            .background(background)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(GlukoRadius.pill))
            .background(if (selected) GlukoColors.Ink else GlukoColors.Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text,
            style = GlukoType.Body.copy(
                fontSize = 12.sp,
                color = if (selected) GlukoColors.Surface else GlukoColors.TextSecondary
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun XeBadge(text: String, background: Color = GlukoColors.Surface) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(GlukoRadius.pill))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, style = GlukoType.CardLabel.copy(color = GlukoColors.Ink).tabular, maxLines = 1)
    }
}
