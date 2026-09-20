package com.neojelll.diaxtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoRadius
import com.neojelll.diaxtracker.ui.theme.GlukoType
import com.neojelll.diaxtracker.ui.theme.tabular

private data class NotificationItem(val categoryRes: Int, val timeRes: Int, val textRes: Int, val actionRes: Int, val iconPath: String)

private val sampleNotifications = listOf(
    NotificationItem(
        R.string.notif_category_report, R.string.notif_time_today,
        R.string.notif_text_report, R.string.notif_action_report, LucidePaths.Report
    ),
    NotificationItem(
        R.string.notif_category_pattern, R.string.notif_time_yesterday,
        R.string.notif_text_pattern, R.string.notif_action_pattern, LucidePaths.Trend
    ),
    NotificationItem(
        R.string.notif_category_reminder, R.string.notif_time_two_days_ago,
        R.string.notif_text_reminder, R.string.notif_action_reminder, LucidePaths.Clock
    )
)

/**
 * Notifications as a bottom sheet, matching the design's other sheets (date, time, presets,
 * export) instead of a full-height side panel whose close button sat out of thumb's reach.
 * The three sample rows and their action links are decorative (no report screens exist),
 * matching the design handoff intentionally.
 */
@Composable
fun NotificationSheet(onDismiss: () -> Unit) {
    GlukoSheet(onDismiss, maxHeightFraction = 0.76f) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.Bottom) {
                Text(stringResource(R.string.notif_panel_title), style = GlukoType.SheetTitle)
                Spacer(Modifier.width(8.dp))
                Text(
                    sampleNotifications.size.toString(),
                    style = GlukoType.CardLabel.copy(fontSize = 11.sp).tabular
                )
            }
            CircleButton(32.dp, GlukoColors.Tile, onDismiss) {
                LucideIcon(LucidePaths.Close, 14.dp, strokeWidth = 2.2f)
            }
        }
        Spacer(Modifier.height(14.dp))

        Column(
            Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            sampleNotifications.forEach { item -> NotificationRow(item) }
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.notif_panel_footer),
                style = GlukoType.CardLabel.copy(fontSize = 11.sp),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun NotificationRow(item: NotificationItem) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GlukoRadius.tile))
            .background(GlukoColors.Screen)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(Modifier.padding(top = 1.dp)) {
            CircleButton(28.dp, GlukoColors.Surface) {
                LucideIcon(item.iconPath, 14.dp, strokeWidth = 1.8f)
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Kicker(stringResource(item.categoryRes), Modifier.weight(1f))
                Text(stringResource(item.timeRes), style = GlukoType.CardLabel.tabular)
            }
            Spacer(Modifier.height(4.dp))
            Text(stringResource(item.textRes), style = GlukoType.Note.copy(color = GlukoColors.Ink))
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(item.actionRes), style = GlukoType.CardLabel.copy(fontSize = 11.sp))
                Spacer(Modifier.width(4.dp))
                LucideIcon(LucidePaths.ChevronRight, 11.dp, GlukoColors.TextLabel, strokeWidth = 2.2f)
            }
        }
    }
}
