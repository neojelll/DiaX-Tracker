package com.neojelll.diaxtracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.neojelll.diaxtracker.R

val JonesFontFamily = FontFamily(
    Font(R.font.jones_book, weight = FontWeight.Normal),
    Font(R.font.jones_medium, weight = FontWeight.Medium),
    Font(R.font.jones_bold, weight = FontWeight.SemiBold),
    Font(R.font.jones_black, weight = FontWeight.Bold)
)

val DiaXTrackerTypography = Typography().let { default ->
    Typography(
        displayLarge = default.displayLarge.copy(fontFamily = JonesFontFamily),
        displayMedium = default.displayMedium.copy(fontFamily = JonesFontFamily),
        displaySmall = default.displaySmall.copy(fontFamily = JonesFontFamily),
        headlineLarge = default.headlineLarge.copy(fontFamily = JonesFontFamily),
        headlineMedium = default.headlineMedium.copy(fontFamily = JonesFontFamily),
        headlineSmall = default.headlineSmall.copy(fontFamily = JonesFontFamily),
        titleLarge = default.titleLarge.copy(fontFamily = JonesFontFamily, fontWeight = FontWeight.SemiBold),
        titleMedium = default.titleMedium.copy(fontFamily = JonesFontFamily, fontWeight = FontWeight.SemiBold),
        titleSmall = default.titleSmall.copy(fontFamily = JonesFontFamily, fontWeight = FontWeight.Medium),
        bodyLarge = default.bodyLarge.copy(fontFamily = JonesFontFamily),
        bodyMedium = default.bodyMedium.copy(fontFamily = JonesFontFamily),
        bodySmall = default.bodySmall.copy(fontFamily = JonesFontFamily),
        labelLarge = default.labelLarge.copy(fontFamily = JonesFontFamily, fontWeight = FontWeight.Medium),
        labelMedium = default.labelMedium.copy(fontFamily = JonesFontFamily, fontWeight = FontWeight.Medium),
        labelSmall = default.labelSmall.copy(fontFamily = JonesFontFamily, fontWeight = FontWeight.Medium)
    )
}
