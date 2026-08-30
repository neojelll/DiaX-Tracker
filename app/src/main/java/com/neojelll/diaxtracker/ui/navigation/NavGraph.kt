package com.neojelll.diaxtracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.neojelll.diaxtracker.ui.screens.AddEntryScreen
import com.neojelll.diaxtracker.ui.screens.HistoryScreen
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
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) }
                    )
                }
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
            composable(Screen.History.route) {
                HistoryScreen(viewModel = viewModel)
            }
        }
    }
}
