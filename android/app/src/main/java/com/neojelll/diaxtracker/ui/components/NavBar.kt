package com.neojelll.diaxtracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoType

data class NavBarItem(val route: String, val labelRes: Int, val iconPath: String)

private val DockItemHeight = 46.dp
private val DockPadding = 6.dp

/** The dock's full height: one row of items plus its inner padding. */
val DockHeight = DockItemHeight + DockPadding * 2

/** Gap between the dock and the system navigation bar. */
val DockBottomGap = 10.dp

/**
 * Extra bottom padding a scrolling screen adds to its content so the last item can scroll clear of
 * the floating dock. Zero outside the app's nav host.
 */
val LocalDockClearance = staticCompositionLocalOf { 0.dp }

private val DockEase = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)
private val DockShape = RoundedCornerShape(22.dp)

/**
 * Floating bottom dock: a translucent white capsule of icons; the selected tab sits on an ink plate
 * and its name slides out beside the icon. [visible] = false slides it below the screen edge
 * (hidden while content scrolls down).
 */
@Composable
fun GlukoNavBar(
    items: List<NavBarItem>,
    currentRoute: String?,
    onSelect: (NavBarItem) -> Unit,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    val hidden by animateFloatAsState(if (visible) 0f else 1f, tween(350, easing = DockEase), label = "dockHidden")
    Row(
        modifier = modifier
            .graphicsLayer {
                // Far enough to clear the gap and the system bar below; the fade hides any rest.
                translationY = hidden * (DockHeight + DockBottomGap + 48.dp).toPx()
                alpha = 1f - hidden
            }
            .shadow(14.dp, DockShape, ambientColor = GlukoColors.Ink.copy(alpha = .06f), spotColor = GlukoColors.Ink.copy(alpha = .22f))
            .clip(DockShape)
            .background(GlukoColors.Surface.copy(alpha = .94f))
            .border(1.dp, GlukoColors.Ink.copy(alpha = .05f), DockShape)
            .padding(DockPadding),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEach { item ->
            DockItem(item, selected = currentRoute == item.route, onClick = { onSelect(item) })
        }
    }
}

/** Icon only; selected = ink plate with the icon white and the tab's name slid out after it. */
@Composable
private fun DockItem(item: NavBarItem, selected: Boolean, onClick: () -> Unit) {
    val label = stringResource(item.labelRes)
    val bg by animateColorAsState(if (selected) GlukoColors.Ink else Color.Transparent, tween(300), label = "dockItemBg")
    val fg by animateColorAsState(if (selected) GlukoColors.Surface else GlukoColors.TextTertiary, tween(250), label = "dockItemFg")
    Row(
        Modifier
            .height(DockItemHeight)
            .clip(RoundedCornerShape(17.dp))
            .background(bg)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            // The name is only visible on the selected tab; the rest are icons, so name every tab here.
            .semantics { contentDescription = label }
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LucideIcon(item.iconPath, 20.dp, fg)
        AnimatedVisibility(
            visible = selected,
            enter = expandHorizontally(tween(400, easing = DockEase)) + fadeIn(tween(250)),
            exit = shrinkHorizontally(tween(400, easing = DockEase)) + fadeOut(tween(150))
        ) {
            Text(
                label,
                style = GlukoType.ButtonPrimary.copy(fontSize = 13.sp, color = GlukoColors.Surface),
                maxLines = 1,
                modifier = Modifier.padding(start = 8.dp, end = 3.dp).clearAndSetSemantics { }
            )
        }
    }
}
