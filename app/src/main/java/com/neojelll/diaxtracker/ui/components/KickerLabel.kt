package com.neojelll.diaxtracker.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import com.neojelll.diaxtracker.ui.theme.Kicker
import com.neojelll.diaxtracker.ui.theme.TextLabel

// Spec applies text-transform:uppercase to every kicker; Compose has no such style
// property, so the string itself must be uppercased at the call site.
@Composable
fun KickerLabel(text: String, modifier: Modifier = Modifier, color: Color = TextLabel) {
    val locale = LocalConfiguration.current.locales[0]
    Text(text.uppercase(locale), style = Kicker, color = color, modifier = modifier)
}
