package com.neojelll.diaxtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoRadius
import com.neojelll.diaxtracker.ui.theme.GlukoSpacing
import com.neojelll.diaxtracker.ui.theme.GlukoType
import com.neojelll.diaxtracker.ui.theme.tabular
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

/** Bottom-sheet shell matching the design's geometry: 28dp top / 42dp bottom radius, scrim tap-to-dismiss. */
@Composable
fun GlukoSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    maxHeightFraction: Float = 0.9f,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    val noRipple = remember { MutableInteractionSource() }
    // Floor preserves the current gesture-nav look (real gesture inset is typically
    // <= 26.dp); the live navigationBars inset dominates only on 3-button nav, keeping
    // sheet content clear of the system bar. Mirrors the GlukoNavBar fix in NavGraph.kt.
    val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(GlukoColors.Scrim)
                .clickable(interactionSource = noRipple, indication = null, onClick = onDismiss)
        )
        Column(
            modifier = modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(maxHeightFraction)
                .wrapContentHeight(Alignment.Bottom)
                .clip(
                    RoundedCornerShape(
                        topStart = GlukoRadius.sheet, topEnd = GlukoRadius.sheet,
                        bottomStart = GlukoRadius.phone, bottomEnd = GlukoRadius.phone
                    )
                )
                .background(GlukoColors.Surface)
                .clickable(interactionSource = noRipple, indication = null) {}
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = maxOf(26.dp, navBarInset))
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(38.dp, 4.dp)
                    .clip(RoundedCornerShape(GlukoRadius.pill))
                    .background(GlukoColors.Border)
            )
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun SheetHeader(title: String, subtitle: String? = null, onClose: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Text(title, style = GlukoType.SheetTitle)
            if (subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = GlukoType.Note.copy(color = GlukoColors.TextLabel))
            }
        }
        CircleButton(32.dp, GlukoColors.Tile, onClose) {
            LucideIcon(LucidePaths.Close, 14.dp, strokeWidth = 2.2f)
        }
    }
    Spacer(Modifier.height(16.dp))
}

/** Date picker sheet with previous/next-month navigation (added beyond the static-month reference). */
@Composable
fun DateSheet(
    initialDate: LocalDate,
    onDone: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val locale = LocalConfiguration.current.locales[0]
    var visibleMonth by remember { mutableStateOf(YearMonth.from(initialDate)) }
    var selected by remember { mutableStateOf(initialDate) }
    val today = LocalDate.now()

    GlukoSheet(onDismiss) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            CircleButton(28.dp, GlukoColors.Tile, { visibleMonth = visibleMonth.minusMonths(1) }) {
                LucideIcon(LucidePaths.ChevronLeft, 13.dp, strokeWidth = 2.2f)
            }
            Text(
                visibleMonth.format(DateTimeFormatter.ofPattern("LLLL yyyy", locale))
                    .replaceFirstChar { it.uppercase(locale) },
                style = GlukoType.SheetTitle,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                textAlign = TextAlign.Center
            )
            CircleButton(28.dp, GlukoColors.Tile, {
                if (visibleMonth < YearMonth.from(today)) visibleMonth = visibleMonth.plusMonths(1)
            }) {
                LucideIcon(LucidePaths.ChevronRight, 13.dp, strokeWidth = 2.2f)
            }
        }
        Spacer(Modifier.height(14.dp))

        Row(Modifier.fillMaxWidth()) {
            val weekdays = DayOfWeek.entries
            weekdays.forEach {
                Text(
                    it.getDisplayName(JavaTextStyle.SHORT, locale).take(2).lowercase(locale),
                    style = GlukoType.CardLabel.copy(color = GlukoColors.TextTertiary),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(Modifier.height(6.dp))

        val firstDay = visibleMonth.atDay(1)
        val leadingBlanks = firstDay.dayOfWeek.value - DayOfWeek.MONDAY.value
        val daysInMonth = visibleMonth.lengthOfMonth()

        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.heightIn(max = 260.dp)) {
            items(leadingBlanks) { Box(Modifier.size(38.dp)) }
            items(daysInMonth) { index ->
                val date = visibleMonth.atDay(index + 1)
                val isSelected = date == selected
                val isFuture = date > today
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(GlukoRadius.strip))
                        .background(if (isSelected) GlukoColors.Ink else Color.Transparent)
                        .clickable(enabled = !isFuture) { selected = date },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (index + 1).toString(),
                        style = GlukoType.Body.copy(
                            color = when {
                                isSelected -> GlukoColors.Surface
                                isFuture -> GlukoColors.Placeholder
                                else -> GlukoColors.Ink
                            }
                        ).tabular
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        PrimaryButton(stringResource(R.string.sheet_done), onClick = { onDone(selected) })
    }
}

/** Time picker sheet: hour column 00-23, minute column 00-55 step 5 (nearest value pre-selected). */
@Composable
fun TimeSheet(
    initial: LocalTime,
    onDone: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    var hour by remember { mutableStateOf(initial.hour) }
    var minute by remember { mutableStateOf((initial.minute / 5) * 5) }

    GlukoSheet(onDismiss) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.sheet_time_title), style = GlukoType.SheetTitle, modifier = Modifier.weight(1f))
            Text("%02d:%02d".format(hour, minute), style = GlukoType.DisplayInsulin.tabular)
        }
        Spacer(Modifier.height(14.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            WheelColumn(stringResource(R.string.sheet_hours_label), (0..23).toList(), hour, Modifier.weight(1f)) { hour = it }
            WheelColumn(stringResource(R.string.sheet_minutes_label), (0..55 step 5).toList(), minute, Modifier.weight(1f)) { minute = it }
        }
        Spacer(Modifier.height(14.dp))
        PrimaryButton(stringResource(R.string.sheet_done), onClick = { onDone(LocalTime.of(hour, minute)) })
    }
}

@Composable
private fun WheelColumn(label: String, values: List<Int>, selected: Int, modifier: Modifier, onSelect: (Int) -> Unit) {
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    LaunchedEffect(Unit) {
        val index = values.indexOf(selected).coerceAtLeast(0)
        listState.scrollToItem(index)
    }
    Column(modifier) {
        Text(label, style = GlukoType.CardLabel, modifier = Modifier.padding(bottom = 7.dp))
        LazyColumn(
            state = listState,
            modifier = Modifier
                .height(184.dp)
                .clip(RoundedCornerShape(GlukoRadius.panel))
                .background(GlukoColors.Screen)
                .padding(6.dp)
        ) {
            items(values) { v ->
                val isSelected = v == selected
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(GlukoRadius.strip))
                        .background(if (isSelected) GlukoColors.Ink else Color.Transparent)
                        .clickable { onSelect(v) }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "%02d".format(v),
                        style = GlukoType.Body.copy(
                            fontSize = 14.sp,
                            color = if (isSelected) GlukoColors.Surface else GlukoColors.Ink
                        ).tabular
                    )
                }
            }
        }
    }
}

/** Full-screen photo preview. Shows the real photo when available, else the camera placeholder. */
@Composable
fun PhotoPreview(photoPath: String?, caption: String, onDismiss: () -> Unit) {
    val noRipple = remember { MutableInteractionSource() }
    Column(
        Modifier
            .fillMaxSize()
            .background(GlukoColors.PhotoOverlay)
            .clickable(interactionSource = noRipple, indication = null, onClick = onDismiss)
            .padding(26.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(GlukoColors.Tile),
            contentAlignment = Alignment.Center
        ) {
            if (photoPath != null) {
                AsyncImage(
                    model = File(photoPath),
                    contentDescription = stringResource(R.string.entry_photo),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp))
                )
            } else {
                LucideIcon(LucidePaths.Camera, 46.dp, GlukoColors.PhotoStubIcon, strokeWidth = 1.5f)
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(caption, style = GlukoType.Note.copy(color = GlukoColors.Surface))
    }
}
