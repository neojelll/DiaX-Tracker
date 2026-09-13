package com.neojelll.diaxtracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.R

/** Design tokens ported from the design_handoff_gluko package. Color is never used as accent. */
object GlukoColors {
    val Screen = Color(0xFFF7F6F5)
    val Surface = Color(0xFFFFFFFF)
    val Tile = Color(0xFFF4F2F0)
    val TileHover = Color(0xFFEBE8E5)
    val Ink = Color(0xFF0D0D0D)
    val InkHover = Color(0xFF333333)
    val InkSoft = Color(0xFF242424)
    val TextSecondary = Color(0xFF55534F)
    val TextLabel = Color(0xFF6E6B66)
    val TextTertiary = Color(0xFF7D7A74)
    val TextOnDark = Color(0xFFA9A5A0)
    val Placeholder = Color(0xFF9A968F)
    val Divider = Color(0xFFF0EEEC)
    val Border = Color(0xFFE6E3E0)
    val BorderHover = Color(0xFFCBC7C2)
    val BorderDashed = Color(0xFFDDD9D5)
    val TrackOnDark = Color(0xFF2E2E2E)
    val PhotoStub = Color(0xFFE6E3E0)
    val PhotoStubIcon = Color(0xFF9A968F)
    val PhotoOverlay = Color(0xB80D0D0D)
    val Scrim = Color(0x570D0D0D)
    val PresetCubeBorder = Color(0xFFEFEDEB)
}

/** Ruda variable font (wght axis); only weights 400/500 are used anywhere in the app. */
@OptIn(ExperimentalTextApi::class)
val Ruda = FontFamily(
    Font(
        R.font.ruda,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        R.font.ruda,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    )
)

private fun style(
    size: Int,
    weight: FontWeight = FontWeight.Normal,
    lineHeight: Float = size * 1.35f,
    letterSpacing: Float = 0f,
    color: Color = GlukoColors.Ink
) = TextStyle(
    fontFamily = Ruda,
    fontSize = size.sp,
    fontWeight = weight,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
    color = color,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    )
)

/** Typography scale from the 360x800 reference layout; sizes in sp equal the design's px values. */
object GlukoType {
    val ScreenTitle = style(24, FontWeight.Medium, 26.4f, -0.48f)
    val SheetTitle = style(18, FontWeight.Medium, 22f, -0.36f)
    val SheetTitleLarge = style(20, FontWeight.Medium, 24f, -0.4f)
    val DisplaySugar = style(34, FontWeight.Medium, 37f, -1.02f)
    val DisplayInsulin = style(24, FontWeight.Medium, 26f, -0.48f)
    val ValueLarge = style(20, FontWeight.Medium, 24f)
    val Value = style(17, FontWeight.Medium, 20f)
    val ValueSmall = style(15, FontWeight.Medium, 18f)
    val RecordTime = style(16, FontWeight.Medium, 19f, -0.16f)
    val ButtonPrimary = style(15, FontWeight.Medium, 18f)
    val Body = style(13, lineHeight = 18f).copy(fontSize = 13.5f.sp)
    val Label = style(13, color = GlukoColors.TextLabel)
    val Kicker = style(11, letterSpacing = 0.99f, color = GlukoColors.TextLabel)
    val CardLabel = style(11, color = GlukoColors.TextLabel).copy(fontSize = 10.5f.sp)
    val Unit = style(10, color = GlukoColors.TextLabel).copy(fontSize = 9.5f.sp)
    val Hint = style(12, color = GlukoColors.TextTertiary).copy(fontSize = 11.5f.sp)
    val Note = style(13, lineHeight = 19f, color = GlukoColors.TextSecondary).copy(fontSize = 12.5f.sp)
    val NavLabel = style(11).copy(fontSize = 10.5f.sp)
    val StatusBar = style(14, FontWeight.Medium).copy(fontSize = 13.5f.sp)
}

/** All numeric displays (sugar, doses, time, counters) use tabular figures. */
val TextStyle.tabular: TextStyle get() = copy(fontFeatureSettings = "tnum")

/** Spacing scale from the reference layout. */
object GlukoSpacing {
    val screenHorizontal = 14.dp
    val cardPadding = 16.dp
    val cardGap = 10.dp
    val itemGap = 9.dp
    val bottomInset = 130.dp
}

/** Corner radii from the reference layout. */
object GlukoRadius {
    val phone = 42.dp
    val sheet = 28.dp
    val card = 22.dp
    val record = 20.dp
    val panel = 18.dp
    val tile = 16.dp
    val field = 14.dp
    val strip = 12.dp
    val pill = 999.dp
}

@Composable
fun DiaXTrackerTheme(content: @Composable () -> Unit) {
    val scheme = lightColorScheme(
        primary = GlukoColors.Ink,
        onPrimary = GlukoColors.Surface,
        background = GlukoColors.Screen,
        onBackground = GlukoColors.Ink,
        surface = GlukoColors.Surface,
        onSurface = GlukoColors.Ink,
        surfaceVariant = GlukoColors.Tile,
        onSurfaceVariant = GlukoColors.TextLabel,
        outline = GlukoColors.Border,
        error = DangerRed,
        onError = GlukoColors.Surface
    )
    val typography = Typography(
        bodyMedium = GlukoType.Body,
        titleLarge = GlukoType.ScreenTitle,
        titleMedium = GlukoType.SheetTitle,
        labelSmall = GlukoType.Kicker
    )
    MaterialTheme(colorScheme = scheme, typography = typography, content = content)
}
