package com.neojelll.diaxtracker.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoType

/**
 * Blocking, one-time notice shown before the app can be used at all (gates NavGraph entirely -
 * see DiaryViewModel.disclaimerAccepted). No close button and no dismiss-on-scrim-tap: the point
 * is that it has to actually be acknowledged, not just be dismissible like every other sheet.
 */
@Composable
fun DisclaimerOverlay(onAccept: () -> Unit) {
    GlukoSheet(onDismiss = {}, maxHeightFraction = 0.85f) {
        Text(stringResource(R.string.disclaimer_title), style = GlukoType.SheetTitleLarge)
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.disclaimer_body), style = GlukoType.Note.copy(color = GlukoColors.TextSecondary))
        Spacer(Modifier.height(18.dp))
        PrimaryButton(stringResource(R.string.disclaimer_accept), onClick = onAccept)
    }
}
