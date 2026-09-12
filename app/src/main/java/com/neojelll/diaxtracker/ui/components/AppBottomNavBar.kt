package com.neojelll.diaxtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.ui.navigation.AppTab
import com.neojelll.diaxtracker.ui.theme.CardSurface
import com.neojelll.diaxtracker.ui.theme.GlucoIcons
import com.neojelll.diaxtracker.ui.theme.Ink
import com.neojelll.diaxtracker.ui.theme.NavBarShape
import com.neojelll.diaxtracker.ui.theme.NavLabel
import com.neojelll.diaxtracker.ui.theme.TextLabel

private data class NavItem(val tab: AppTab, val icon: ImageVector, @androidx.annotation.StringRes val labelRes: Int)

private val NAV_ITEMS = listOf(
    NavItem(AppTab.HOME, GlucoIcons.NavHome, R.string.nav_home),
    NavItem(AppTab.FOOD, GlucoIcons.NavFood, R.string.nav_food),
    NavItem(AppTab.HISTORY, GlucoIcons.NavHistory, R.string.nav_history),
    NavItem(AppTab.SETTINGS, GlucoIcons.NavSettings, R.string.nav_settings)
)

@Composable
fun AppBottomNavBar(currentTab: AppTab, onSelect: (AppTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .shadow(4.dp, NavBarShape, clip = false, ambientColor = Color(0x12000000), spotColor = Color(0x12000000))
            .clip(NavBarShape)
            .background(CardSurface)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        NAV_ITEMS.forEach { item ->
            val selected = currentTab == item.tab
            val label = stringResource(item.labelRes)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onSelect(item.tab) }
                    .padding(vertical = 11.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(item.icon, contentDescription = label, tint = if (selected) Ink else TextLabel)
                Text(label, style = NavLabel, color = if (selected) Ink else TextLabel)
            }
        }
    }
}
