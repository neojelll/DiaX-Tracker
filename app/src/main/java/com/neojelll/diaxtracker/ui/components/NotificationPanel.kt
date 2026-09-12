package com.neojelll.diaxtracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.ui.theme.CardDivider
import com.neojelll.diaxtracker.ui.theme.CardSurface
import com.neojelll.diaxtracker.ui.theme.FieldTile
import com.neojelll.diaxtracker.ui.theme.GlucoIcons
import com.neojelll.diaxtracker.ui.theme.Ink
import com.neojelll.diaxtracker.ui.theme.Kicker
import com.neojelll.diaxtracker.ui.theme.PageBackground
import com.neojelll.diaxtracker.ui.theme.SheetTitle
import com.neojelll.diaxtracker.ui.theme.TextLabel
import com.neojelll.diaxtracker.ui.theme.TextTertiary

private data class NotifItem(val kicker: String, val time: String, val text: String, val cta: String)

@Composable
fun NotificationBellButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(CardSurface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            GlucoIcons.Bell,
            contentDescription = stringResource(R.string.notifications_cd),
            tint = Ink,
            modifier = Modifier.size(19.dp)
        )
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 9.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(Ink)
        )
    }
}

@Composable
fun NotificationPanel(onClose: () -> Unit) {
    val items = listOf(
        NotifItem(
            stringResource(R.string.notif_1_kicker),
            stringResource(R.string.notif_1_time),
            stringResource(R.string.notif_1_text),
            stringResource(R.string.notif_1_cta)
        ),
        NotifItem(
            stringResource(R.string.notif_2_kicker),
            stringResource(R.string.notif_2_time),
            stringResource(R.string.notif_2_text),
            stringResource(R.string.notif_2_cta)
        ),
        NotifItem(
            stringResource(R.string.notif_3_kicker),
            stringResource(R.string.notif_3_time),
            stringResource(R.string.notif_3_text),
            stringResource(R.string.notif_3_cta)
        )
    )

    Row(Modifier.fillMaxWidth().fillMaxHeight()) {
        Spacer(Modifier.weight(1f))
        AnimatedVisibility(
            visible = true,
            enter = slideInHorizontally(tween(240)) { it } + fadeIn(tween(240)),
            exit = slideOutHorizontally(tween(200)) { it } + fadeOut(tween(160))
        ) {
            Column(
                Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .background(PageBackground)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.notifications_title), style = SheetTitle, color = Ink)
                    RoundIconButton(
                        icon = GlucoIcons.Close,
                        contentDescription = stringResource(R.string.close),
                        onClick = onClose,
                        background = CardSurface
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    items(items) { item -> NotificationCard(item) }
                    item {
                        Text(
                            stringResource(R.string.notif_footer),
                            fontSize = 11.5.sp,
                            color = TextTertiary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(item: NotifItem) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardSurface)
            .padding(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(item.kicker.uppercase(), style = Kicker, color = TextLabel)
            Text(item.time, fontSize = 10.5.sp, color = TextTertiary)
        }
        Text(
            item.text,
            fontSize = 13.5.sp,
            lineHeight = 20.sp,
            color = Ink,
            modifier = Modifier.padding(top = 8.dp)
        )
        Box(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .height(1.dp)
                .background(CardDivider)
        )
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(item.cta, fontSize = 12.5.sp, color = Ink)
            RoundIconButton(
                icon = GlucoIcons.ArrowRight,
                contentDescription = null,
                onClick = { },
                size = 26.dp,
                background = FieldTile
            )
        }
    }
}
