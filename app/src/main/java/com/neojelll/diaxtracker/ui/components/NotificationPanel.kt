package com.neojelll.diaxtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.ui.theme.CardSurface
import com.neojelll.diaxtracker.ui.theme.GlucoIcons
import com.neojelll.diaxtracker.ui.theme.Ink

// Not yet clickable: there's no real notification content behind it yet.
@Composable
fun NotificationBellButton(modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(CardSurface),
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
