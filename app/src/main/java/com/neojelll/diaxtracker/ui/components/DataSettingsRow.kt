package com.neojelll.diaxtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.ui.theme.FieldTile
import com.neojelll.diaxtracker.ui.theme.GlucoIcons
import com.neojelll.diaxtracker.ui.theme.Ink
import com.neojelll.diaxtracker.ui.theme.TextTertiary

@Composable
fun DataSettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(vertical = 11.dp, horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f, fill = false)) {
            Box(Modifier.size(34.dp).clip(CircleShape).background(FieldTile), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Ink, modifier = Modifier.size(16.dp))
            }
            Column(Modifier.padding(start = 11.dp)) {
                Text(title, fontSize = 13.5.sp, color = Ink)
                Text(subtitle, fontSize = 11.5.sp, color = TextTertiary, modifier = Modifier.padding(top = 2.dp))
            }
        }
        Icon(GlucoIcons.ChevronRight, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(15.dp))
    }
}

// UI-only placeholder: toggles visually, not backed by a real nightly backup job.
@Composable
fun BackupToggleRow() {
    var enabled by remember { mutableStateOf(true) }
    Row(
        Modifier.fillMaxWidth().padding(vertical = 11.dp, horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(stringResource(R.string.backup_toggle_title), fontSize = 13.5.sp, color = Ink)
            Text(
                stringResource(if (enabled) R.string.backup_hint_on else R.string.backup_hint_off),
                fontSize = 11.5.sp,
                color = TextTertiary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Box(
            Modifier
                .width(46.dp)
                .padding(vertical = 3.dp)
                .clip(RoundedCornerShape(50))
                .background(if (enabled) Ink else com.neojelll.diaxtracker.ui.theme.BorderDashed)
                .clickable { enabled = !enabled }
                .padding(3.dp),
            contentAlignment = if (enabled) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Box(Modifier.size(21.dp).clip(CircleShape).background(Color.White))
        }
    }
}

// UI-only stub: confirms, but performs no destructive action yet.
@Composable
fun DeleteAllRecordsCard() {
    var confirming by remember { mutableStateOf(false) }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(com.neojelll.diaxtracker.ui.theme.CardShapeLarge)
            .background(com.neojelll.diaxtracker.ui.theme.CardSurface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(stringResource(R.string.delete_all_title), fontSize = 13.5.sp, color = Ink)
            Text(stringResource(R.string.delete_all_subtitle), fontSize = 11.5.sp, color = TextTertiary, modifier = Modifier.padding(top = 2.dp))
        }
        OutlinedPillButton(
            text = stringResource(R.string.delete),
            textColor = TextTertiary,
            onClick = { confirming = true }
        )
    }

    if (confirming) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { confirming = false },
            title = { Text(stringResource(R.string.delete_all_confirm_title)) },
            text = { Text(stringResource(R.string.delete_entry_confirm_text)) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { confirming = false }) {
                    Text(stringResource(R.string.delete), color = com.neojelll.diaxtracker.ui.theme.DangerRed)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { confirming = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
