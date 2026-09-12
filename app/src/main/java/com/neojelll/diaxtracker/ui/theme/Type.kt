package com.neojelll.diaxtracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.R

val RudaFontFamily = FontFamily(
    Font(R.font.ruda_regular, weight = FontWeight.Normal),
    Font(R.font.ruda_medium, weight = FontWeight.Medium)
)

// Screen title: "Привет!" / "Настройки" / etc.
val ScreenTitle = TextStyle(
    fontFamily = RudaFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 24.sp,
    lineHeight = 26.4.sp,
    letterSpacing = (-0.02).em
)

// Sheet title: bottom-sheet / side-panel headers.
val SheetTitle = TextStyle(
    fontFamily = RudaFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 18.sp,
    letterSpacing = (-0.02).em
)

// Large tabular value: blood sugar input on Home.
val BigValue = TextStyle(
    fontFamily = RudaFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 34.sp,
    letterSpacing = (-0.03).em
)

// Large tabular value inside the dark insulin plaque.
val InsulinBigValue = TextStyle(
    fontFamily = RudaFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 24.sp,
    letterSpacing = (-0.02).em
)

// Tile value: short/long insulin tiles, history stat values.
val TileValue = TextStyle(
    fontFamily = RudaFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 19.sp
)

// Record time in History rows.
val RecordTime = TextStyle(
    fontFamily = RudaFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    letterSpacing = (-0.01).em
)

// Primary pill button label.
val ButtonLabel = TextStyle(
    fontFamily = RudaFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 15.sp
)

// Field text / list row text.
val FieldText = TextStyle(
    fontFamily = RudaFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 13.5.sp
)

// Field label (above an input, e.g. "Нижняя граница").
val FieldLabel = TextStyle(
    fontFamily = RudaFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp
)

// Uppercase kicker: "ПРЕСЕТЫ", "ПРОДУКТЫ", section eyebrows.
val Kicker = TextStyle(
    fontFamily = RudaFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 11.sp,
    letterSpacing = 0.09.em
)

// Caption inside a History card (comment, preset composition).
val HistoryCaption = TextStyle(
    fontFamily = RudaFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 10.5.sp
)

// Unit label: "ммоль/л", "ед".
val UnitLabel = TextStyle(
    fontFamily = RudaFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 10.sp
)

// Bottom-nav item label.
val NavLabel = TextStyle(
    fontFamily = RudaFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 10.5.sp
)

// Status-bar clock / tabular numeric row.
val StatusBarText = TextStyle(
    fontFamily = RudaFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 13.5.sp
)

val DiaXTrackerTypography = Typography(
    displayLarge = ScreenTitle,
    displayMedium = ScreenTitle,
    displaySmall = ScreenTitle,
    headlineLarge = ScreenTitle,
    headlineMedium = ScreenTitle,
    headlineSmall = ScreenTitle,
    titleLarge = SheetTitle,
    titleMedium = SheetTitle,
    titleSmall = TileValue,
    bodyLarge = FieldText,
    bodyMedium = FieldText,
    bodySmall = FieldLabel,
    labelLarge = ButtonLabel,
    labelMedium = HistoryCaption,
    labelSmall = Kicker
)
