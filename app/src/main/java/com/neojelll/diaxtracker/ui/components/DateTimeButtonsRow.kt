package com.neojelll.diaxtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.ui.theme.FieldTile
import com.neojelll.diaxtracker.ui.theme.GlucoIcons
import com.neojelll.diaxtracker.ui.theme.Ink
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DateTimeButtonsRow(
    date: LocalDate,
    time: LocalTime,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val locale = Locale.getDefault()
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        DateTimePill(
            icon = GlucoIcons.Calendar,
            text = date.format(DateTimeFormatter.ofPattern("d MMM", locale)).removeSuffix("."),
            contentDescription = stringResource(R.string.entry_date_label),
            onClick = onDateClick,
            modifier = Modifier.weight(1f)
        )
        DateTimePill(
            icon = GlucoIcons.ClockIcon,
            text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
            contentDescription = stringResource(R.string.entry_time_label),
            onClick = onTimeClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DateTimePill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(FieldTile)
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp, horizontal = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = contentDescription, tint = Ink, modifier = Modifier.padding(end = 8.dp).size(15.dp))
        Text(text, fontSize = 13.5.sp, color = Ink)
    }
}
