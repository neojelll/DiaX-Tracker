package com.neojelll.diaxtracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.ui.theme.BackdropScrim
import com.neojelll.diaxtracker.ui.theme.BorderLight
import com.neojelll.diaxtracker.ui.theme.CardSurface
import com.neojelll.diaxtracker.ui.theme.FieldTile
import com.neojelll.diaxtracker.ui.theme.GlucoIcons
import com.neojelll.diaxtracker.ui.theme.Ink
import com.neojelll.diaxtracker.ui.theme.SheetShape
import com.neojelll.diaxtracker.ui.theme.SheetTitle
import com.neojelll.diaxtracker.ui.theme.TextLabel

@Composable
fun SheetBackdrop(onDismiss: () -> Unit) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(160)),
        exit = fadeOut(tween(160))
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(BackdropScrim)
                .clickable(onClick = onDismiss)
        )
    }
}

/** Bottom-up slide entrance shared by every sheet (`sheetUp`, 220ms). */
@Composable
fun BottomSheetSurface(
    modifier: Modifier = Modifier,
    maxHeightFraction: Float = 0.9f,
    content: @Composable ColumnScope.() -> Unit
) {
    val screenHeightDp = LocalConfiguration.current.screenHeightDp
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(tween(220)) { it } + fadeIn(tween(220)),
        exit = slideOutVertically(tween(220)) { it } + fadeOut(tween(160))
    ) {
        Column(
            modifier
                .fillMaxWidth()
                .heightIn(max = (maxHeightFraction * screenHeightDp).dp)
                .clip(SheetShape)
                .background(CardSurface),
            content = content
        )
    }
}

@Composable
fun ColumnScope.SheetHandle() {
    Box(
        Modifier
            .padding(top = 12.dp)
            .size(width = 38.dp, height = 4.dp)
            .align(Alignment.CenterHorizontally)
            .clip(CircleShape)
            .background(BorderLight)
    )
}

@Composable
fun SheetHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClose: (() -> Unit)? = null
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(Modifier.weight(1f, fill = false)) {
            Text(title, style = SheetTitle, color = Ink)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextLabel,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        if (onClose != null) {
            RoundIconButton(icon = GlucoIcons.Close, contentDescription = stringResource(R.string.close), onClick = onClose)
        }
    }
}

@Composable
fun RoundIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    background: Color = FieldTile,
    tint: Color = Ink
) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(size * 0.44f))
    }
}

@Composable
fun ColumnScope.SheetScrollColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier
            .weight(1f, fill = false)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 22.dp),
        content = content
    )
}
