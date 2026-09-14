package com.neojelll.diaxtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoType

data class NavBarItem(val route: String, val labelRes: Int, val iconPath: String)

/** Bottom navbar: four equal cells, selected = black pill with white icon+label. */
@Composable
fun GlukoNavBar(
    items: List<NavBarItem>,
    currentRoute: String?,
    onSelect: (NavBarItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp, bottomStart = 42.dp, bottomEnd = 42.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, shape, ambientColor = Color(0x12000000), spotColor = Color(0x12000000))
            .clip(shape)
            .background(GlukoColors.Surface)
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
            .padding(horizontal = 10.dp)
            .padding(top = 10.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEach { item ->
            NavItem(
                modifier = Modifier.weight(1f),
                item = item,
                selected = currentRoute == item.route,
                onClick = { onSelect(item) }
            )
        }
    }
}

@Composable
private fun NavItem(modifier: Modifier, item: NavBarItem, selected: Boolean, onClick: () -> Unit) {
    val fg = if (selected) GlukoColors.Surface else GlukoColors.TextLabel
    val label = stringResource(item.labelRes)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) GlukoColors.Ink else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(top = 11.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        LucideIcon(item.iconPath, 20.dp, fg)
        Text(label, style = GlukoType.NavLabel.copy(color = fg), maxLines = 1)
    }
}
