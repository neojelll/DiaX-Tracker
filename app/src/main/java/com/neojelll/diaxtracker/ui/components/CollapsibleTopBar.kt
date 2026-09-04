package com.neojelll.diaxtracker.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
class CollapsibleTopBarState(val maxHeightPx: Float) {
    var heightPx by mutableFloatStateOf(maxHeightPx)
        private set

    val isFullyExpanded: Boolean
        get() = heightPx >= maxHeightPx

    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (available.y == 0f) return Offset.Zero
            val newHeight = (heightPx + available.y).coerceIn(0f, maxHeightPx)
            val consumed = newHeight - heightPx
            heightPx = newHeight
            return Offset(0f, consumed)
        }
    }
}

@Composable
fun rememberCollapsibleTopBarState(contentHeight: Dp = 52.dp): CollapsibleTopBarState {
    val density = LocalDensity.current
    val statusBarPx = WindowInsets.statusBars.getTop(density)
    val maxHeightPx = with(density) { contentHeight.toPx() } + statusBarPx
    return remember(maxHeightPx) { CollapsibleTopBarState(maxHeightPx) }
}

@Composable
fun CollapsibleTopBar(
    state: CollapsibleTopBarState,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    title: @Composable () -> Unit
) {
    val heightDp = with(LocalDensity.current) { state.heightPx.toDp() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp)
            .clipToBounds()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .align(Alignment.BottomStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            navigationIcon()
            Box(modifier = Modifier.weight(1f)) { title() }
            actions()
        }
    }
}
