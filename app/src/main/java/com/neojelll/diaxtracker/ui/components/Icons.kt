package com.neojelll.diaxtracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import com.neojelll.diaxtracker.ui.theme.GlukoColors

/** Lucide (https://lucide.dev) icon path data, 24x24 grid, drawn via Canvas for pixel fidelity. */
object LucidePaths {
    const val Calendar = "M8 2.5v4M16 2.5v4M3 10h18M7 4.5h10a4 4 0 0 1 4 4v9a4 4 0 0 1-4 4H7a4 4 0 0 1-4-4v-9a4 4 0 0 1 4-4z"
    const val Clock = "M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18zM12 7.2v5l3 2"
    const val Droplet = "M12 3.2c3.4 3.8 6 6.9 6 10.1a6 6 0 0 1-12 0c0-3.2 2.6-6.3 6-10.1z"
    const val Syringe = "m18 2 4 4M17 7l3-3M19 9 8.7 19.3c-1 1-2.5 1-3.4 0l-.6-.6c-1-1-1-2.5 0-3.4L15 5m-6 6 4 4m-8 4-3.1 3.1M14 4l6 6"
    const val Camera = "M14.5 4.5h-5L8 6.5H5a2 2 0 0 0-2 2v9.5a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V8.5a2 2 0 0 0-2-2h-3zM12 16.1a3.1 3.1 0 1 0 0-6.2 3.1 3.1 0 0 0 0 6.2z"
    const val Bell = "M6 8.5a6 6 0 0 1 12 0c0 6.5 2.5 8.5 2.5 8.5h-17S6 15 6 8.5M10.2 20.5a2 2 0 0 0 3.6 0"
    const val Search = "M11 17.5a6.5 6.5 0 1 0 0-13 6.5 6.5 0 0 0 0 13zM16 16l4.5 4.5"
    const val Close = "M6 6l12 12M18 6L6 18"
    const val Plus = "M12 6v12M6 12h12"
    const val Minus = "M6 12h12"
    const val ChevronRight = "M9.5 5.5 16 12l-6.5 6.5"
    const val ChevronDown = "M6 9.5l6 6 6-6"
    const val ChevronUp = "M6 14.5l6-6 6 6"
    const val ChevronLeft = "M14.5 5.5 8 12l6.5 6.5"
    const val ArrowRight = "M4.5 12h15M13 5.5l6.5 6.5-6.5 6.5"
    const val Expand = "M4 9V4h5M20 15v5h-5"
    const val History = "M3.5 12a8.5 8.5 0 1 0 2.6-6.1M3 4v4h4M12 8v4.2l3 1.8"
    const val Dish = "M4 11.5h16a8 8 0 0 1-16 0zM2.5 20.5h19M7.5 3.5c-1.6 1.3-1.6 2.7 0 4s1.6 2.7 0 4M12 3.5c-1.6 1.3-1.6 2.7 0 4s1.6 2.7 0 4M16.5 3.5c-1.6 1.3-1.6 2.7 0 4s1.6 2.7 0 4"
    const val Settings = "M4 6h10M18 6h2M4 12h2M10 12h10M4 18h10M18 18h2M16 8a2 2 0 1 0 0-4 2 2 0 0 0 0 4zM8 14a2 2 0 1 0 0-4 2 2 0 0 0 0 4zM16 20a2 2 0 1 0 0-4 2 2 0 0 0 0 4z"
    const val Home = "M3.5 10.5 12 3.5l8.5 7M5.5 9.2V19a1.5 1.5 0 0 0 1.5 1.5h10a1.5 1.5 0 0 0 1.5-1.5V9.2"
    const val Download = "M12 3.5v11M7.5 10 12 14.5 16.5 10M4.5 19.5h15"
    const val Upload = "M12 14.5v-11M7.5 8 12 3.5 16.5 8M4.5 19.5h15"
    const val File = "M13.5 3.5H7A1.5 1.5 0 0 0 5.5 5v14A1.5 1.5 0 0 0 7 20.5h10a1.5 1.5 0 0 0 1.5-1.5V8.5zM13.5 3.5v5h5"
    const val Sort = "M7 4.5v15M3.5 16l3.5 3.5L10.5 16M17 19.5v-15M13.5 8l3.5-3.5L20.5 8"
    const val AlertTriangle = "M12 9v4.5M12 17h.01M10.3 3.9 2 18a1.6 1.6 0 0 0 1.4 2.4h17.2A1.6 1.6 0 0 0 22 18L13.7 3.9a1.6 1.6 0 0 0-2.8 0z"
    const val Report = "M7 3.5h7l4.5 4.5v12a1.5 1.5 0 0 1-1.5 1.5H7a1.5 1.5 0 0 1-1.5-1.5V5A1.5 1.5 0 0 1 7 3.5zM14 3.5v5h4.5M9 13h6M9 16.5h4"
    const val Image = "M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2zM9 7a2 2 0 1 0 0 4 2 2 0 0 0 0-4zM21 15l-3.086-3.086a2 2 0 0 0-2.828 0L6 21"
    const val Trash = "M3 6h18M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2M10 11v6M14 11v6"
    const val Trend = "M3.5 17.5 9 11l3.5 3.5L20.5 6M15.5 6h5v5"
}

/** Draws a Lucide icon with the given stroke thickness, scaled to the target size. */
@Composable
fun LucideIcon(
    path: String,
    size: Dp,
    color: Color = GlukoColors.Ink,
    strokeWidth: Float = 1.8f,
    modifier: Modifier = Modifier
) {
    Canvas(modifier.size(size)) {
        val scaleFactor = this.size.minDimension / 24f
        val outline = PathParser().parsePathString(path).toPath()
        scale(scaleFactor, Offset.Zero) {
            drawPath(
                path = outline,
                color = color,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}
