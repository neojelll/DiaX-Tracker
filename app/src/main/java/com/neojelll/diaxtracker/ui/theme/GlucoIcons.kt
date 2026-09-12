package com.neojelll.diaxtracker.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// Lucide glyphs transcribed 1:1 from the SVG paths in Glucose Home.dc.html — the project
// only ships Material icons, which don't match. Each vector is a 24x24 viewport at its
// source stroke weight; Icon()'s size scales stroke + shape together, like the SVGs.
object GlucoIcons {
    val NavHome: ImageVector by lazy {
        outlineIcon(1.8f) {
            moveTo(3f, 10.6f); lineTo(12f, 3.4f); lineToRelative(9f, 7.2f)
            moveTo(5.6f, 9.6f); verticalLineTo(20f); horizontalLineToRelative(12.8f); verticalLineTo(9.6f)
        }
    }

    val NavFood: ImageVector by lazy {
        outlineIcon(1.8f) {
            moveTo(4f, 11.5f); horizontalLineToRelative(16f); arcToRelative(8f, 8f, 0f, false, true, -16f, 0f); close()
            moveTo(2.5f, 20.5f); horizontalLineToRelative(19f)
            moveTo(7.5f, 3.5f); curveToRelative(-1.6f, 1.3f, -1.6f, 2.7f, 0f, 4f); reflectiveCurveToRelative(1.6f, 2.7f, 0f, 4f)
            moveTo(12f, 3.5f); curveToRelative(-1.6f, 1.3f, -1.6f, 2.7f, 0f, 4f); reflectiveCurveToRelative(1.6f, 2.7f, 0f, 4f)
            moveTo(16.5f, 3.5f); curveToRelative(-1.6f, 1.3f, -1.6f, 2.7f, 0f, 4f); reflectiveCurveToRelative(1.6f, 2.7f, 0f, 4f)
        }
    }

    val NavHistory: ImageVector by lazy {
        outlineIcon(1.8f) {
            moveTo(3.5f, 12f); arcToRelative(8.5f, 8.5f, 0f, true, false, 2.6f, -6.1f)
            moveTo(3f, 4f); verticalLineToRelative(4f); horizontalLineToRelative(4f)
            moveTo(12f, 8f); verticalLineToRelative(4.2f); lineToRelative(3f, 1.8f)
        }
    }

    val NavSettings: ImageVector by lazy {
        outlineIcon(1.8f) {
            moveTo(4f, 6f); horizontalLineToRelative(10f)
            moveTo(18f, 6f); horizontalLineToRelative(2f)
            moveTo(4f, 12f); horizontalLineToRelative(2f)
            moveTo(10f, 12f); horizontalLineToRelative(10f)
            moveTo(4f, 18f); horizontalLineToRelative(10f)
            moveTo(18f, 18f); horizontalLineToRelative(2f)
            circle(16f, 6f, 2f)
            circle(8f, 12f, 2f)
            circle(16f, 18f, 2f)
        }
    }

    val Syringe: ImageVector by lazy {
        outlineIcon(2f) {
            moveTo(18f, 2f); lineToRelative(4f, 4f)
            moveTo(17f, 7f); lineToRelative(3f, -3f)
            moveTo(19f, 9f); lineTo(8.7f, 19.3f)
            curveToRelative(-1f, 1f, -2.5f, 1f, -3.4f, 0f)
            lineToRelative(-0.6f, -0.6f)
            curveToRelative(-1f, -1f, -1f, -2.5f, 0f, -3.4f)
            lineTo(15f, 5f)
            moveTo(9f, 11f); lineToRelative(4f, 4f)
            moveTo(5.6f, 18.4f); lineToRelative(-3.1f, 3.1f)
            moveTo(14f, 4f); lineToRelative(6f, 6f)
        }
    }

    val Droplet: ImageVector by lazy {
        outlineIcon(1.7f) {
            moveTo(12f, 3.2f)
            curveToRelative(3.4f, 3.8f, 6f, 6.9f, 6f, 10.1f)
            arcToRelative(6f, 6f, 0f, false, true, -12f, 0f)
            curveToRelative(0f, -3.2f, 2.6f, -6.3f, 6f, -10.1f)
            close()
        }
    }

    val Calendar: ImageVector by lazy {
        outlineIcon(1.8f) {
            roundedRect(3f, 4.5f, 18f, 17f, 4f)
            moveTo(8f, 2.5f); verticalLineToRelative(4f)
            moveTo(16f, 2.5f); verticalLineToRelative(4f)
            moveTo(3f, 10f); horizontalLineToRelative(18f)
        }
    }

    val ClockIcon: ImageVector by lazy {
        outlineIcon(1.8f) {
            circle(12f, 12f, 9f)
            moveTo(12f, 7.2f); verticalLineToRelative(5f); lineToRelative(3f, 2f)
        }
    }

    val ChevronDown: ImageVector by lazy {
        outlineIcon(2.2f) { moveTo(6f, 9.5f); lineToRelative(6f, 6f); lineToRelative(6f, -6f) }
    }

    val ChevronUp: ImageVector by lazy {
        outlineIcon(2.2f) { moveTo(6f, 14.5f); lineToRelative(6f, -6f); lineToRelative(6f, 6f) }
    }

    val Bell: ImageVector by lazy {
        outlineIcon(1.7f) {
            moveTo(6f, 8.5f)
            arcToRelative(6f, 6f, 0f, false, true, 12f, 0f)
            curveToRelative(0f, 6.5f, 2.5f, 8.5f, 2.5f, 8.5f)
            horizontalLineToRelative(-17f)
            reflectiveCurveTo(6f, 15f, 6f, 8.5f)
            moveTo(10.2f, 20.5f); arcToRelative(2f, 2f, 0f, false, false, 3.6f, 0f)
        }
    }

    val Plus: ImageVector by lazy {
        outlineIcon(2f) {
            moveTo(12f, 5.5f); verticalLineToRelative(13f)
            moveTo(5.5f, 12f); horizontalLineToRelative(13f)
        }
    }

    val Search: ImageVector by lazy {
        outlineIcon(1.9f) {
            circle(11f, 11f, 6.5f)
            moveTo(16f, 16f); lineToRelative(4.5f, 4.5f)
        }
    }

    val Close: ImageVector by lazy {
        outlineIcon(2.2f) {
            moveTo(6f, 6f); lineToRelative(12f, 12f)
            moveTo(18f, 6f); lineTo(6f, 18f)
        }
    }

    val Minus: ImageVector by lazy {
        outlineIcon(2.2f) { moveTo(6f, 12f); horizontalLineToRelative(12f) }
    }

    val Pencil: ImageVector by lazy {
        outlineIcon(1.8f) {
            moveTo(15.5f, 4.5f); lineTo(19.5f, 8.5f)
            moveTo(17.5f, 2.5f)
            arcToRelative(2f, 2f, 0f, false, true, 3f, 3f)
            lineTo(8f, 18f)
            lineToRelative(-4.5f, 1.5f)
            lineTo(5f, 15f)
            close()
        }
    }

    val Camera: ImageVector by lazy {
        outlineIcon(1.7f) {
            moveTo(14.5f, 4.5f); horizontalLineToRelative(-5f)
            lineTo(8f, 6.5f); horizontalLineTo(5f)
            arcToRelative(2f, 2f, 0f, false, false, -2f, 2f)
            verticalLineToRelative(9.5f)
            arcToRelative(2f, 2f, 0f, false, false, 2f, 2f)
            horizontalLineToRelative(14f)
            arcToRelative(2f, 2f, 0f, false, false, 2f, -2f)
            verticalLineTo(8.5f)
            arcToRelative(2f, 2f, 0f, false, false, -2f, -2f)
            horizontalLineToRelative(-3f)
            close()
            circle(12f, 13f, 3.2f)
        }
    }

    val ArrowRight: ImageVector by lazy {
        outlineIcon(2f) {
            moveTo(5f, 12f); horizontalLineToRelative(13f)
            moveTo(12.5f, 6.5f); lineTo(19f, 12f); lineToRelative(-6.5f, 5.5f)
        }
    }

    val ChevronRight: ImageVector by lazy {
        outlineIcon(2f) { moveTo(9.5f, 5.5f); lineTo(16f, 12f); lineToRelative(-6.5f, 6.5f) }
    }

    val ExpandCorner: ImageVector by lazy {
        outlineIcon(2.2f) {
            moveTo(4f, 9f); verticalLineTo(4f); horizontalLineToRelative(5f)
            moveTo(20f, 15f); verticalLineToRelative(5f); horizontalLineToRelative(-5f)
        }
    }

    val Document: ImageVector by lazy {
        outlineIcon(1.6f) {
            moveTo(13.5f, 3.5f); horizontalLineTo(7f)
            arcTo(1.5f, 1.5f, 0f, false, false, 5.5f, 5f)
            verticalLineToRelative(14f)
            arcToRelative(1.5f, 1.5f, 0f, false, false, 1.5f, 1.5f)
            horizontalLineToRelative(10f)
            arcToRelative(1.5f, 1.5f, 0f, false, false, 1.5f, -1.5f)
            verticalLineTo(8.5f)
            close()
            moveTo(13.5f, 3.5f); verticalLineToRelative(5f); horizontalLineToRelative(5f)
        }
    }

    val UploadTray: ImageVector by lazy {
        outlineIcon(1.8f) {
            moveTo(12f, 14.5f); verticalLineToRelative(-11f)
            moveTo(7.5f, 8f); lineTo(12f, 3.5f); lineTo(16.5f, 8f)
            moveTo(4.5f, 19.5f); horizontalLineToRelative(15f)
        }
    }

    val DownloadTray: ImageVector by lazy {
        outlineIcon(1.8f) {
            moveTo(12f, 3.5f); verticalLineToRelative(11f)
            moveTo(7.5f, 10f); lineTo(12f, 14.5f); lineTo(16.5f, 10f)
            moveTo(4.5f, 19.5f); horizontalLineToRelative(15f)
        }
    }

    val SortArrows: ImageVector by lazy {
        outlineIcon(1.9f) {
            moveTo(7f, 4.5f); verticalLineToRelative(15f)
            moveTo(3.5f, 16f); lineToRelative(3.5f, 3.5f); lineTo(10.5f, 16f)
            moveTo(17f, 19.5f); verticalLineToRelative(-15f)
            moveTo(13.5f, 8f); lineToRelative(3.5f, -3.5f); lineTo(20.5f, 8f)
        }
    }
}

private fun outlineIcon(
    strokeWidth: Float,
    block: PathBuilder.() -> Unit
): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).path(
    fill = null,
    stroke = SolidColor(Color.Black),
    strokeLineWidth = strokeWidth,
    strokeLineCap = StrokeCap.Round,
    strokeLineJoin = StrokeJoin.Round,
    pathBuilder = block
).build()

private fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
    moveTo(cx + r, cy)
    arcToRelative(r, r, 0f, false, true, -r, r)
    arcToRelative(r, r, 0f, false, true, -r, -r)
    arcToRelative(r, r, 0f, false, true, r, -r)
    arcToRelative(r, r, 0f, false, true, r, r)
    close()
}

private fun PathBuilder.roundedRect(x: Float, y: Float, w: Float, h: Float, r: Float) {
    moveTo(x + r, y)
    horizontalLineTo(x + w - r)
    arcTo(r, r, 0f, false, true, x + w, y + r)
    verticalLineTo(y + h - r)
    arcTo(r, r, 0f, false, true, x + w - r, y + h)
    horizontalLineTo(x + r)
    arcTo(r, r, 0f, false, true, x, y + h - r)
    verticalLineTo(y + r)
    arcTo(r, r, 0f, false, true, x + r, y)
    close()
}
