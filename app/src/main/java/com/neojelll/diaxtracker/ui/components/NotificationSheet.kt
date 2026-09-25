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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.data.DiaryEntryProduct
import com.neojelll.diaxtracker.ui.notifications.yesterdayMeals
import com.neojelll.diaxtracker.ui.screens.formatAmount
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoRadius
import com.neojelll.diaxtracker.ui.theme.GlukoType
import com.neojelll.diaxtracker.ui.theme.tabular

/**
 * Notifications as a bottom sheet, matching the design's other sheets (date, time, presets,
 * export) instead of a full-height side panel whose close button sat out of thumb's reach.
 * For now there is one kind: a reminder of what was eaten at this time yesterday, shown only
 * within an hour of yesterday's meal.
 */
@Composable
fun NotificationSheet(viewModel: DiaryViewModel, onDismiss: () -> Unit) {
    val entries by viewModel.entries.collectAsState()
    val meals = remember(entries) { yesterdayMeals(entries, LocalDateTime.now()) }
    val xeFormat = stringResource(R.string.bread_units_value_format)

    GlukoSheet(onDismiss, maxHeightFraction = 0.76f) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.Bottom) {
                Text(stringResource(R.string.notif_panel_title), style = GlukoType.SheetTitle)
                if (meals.isNotEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    Text(meals.size.toString(), style = GlukoType.CardLabel.copy(fontSize = 11.sp).tabular)
                }
            }
            CircleButton(32.dp, GlukoColors.Tile, onDismiss) {
                LucideIcon(LucidePaths.Close, 14.dp, strokeWidth = 2.2f)
            }
        }
        Spacer(Modifier.height(14.dp))

        if (meals.isEmpty()) {
            Text(
                stringResource(R.string.notif_empty),
                style = GlukoType.CardLabel.copy(fontSize = 12.sp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp),
                textAlign = TextAlign.Center
            )
        } else {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                meals.forEach { meal -> YesterdayMealRow(viewModel, meal, xeFormat) }
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun YesterdayMealRow(viewModel: DiaryViewModel, meal: DiaryEntry, xeFormat: String) {
    var products by remember(meal.id) { mutableStateOf<List<DiaryEntryProduct>>(emptyList()) }
    LaunchedEffect(meal.id) { products = viewModel.getEntryProducts(meal.id) }

    val what = products.takeIf { it.isNotEmpty() }?.joinToString { it.name } ?: meal.mealLabel.orEmpty()
    val amount = meal.breadUnits?.let { xeFormat.format(formatAmount(it)) }
    val food = listOf(what, amount.orEmpty()).filter { it.isNotEmpty() }.joinToString(" · ")
    val time = meal.createdAt.format(DateTimeFormatter.ofPattern("HH:mm"))

    NotificationRow(
        category = stringResource(R.string.notif_category_report),
        time = time,
        text = stringResource(R.string.notif_yesterday_meal, time, food),
        iconPath = LucidePaths.Report
    )
}

@Composable
private fun NotificationRow(category: String, time: String, text: String, iconPath: String) {
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
                LucideIcon(iconPath, 14.dp, strokeWidth = 1.8f)
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Kicker(category, Modifier.weight(1f))
                Text(time, style = GlukoType.CardLabel.tabular)
            }
            Spacer(Modifier.height(4.dp))
            Text(text, style = GlukoType.Note.copy(color = GlukoColors.Ink))
        }
    }
}
