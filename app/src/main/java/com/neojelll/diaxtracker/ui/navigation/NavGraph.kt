package com.neojelll.diaxtracker.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
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
import com.neojelll.diaxtracker.ui.theme.AccentDark
import com.neojelll.diaxtracker.ui.theme.PageBackground
import com.neojelll.diaxtracker.ui.theme.TextSecondary
import com.neojelll.diaxtracker.ui.theme.card
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel

sealed class Screen(val route: String, @StringRes val labelRes: Int, val icon: ImageVector) {
    data object AddEntry : Screen("add_entry", R.string.nav_entry, Icons.Filled.Home)
    data object MealPresets : Screen("meal_presets", R.string.nav_meal_presets, Icons.Filled.Restaurant)
    data object History : Screen("history", R.string.nav_history, Icons.Filled.History)
}

private val bottomNavItems = listOf(Screen.AddEntry, Screen.MealPresets, Screen.History)

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

        if (activeInsulinEntries.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 108.dp)
            ) {
                InsulinActiveBanner(entries = activeInsulinEntries)
            }
        }
    }
}

@Composable
private fun AppBottomBar(
    currentRoute: String?,
    onSelect: (Screen) -> Unit
) {
    val barShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .card(barShape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 24.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            bottomNavItems.forEach { screen ->
                val selected = currentRoute == screen.route
                val label = stringResource(screen.labelRes)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .then(if (selected) Modifier.background(AccentDark) else Modifier)
                        .clickable { onSelect(screen) }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = label,
                        tint = if (selected) Color.White else TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
