package com.neojelll.diaxtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.MealPresetWithProducts
import com.neojelll.diaxtracker.ui.components.NotificationBellButton
import com.neojelll.diaxtracker.ui.theme.BorderDashed
import com.neojelll.diaxtracker.ui.theme.CardDivider
import com.neojelll.diaxtracker.ui.theme.CardShapeMedium
import com.neojelll.diaxtracker.ui.theme.FieldTile
import com.neojelll.diaxtracker.ui.theme.GlucoIcons
import com.neojelll.diaxtracker.ui.theme.Ink
import com.neojelll.diaxtracker.ui.theme.ScreenTitle
import com.neojelll.diaxtracker.ui.theme.TextLabel
import com.neojelll.diaxtracker.ui.theme.TextSecondary
import com.neojelll.diaxtracker.ui.theme.TextTertiary
import com.neojelll.diaxtracker.ui.theme.card
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel

@Composable
fun FoodScreen(
    viewModel: DiaryViewModel,
    onOpenNotifications: () -> Unit,
    onOpenPresetDetail: (Long) -> Unit,
    onCreatePreset: () -> Unit
) {
    val mealPresets by viewModel.mealPresets.collectAsState()

    Column(Modifier.fillMaxSize().padding(horizontal = 14.dp).padding(top = 18.dp, bottom = 130.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(stringResource(R.string.food_screen_title), style = ScreenTitle, color = Ink)
                Text(
                    pluralStringResource(R.plurals.preset_count, mealPresets.size, mealPresets.size),
                    fontSize = 13.sp,
                    color = TextLabel,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
            NotificationBellButton(onClick = onOpenNotifications)
        }

        if (mealPresets.isEmpty()) {
            Column(Modifier.fillMaxWidth().padding(top = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.no_meal_presets_title), fontSize = 15.sp, color = Ink)
                Text(
                    stringResource(R.string.no_meal_presets_subtitle),
                    fontSize = 13.sp,
                    color = TextLabel,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(mealPresets, key = { it.preset.id }) { preset ->
                    PresetGridCard(preset = preset, onClick = { onOpenPresetDetail(preset.preset.id) })
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, BorderDashed, RoundedCornerShape(20.dp))
                .clickable(onClick = onCreatePreset)
                .padding(15.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(GlucoIcons.Plus, contentDescription = null, tint = TextLabel, modifier = Modifier.padding(end = 8.dp))
            Text(stringResource(R.string.add_meal_preset), fontSize = 13.sp, color = TextLabel)
        }
    }
}

@Composable
private fun PresetGridCard(preset: MealPresetWithProducts, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .card(CardShapeMedium)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(preset.preset.name, fontSize = 14.sp, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier.clip(RoundedCornerShape(50)).background(FieldTile).padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(stringResource(R.string.bread_units_value_format, formatAmount(preset.totalBreadUnits)), fontSize = 10.5.sp, color = Ink)
            }
        }
        preset.products.sortedBy { it.sortOrder }.forEach { product ->
            Row(Modifier.fillMaxWidth().padding(vertical = 2.5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(product.name, fontSize = 11.5.sp, color = TextLabel, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text(formatAmount(product.breadUnits), fontSize = 11.5.sp, color = TextSecondary)
            }
        }
        Box(Modifier.fillMaxWidth().padding(vertical = 9.dp).height(1.dp).background(CardDivider))
        if (preset.preset.comment.isNotBlank()) {
            Text(preset.preset.comment, fontSize = 10.5.sp, color = TextTertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
