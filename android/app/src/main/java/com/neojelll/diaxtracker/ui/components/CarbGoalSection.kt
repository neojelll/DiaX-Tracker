package com.neojelll.diaxtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.CarbUnit
import com.neojelll.diaxtracker.data.MAX_CARB_GOAL_GRAMS
import com.neojelll.diaxtracker.data.carbGoalStepGrams
import com.neojelll.diaxtracker.data.formatXe
import com.neojelll.diaxtracker.data.stepCarbGoal
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoRadius
import com.neojelll.diaxtracker.ui.theme.GlukoType
import com.neojelll.diaxtracker.ui.theme.tabular
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel
import kotlin.math.roundToInt

/**
 * The daily carbohydrate goal: a switch, then the goal itself - typed straight into the field or
 * nudged with −/+ - in the carb unit from the settings, with the other unit's equivalent beside it.
 */
@Composable
fun CarbGoalSection(viewModel: DiaryViewModel) {
    val goal by viewModel.carbGoal.collectAsState()
    val display by viewModel.carbDisplay.collectAsState()

    // The field keeps what's being typed ("12." mid-entry); only a unit change or a −/+ rewrites it.
    var text by remember(display) { mutableStateOf(goal.grams?.takeIf { it > 0f }?.let(display::format).orEmpty()) }
    fun step(steps: Int) {
        val grams = stepCarbGoal(display.parse(text) ?: goal.grams, steps, display)
        text = if (grams > 0f) display.format(grams) else ""
        viewModel.setCarbGoal(goal.copy(grams = grams))
    }

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.carb_goal_title), style = GlukoType.Label, modifier = Modifier.weight(1f))
        GlukoSwitch(goal.enabled) { viewModel.setCarbGoal(goal.copy(enabled = !goal.enabled)) }
    }

    if (goal.enabled) {
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            GlukoField(
                value = text,
                onValueChange = { typed ->
                    text = numeric(typed)
                    val grams = display.parse(text)?.coerceIn(0f, MAX_CARB_GOAL_GRAMS)
                    viewModel.setCarbGoal(goal.copy(grams = grams))
                },
                placeholder = "0",
                modifier = Modifier.width(112.dp),
                textStyle = GlukoType.ScreenTitle.copy(fontSize = 30.sp, lineHeight = 34.sp).tabular,
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done,
                background = Color.Transparent,
                padding = PaddingValues(0.dp),
                cursorBrush = SolidColor(GlukoColors.CursorSoft)
            )
            Text(carbsUnitLabel(), style = GlukoType.Label, modifier = Modifier.padding(bottom = 5.dp))
            Spacer(Modifier.weight(1f))
            display.parse(text)?.takeIf { it > 0f }?.let { grams ->
                Text(equivalent(grams, display.unit, display.gramsPerXe), style = GlukoType.Hint, modifier = Modifier.padding(bottom = 6.dp))
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(GlukoRadius.tile))
                .background(GlukoColors.Tile)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleButton(32.dp, GlukoColors.Surface, { step(-1) }) {
                LucideIcon(LucidePaths.Minus, 12.dp, strokeWidth = 2.4f)
            }
            Text(
                stringResource(R.string.carb_goal_step, carbsText(carbGoalStepGrams(display))),
                style = GlukoType.Hint.copy(color = GlukoColors.TextSecondary),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            CircleButton(32.dp, GlukoColors.Surface, { step(1) }) {
                LucideIcon(LucidePaths.Plus, 12.dp, strokeWidth = 2.4f)
            }
        }
    }

    Spacer(Modifier.height(11.dp))
    Text(stringResource(R.string.carb_goal_hint), style = GlukoType.Hint)
}

/** The goal in the other unit: "≈ 150 г" while carbs are shown in XE, "≈ 12.5 ХЕ" while in grams. */
@Composable
private fun equivalent(grams: Float, unit: CarbUnit, gramsPerXe: Float): String = when (unit) {
    CarbUnit.XE -> stringResource(R.string.carb_goal_equivalent, stringResource(R.string.carbs_grams_value_format, grams.roundToInt().toString()))
    CarbUnit.GRAMS -> stringResource(R.string.carb_goal_equivalent, stringResource(R.string.bread_units_value_format, formatXe(grams, gramsPerXe)))
}
