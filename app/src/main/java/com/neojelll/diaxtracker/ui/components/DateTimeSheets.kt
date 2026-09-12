package com.neojelll.diaxtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.ui.theme.CollapsedPlaqueShape
import com.neojelll.diaxtracker.ui.theme.FieldText
import com.neojelll.diaxtracker.ui.theme.Ink
import com.neojelll.diaxtracker.ui.theme.Kicker
import com.neojelll.diaxtracker.ui.theme.PageBackground
import com.neojelll.diaxtracker.ui.theme.TextTertiary
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

@Composable
fun DateSheet(
    initialDate: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var displayedMonth by remember { mutableStateOf(YearMonth.from(initialDate)) }
    var selected by remember { mutableStateOf(initialDate) }
    val locale = LocalConfiguration.current.locales[0]
    val weekdayLabels = stringArrayResource(R.array.weekday_short_labels)
    val today = LocalDate.now()

    BottomSheetSurface(modifier = modifier) {
        SheetHandle()
        Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RoundIconButton(
                        icon = com.neojelll.diaxtracker.ui.theme.GlucoIcons.ChevronRight,
                        contentDescription = null,
                        onClick = { displayedMonth = displayedMonth.minusMonths(1) },
                        size = 26.dp,
                        modifier = Modifier.rotate(180f)
                    )
                    Text(
                        stringResource(
                            R.string.date_sheet_title_format,
                            displayedMonth.month.getDisplayName(JavaTextStyle.FULL, locale)
                                .replaceFirstChar { it.uppercase(locale) },
                            displayedMonth.year
                        ),
                        fontSize = 17.sp,
                        color = Ink,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    RoundIconButton(
                        icon = com.neojelll.diaxtracker.ui.theme.GlucoIcons.ChevronRight,
                        contentDescription = null,
                        onClick = { displayedMonth = displayedMonth.plusMonths(1) },
                        size = 26.dp
                    )
                }
                Text(
                    stringResource(R.string.close),
                    style = FieldText,
                    color = com.neojelll.diaxtracker.ui.theme.TextLabel,
                    modifier = Modifier.clickable(onClick = onDismiss)
                )
            }

            Row(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 6.dp)) {
                weekdayLabels.forEach { label ->
                    Text(
                        label,
                        style = Kicker.copy(fontSize = 10.5.sp, letterSpacing = 0.sp),
                        color = TextTertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            val firstOfMonth = displayedMonth.atDay(1)
            val leadingBlanks = (firstOfMonth.dayOfWeek.value - 1).coerceAtLeast(0)
            val daysInMonth = displayedMonth.lengthOfMonth()
            val cells = leadingBlanks + daysInMonth

            for (row in 0 until (cells + 6) / 7) {
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val dayNumber = cellIndex - leadingBlanks + 1
                        Box(Modifier.weight(1f).aspectRatio(1f).padding(2.dp), contentAlignment = Alignment.Center) {
                            if (dayNumber in 1..daysInMonth) {
                                val date = displayedMonth.atDay(dayNumber)
                                val isSelected = date == selected
                                val isFuture = date.isAfter(today)
                                Box(
                                    Modifier
                                        .size(38.dp)
                                        .clip(CollapsedPlaqueShape)
                                        .background(if (isSelected) Ink else Color.Transparent)
                                        .clickable(enabled = !isFuture) { selected = date },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        dayNumber.toString(),
                                        fontSize = 13.5.sp,
                                        color = when {
                                            isSelected -> Color.White
                                            isFuture -> TextTertiary.copy(alpha = 0.5f)
                                            else -> Ink
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            PrimaryPillButton(
                text = stringResource(R.string.done),
                onClick = { onConfirm(selected) },
                modifier = Modifier.padding(top = 18.dp)
            )
        }
    }
}

@Composable
fun TimeSheet(
    initialTime: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    var hour by remember { mutableStateOf(initialTime.hour) }
    var minute by remember { mutableStateOf((initialTime.minute / 5) * 5) }

    BottomSheetSurface(modifier = modifier) {
        SheetHandle()
        Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.time_sheet_title), fontSize = 17.sp, color = Ink)
                Text(
                    "%02d:%02d".format(hour, minute),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = Ink
                )
            }

            Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                WheelColumn(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.hours_kicker),
                    values = (0..23).toList(),
                    selected = hour,
                    onSelect = { hour = it }
                )
                WheelColumn(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.minutes_kicker),
                    values = (0..55 step 5).toList(),
                    selected = minute,
                    onSelect = { minute = it }
                )
            }

            PrimaryPillButton(
                text = stringResource(R.string.done),
                onClick = { onConfirm(LocalTime.of(hour, minute)) },
                modifier = Modifier.padding(top = 20.dp)
            )
        }
    }
}

@Composable
private fun WheelColumn(
    label: String,
    values: List<Int>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    LaunchedEffect(Unit) {
        val index = values.indexOf(selected).coerceAtLeast(0)
        listState.scrollToItem((index - 2).coerceAtLeast(0))
    }

    Column(modifier) {
        KickerLabel(label, modifier = Modifier.padding(bottom = 8.dp))
        LazyColumn(
            modifier = Modifier
                .height(184.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(PageBackground)
                .padding(6.dp),
            state = listState
        ) {
            items(values) { value ->
                val isSelected = value == selected
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(CollapsedPlaqueShape)
                        .background(if (isSelected) Ink else Color.Transparent)
                        .clickable { onSelect(value) }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "%02d".format(value),
                        fontSize = 14.sp,
                        color = if (isSelected) Color.White else Ink
                    )
                }
            }
        }
    }
}
