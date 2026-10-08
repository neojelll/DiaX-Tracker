package com.neojelll.diaxtracker.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.time.LocalDateTime
import kotlinx.coroutines.delay

private const val MINUTE_MILLIS = 60_000L

/** The current time, refreshed at the turn of every minute, so a shown "now" doesn't freeze while the screen stays open. */
@Composable
fun rememberCurrentMinute(): LocalDateTime {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(MINUTE_MILLIS - System.currentTimeMillis() % MINUTE_MILLIS)
            now = LocalDateTime.now()
        }
    }
    return now
}
