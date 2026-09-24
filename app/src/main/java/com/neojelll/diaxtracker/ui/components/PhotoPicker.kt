package com.neojelll.diaxtracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoRadius
import com.neojelll.diaxtracker.ui.theme.GlukoType
import java.io.File

private val MenuEase = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)
private val MenuCardRadius = 24.dp

/**
 * Action sheet behind the photo tile/button: floating cards that slide up from the bottom.
 *
 * Rendered as its own overlay layer so it sits above whatever sheet opened it. The cards are
 * padded by the live navigation-bar inset (not a fixed constant), so they stay strictly above the
 * system bar in gesture and 3-button navigation alike; only the scrim runs full-bleed behind it.
 *
 * A picked action runs after the exit animation has finished and this layer is gone.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PhotoActionMenu(
    hasPhoto: Boolean,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit
) {
    val visible = remember { MutableTransitionState(false).apply { targetState = true } }
    var picked by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun close(action: (() -> Unit)? = null) {
        if (!visible.targetState) return
        picked = action
        visible.targetState = false
    }

    LaunchedEffect(visible.currentState, visible.isIdle) {
        if (visible.isIdle && !visible.currentState) {
            onDismiss()
            picked?.invoke()
        }
    }

    AnimatedVisibility(visibleState = visible, enter = EnterTransition.None, exit = ExitTransition.None) {
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .animateEnterExit(enter = fadeIn(tween(160)), exit = fadeOut(tween(120)))
                    .background(GlukoColors.Scrim)
                    .plainClickable { close() }
            )
            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .animateEnterExit(
                        enter = slideInVertically(tween(220, easing = MenuEase)) { it },
                        exit = slideOutVertically(tween(160)) { it }
                    )
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 10.dp, end = 10.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(MenuCardRadius))
                        .background(GlukoColors.Surface)
                ) {
                    MenuRow(LucidePaths.Camera, stringResource(R.string.photo_take), GlukoColors.Ink) { close(onCamera) }
                    MenuDivider()
                    MenuRow(LucidePaths.Image, stringResource(R.string.photo_choose), GlukoColors.Ink) { close(onGallery) }
                    if (hasPhoto) {
                        MenuDivider()
                        MenuRow(LucidePaths.Trash, stringResource(R.string.remove_photo), GlukoColors.TextLabel) { close(onRemove) }
                    }
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(GlukoRadius.pill))
                        .background(GlukoColors.Surface)
                        .plainClickable { close() }
                        .padding(vertical = 15.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.cancel), style = menuText(GlukoColors.Ink))
                }
            }
        }
    }
}

@Composable
private fun MenuRow(icon: String, label: String, color: Color, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .plainClickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        LucideIcon(icon, 20.dp, color, strokeWidth = 1.7f)
        Text(label, style = menuText(color))
    }
}

@Composable
private fun MenuDivider() {
    Box(
        Modifier
            .padding(horizontal = 18.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(GlukoColors.Divider)
    )
}

private fun menuText(color: Color) = GlukoType.Body.copy(fontSize = 14.5.sp, color = color)

/**
 * Photo affordance on the entry form. Empty: dashed "meal photo" tile. With a photo: thumbnail,
 * "attached", "replace" and a remove cross. Tapping the tile opens the action menu either way.
 */
@Composable
fun PhotoTile(
    photoPath: String?,
    onOpenPicker: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (photoPath == null) {
        Row(
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(GlukoRadius.tile))
                .dashedBorder(radius = GlukoRadius.tile)
                .plainClickable(onClick = onOpenPicker)
                .padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Box(
                Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(GlukoColors.Tile),
                contentAlignment = Alignment.Center
            ) {
                LucideIcon(LucidePaths.Camera, 18.dp, GlukoColors.Ink, strokeWidth = 1.7f)
            }
            Column {
                Text(stringResource(R.string.photo_add_title), style = GlukoType.Body.copy(fontSize = 13.sp))
                Spacer(Modifier.height(2.dp))
                Text(
                    stringResource(R.string.photo_add_hint),
                    style = GlukoType.CardLabel.copy(color = GlukoColors.TextTertiary)
                )
            }
        }
    } else {
        Row(
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(GlukoRadius.tile))
                .background(GlukoColors.Tile)
                .plainClickable(onClick = onOpenPicker)
                .padding(start = 9.dp, top = 9.dp, bottom = 9.dp, end = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            AsyncImage(
                model = File(photoPath),
                contentDescription = stringResource(R.string.entry_photo),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(GlukoColors.BorderDashed)
            )
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.photo_attached), style = GlukoType.Body.copy(fontSize = 13.sp))
                Spacer(Modifier.height(2.dp))
                Text(
                    stringResource(R.string.photo_replace),
                    style = GlukoType.CardLabel.copy(color = GlukoColors.TextLabel)
                )
            }
            Box(
                Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(GlukoRadius.pill))
                    .background(GlukoColors.Surface)
                    .plainClickable(onClick = onRemove),
                contentAlignment = Alignment.Center
            ) {
                LucideIcon(LucidePaths.Close, 12.dp, GlukoColors.Ink, strokeWidth = 2.2f)
            }
        }
    }
}

/** Photo affordance inside sheets (record edit): an outlined pill, sized like the other sheet buttons. */
@Composable
fun PhotoButton(hasPhoto: Boolean, onOpenPicker: () -> Unit, modifier: Modifier = Modifier) {
    val pill = RoundedCornerShape(GlukoRadius.pill)
    Row(
        modifier
            .fillMaxWidth()
            .clip(pill)
            .border(BorderStroke(1.dp, GlukoColors.Border), pill)
            .plainClickable(onClick = onOpenPicker)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LucideIcon(LucidePaths.Camera, 15.dp, GlukoColors.Ink, strokeWidth = 1.8f)
        Text(
            stringResource(if (hasPhoto) R.string.photo_attached_replace else R.string.add_photo_short),
            style = GlukoType.Body.copy(fontSize = 13.sp)
        )
    }
}
