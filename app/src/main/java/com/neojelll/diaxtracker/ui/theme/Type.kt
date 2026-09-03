package com.neojelll.diaxtracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.neojelll.diaxtracker.R

@OptIn(ExperimentalTextApi::class)
val RubikFontFamily = FontFamily(
    Font(R.font.rubik, weight = FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.rubik, weight = FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.rubik, weight = FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.rubik, weight = FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700)))
)

val DiaXTrackerTypography = Typography().let { default ->
    Typography(
        displayLarge = default.displayLarge.copy(fontFamily = RubikFontFamily),
        displayMedium = default.displayMedium.copy(fontFamily = RubikFontFamily),
        displaySmall = default.displaySmall.copy(fontFamily = RubikFontFamily),
        headlineLarge = default.headlineLarge.copy(fontFamily = RubikFontFamily),
        headlineMedium = default.headlineMedium.copy(fontFamily = RubikFontFamily),
        headlineSmall = default.headlineSmall.copy(fontFamily = RubikFontFamily),
        titleLarge = default.titleLarge.copy(fontFamily = RubikFontFamily, fontWeight = FontWeight.SemiBold),
        titleMedium = default.titleMedium.copy(fontFamily = RubikFontFamily, fontWeight = FontWeight.SemiBold),
        titleSmall = default.titleSmall.copy(fontFamily = RubikFontFamily, fontWeight = FontWeight.Medium),
        bodyLarge = default.bodyLarge.copy(fontFamily = RubikFontFamily),
        bodyMedium = default.bodyMedium.copy(fontFamily = RubikFontFamily),
        bodySmall = default.bodySmall.copy(fontFamily = RubikFontFamily),
        labelLarge = default.labelLarge.copy(fontFamily = RubikFontFamily, fontWeight = FontWeight.Medium),
        labelMedium = default.labelMedium.copy(fontFamily = RubikFontFamily, fontWeight = FontWeight.Medium),
        labelSmall = default.labelSmall.copy(fontFamily = RubikFontFamily, fontWeight = FontWeight.Medium)
    )
}
