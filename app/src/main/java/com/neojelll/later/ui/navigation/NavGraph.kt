package com.neojelll.later.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.neojelll.later.ui.screens.AddEntryScreen
import com.neojelll.later.ui.screens.HistoryScreen
import com.neojelll.later.ui.viewmodel.DiaryViewModel

sealed class Screen(val route: String) {
    data object AddEntry : Screen("add_entry")
    data object History : Screen("history")
}

@Composable
fun NavGraph(navController: NavHostController) {
    val viewModel: DiaryViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.AddEntry.route) {
        composable(Screen.AddEntry.route) {
            AddEntryScreen(
                viewModel = viewModel,
                onNavigateToHistory = { navController.navigate(Screen.History.route) }
            )
        }
        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
