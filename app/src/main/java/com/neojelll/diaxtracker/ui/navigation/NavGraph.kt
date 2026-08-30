package com.neojelll.diaxtracker.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.neojelll.diaxtracker.ui.screens.AddEntryScreen
import com.neojelll.diaxtracker.ui.screens.HistoryScreen
import com.neojelll.diaxtracker.ui.theme.AppGradient
import com.neojelll.diaxtracker.ui.theme.DeepForest
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object AddEntry : Screen("add_entry", "Запись", Icons.Filled.Edit)
    data object History : Screen("history", "История", Icons.Filled.History)
}

private val bottomNavItems = listOf(Screen.AddEntry, Screen.History)

@Composable
fun NavGraph(navController: NavHostController) {
    val viewModel: DiaryViewModel = viewModel()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

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
            composable(Screen.History.route) {
                HistoryScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun AppBottomBar(
    currentRoute: String?,
    onSelect: (Screen) -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            bottomNavItems.forEach { screen ->
                val selected = currentRoute == screen.route
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .then(if (selected) Modifier.background(AppGradient) else Modifier)
                        .clickable { onSelect(screen) }
                        .padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.label,
                        tint = if (selected) Color.White else DeepForest,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = screen.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) Color.White else DeepForest
                    )
                }
            }
        }
    }
}
