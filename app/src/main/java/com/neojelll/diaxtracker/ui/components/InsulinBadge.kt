package com.neojelll.diaxtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.DiaryEntry
import com.neojelll.diaxtracker.ui.theme.BorderOnDark
import com.neojelll.diaxtracker.ui.theme.CollapsedPlaqueShape
import com.neojelll.diaxtracker.ui.theme.GlucoIcons
import com.neojelll.diaxtracker.ui.theme.Ink
import com.neojelll.diaxtracker.ui.theme.InkOnDarkTile
import com.neojelll.diaxtracker.ui.theme.OnDarkSecondary
import com.neojelll.diaxtracker.ui.theme.PlaqueShape
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val IOB_DURATION: Duration = Duration.ofHours(4)

@Composable
fun InsulinBadge(entry: DiaryEntry) {
    var expanded by rememberSaveable(entry.id) { mutableStateOf(true) }
    var now by remember(entry.id) { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(entry.id) {
        while (true) {
            now = LocalDateTime.now()
            delay(30_000L)
        }
    }

    val elapsed = Duration.between(entry.createdAt, now)
    val remaining = (IOB_DURATION - elapsed).let { if (it.isNegative) Duration.ZERO else it }
    val remainingMinutes = remaining.toMinutes()
    val progress = (remainingMinutes.toFloat() / IOB_DURATION.toMinutes()).coerceIn(0f, 1f)
    val remainingLabel = if (remainingMinutes >= 60) {
        stringResource(R.string.duration_hours_minutes, remainingMinutes / 60, remainingMinutes % 60)
    } else {
        stringResource(R.string.duration_minutes, remainingMinutes)
    }
    val units = entry.shortInsulinDose ?: return
    val unitsLabel = if (units == units.toInt().toFloat()) units.toInt().toString() else units.toString()
    val fromLabel = entry.createdAt.format(DateTimeFormatter.ofPattern("HH:mm"))

    if (expanded) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .padding(top = 8.dp)
                .clip(PlaqueShape)
                .background(Ink)
                .clickable { expanded = false }
                .padding(horizontal = 15.dp, vertical = 13.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconCircle(GlucoIcons.Syringe)
                    Text(
                        stringResource(R.string.insulin_active_title),
                        fontSize = 13.sp,
                        color = Color.White,
                        modifier = Modifier.padding(start = 9.dp)
                    )
                }
                IconCircle(GlucoIcons.ChevronUp)
            }

            Row(Modifier.padding(top = 11.dp), verticalAlignment = Alignment.Bottom) {
                Text(unitsLabel, fontSize = 24.sp, fontWeight = FontWeight.Medium, color = Color.White)
                Text(
                    stringResource(R.string.insulin_units_active),
                    fontSize = 11.sp,
                    color = OnDarkSecondary,
                    modifier = Modifier.padding(start = 5.dp)
                )
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 8.dp)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(BorderOnDark)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(progress)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.insulin_from_format, fromLabel), fontSize = 11.sp, color = OnDarkSecondary)
                Text(stringResource(R.string.insulin_time_left_format, remainingLabel), fontSize = 11.sp, color = OnDarkSecondary)
            }
        }
    } else {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .padding(top = 8.dp)
                .clip(CollapsedPlaqueShape)
                .background(Ink)
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                GlucoIcons.Syringe,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
            Row(Modifier.padding(start = 8.dp).weight(1f, fill = false), verticalAlignment = Alignment.Bottom) {
                Text("$unitsLabel ", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.White)
                Text(stringResource(R.string.insulin_units_active), fontSize = 9.5.sp, color = OnDarkSecondary)
            }
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(R.string.insulin_time_left_format, remainingLabel),
                fontSize = 10.5.sp,
                color = OnDarkSecondary
            )
            Icon(
                GlucoIcons.ChevronDown,
                contentDescription = null,
                tint = OnDarkSecondary,
                modifier = Modifier.padding(start = 6.dp).size(11.dp)
            )
        }
    }
}

@Composable
private fun IconCircle(icon: ImageVector) {
    Box(
        Modifier.size(26.dp).clip(CircleShape).background(InkOnDarkTile),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
    }
}
