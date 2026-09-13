package com.neojelll.diaxtracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoRadius
import com.neojelll.diaxtracker.ui.theme.GlukoSpacing
import com.neojelll.diaxtracker.ui.theme.GlukoType

private data class NotificationItem(val categoryRes: Int, val timeRes: Int, val textRes: Int, val actionRes: Int)

private val sampleNotifications = listOf(
    NotificationItem(
        R.string.notif_category_report, R.string.notif_time_today,
        R.string.notif_text_report, R.string.notif_action_report
    ),
    NotificationItem(
        R.string.notif_category_pattern, R.string.notif_time_yesterday,
        R.string.notif_text_pattern, R.string.notif_action_pattern
    ),
    NotificationItem(
        R.string.notif_category_reminder, R.string.notif_time_two_days_ago,
        R.string.notif_text_reminder, R.string.notif_action_reminder
    )
)

/**
 * Static notifications side panel. The three sample cards and their action links are decorative
 * (no report screens exist), matching the design handoff intentionally.
 */
@Composable
fun NotificationPanel(onDismiss: () -> Unit) {
    val noRipple = remember { MutableInteractionSource() }

    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(GlukoColors.Scrim)
                .clickable(interactionSource = noRipple, indication = null, onClick = onDismiss)
        )
        AnimatedVisibility(
            visible = true,
            enter = slideInHorizontally(tween(240)) { it },
            exit = slideOutHorizontally(tween(240)) { it },
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Column(
                Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .background(GlukoColors.Screen)
                    .clickable(interactionSource = noRipple, indication = null) {}
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 22.dp)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.notif_panel_title), style = GlukoType.SheetTitleLarge, modifier = Modifier.weight(1f))
                    CircleButton(32.dp, GlukoColors.Surface, onDismiss) {
                        LucideIcon(LucidePaths.Close, 14.dp, strokeWidth = 2.2f)
                    }
                }
                Spacer(Modifier.height(14.dp))

                sampleNotifications.forEach { item ->
                    GlukoCard(padding = 14.dp, radius = GlukoRadius.record) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Kicker(stringResource(item.categoryRes), Modifier.weight(1f))
                            Text(stringResource(item.timeRes), style = GlukoType.CardLabel.copy(color = GlukoColors.TextTertiary))
                        }
                        Spacer(Modifier.height(9.dp))
                        Text(stringResource(item.textRes), style = GlukoType.Body)
                        Spacer(Modifier.height(11.dp))
                        GlukoDivider()
                        Spacer(Modifier.height(11.dp))
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(item.actionRes), style = GlukoType.Body, modifier = Modifier.weight(1f))
                            CircleButton(26.dp, GlukoColors.Tile) {
                                LucideIcon(LucidePaths.ArrowRight, 13.dp, strokeWidth = 2f)
                            }
                        }
                    }
                    Spacer(Modifier.height(GlukoSpacing.itemGap))
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.notif_panel_footer),
                    style = GlukoType.Hint,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
