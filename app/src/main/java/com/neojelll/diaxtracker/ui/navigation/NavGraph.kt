package com.neojelll.diaxtracker.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.neojelll.diaxtracker.R
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.neojelll.diaxtracker.ui.screens.AddEntryScreen
import com.neojelll.diaxtracker.ui.screens.EditEntryScreen
import com.neojelll.diaxtracker.ui.screens.HistoryScreen
import com.neojelll.diaxtracker.ui.screens.InsulinActiveBanner
import com.neojelll.diaxtracker.ui.screens.MealPresetsScreen
import com.neojelll.diaxtracker.ui.screens.SettingsScreen
import com.neojelll.diaxtracker.ui.theme.CardBackground
import com.neojelll.diaxtracker.ui.theme.CardBorder
import com.neojelll.diaxtracker.ui.theme.FieldBackground
import com.neojelll.diaxtracker.ui.theme.PageBackground
import com.neojelll.diaxtracker.ui.theme.TextPrimary
import com.neojelll.diaxtracker.ui.theme.TextSecondary
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel

sealed class Screen(val route: String, @StringRes val labelRes: Int, val icon: ImageVector) {
    data object AddEntry : Screen("add_entry", R.string.nav_entry, Icons.Filled.Home)
    data object MealPresets : Screen("meal_presets", R.string.nav_meal_presets, Icons.Filled.Restaurant)
    data object History : Screen("history", R.string.nav_history, Icons.Filled.History)
    data object Settings : Screen("settings", R.string.nav_settings, Icons.Filled.Settings)
}

private val bottomNavItems = listOf(Screen.AddEntry, Screen.MealPresets, Screen.History, Screen.Settings)

private const val EDIT_ENTRY_ROUTE = "edit_entry/{entryId}"
private fun editEntryRoute(entryId: Long) = "edit_entry/$entryId"

@Composable
fun NavGraph(navController: NavHostController) {
    val viewModel: DiaryViewModel = viewModel()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val activeInsulinEntries by viewModel.activeInsulinEntries.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PageBackground)
        )

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Column {
                    if (activeInsulinEntries.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PageBackground)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            InsulinActiveBanner(entries = activeInsulinEntries)
                        }
                    }
                    AppBottomBar(
                        currentRoute = currentRoute,
                        onSelect = { screen ->
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Screen.AddEntry.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(Screen.AddEntry.route) {
                    AddEntryScreen(viewModel = viewModel)
                }
                composable(Screen.MealPresets.route) {
                    MealPresetsScreen(viewModel = viewModel)
                }
                composable(Screen.History.route) {
                    HistoryScreen(
                        viewModel = viewModel,
                        onEntryClick = { entryId -> navController.navigate(editEntryRoute(entryId)) }
                    )
                }
                composable(Screen.Settings.route) {
                    SettingsScreen()
                }
                composable(
                    route = EDIT_ENTRY_ROUTE,
                    arguments = listOf(navArgument("entryId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val entryId = backStackEntry.arguments?.getLong("entryId") ?: return@composable
                    EditEntryScreen(
                        viewModel = viewModel,
                        entryId = entryId,
                        onDone = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppBottomBar(
    currentRoute: String?,
    onSelect: (Screen) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground)
    ) {
        HorizontalDivider(color = CardBorder)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { screen ->
                val selected = currentRoute == screen.route
                val label = stringResource(screen.labelRes)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .then(if (selected) Modifier.background(FieldBackground) else Modifier)
                        .clickable { onSelect(screen) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = label,
                        tint = if (selected) TextPrimary else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
