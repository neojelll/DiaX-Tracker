package com.neojelll.diaxtracker.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.ui.components.InsulinBanner
import com.neojelll.diaxtracker.ui.components.DisclaimerOverlay
import com.neojelll.diaxtracker.ui.components.GlukoNavBar
import com.neojelll.diaxtracker.ui.components.LucidePaths
import com.neojelll.diaxtracker.ui.components.NavBarItem
import com.neojelll.diaxtracker.ui.components.NotificationSheet
import com.neojelll.diaxtracker.ui.components.Overlay
import com.neojelll.diaxtracker.ui.components.OverlayController
import com.neojelll.diaxtracker.ui.components.PhotoActionMenu
import com.neojelll.diaxtracker.ui.components.PhotoPreview
import com.neojelll.diaxtracker.ui.components.LocalToast
import com.neojelll.diaxtracker.ui.components.ToastHost
import com.neojelll.diaxtracker.ui.components.rememberAppToasts
import com.neojelll.diaxtracker.ui.components.rememberToastState
import com.neojelll.diaxtracker.ui.screens.AddEntryScreen
import com.neojelll.diaxtracker.ui.screens.HistoryScreen
import com.neojelll.diaxtracker.ui.screens.MealPresetsScreen
import com.neojelll.diaxtracker.ui.screens.PresetDetailSheet
import com.neojelll.diaxtracker.ui.screens.PresetEditSheet
import com.neojelll.diaxtracker.ui.screens.RecordCompositionSheet
import com.neojelll.diaxtracker.ui.screens.RecordEditSheet
import com.neojelll.diaxtracker.ui.screens.SettingsScreen
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel
import com.neojelll.diaxtracker.ui.components.DateSheet
import com.neojelll.diaxtracker.ui.components.TimeSheet

private const val ROUTE_ADD_ENTRY = "add_entry"
private const val ROUTE_MEAL_PRESETS = "meal_presets"
private const val ROUTE_HISTORY = "history"
private const val ROUTE_SETTINGS = "settings"

private val navBarItems = listOf(
    NavBarItem(ROUTE_ADD_ENTRY, R.string.nav_entry, LucidePaths.Home),
    NavBarItem(ROUTE_MEAL_PRESETS, R.string.nav_meal_presets, LucidePaths.Dish),
    NavBarItem(ROUTE_HISTORY, R.string.nav_history, LucidePaths.History),
    NavBarItem(ROUTE_SETTINGS, R.string.nav_settings, LucidePaths.Settings)
)

@Composable
fun NavGraph(navController: NavHostController) {
    val viewModel: DiaryViewModel = viewModel()
    val disclaimerAccepted by viewModel.disclaimerAccepted.collectAsState()

    if (!disclaimerAccepted) {
        Box(Modifier.fillMaxSize().background(GlukoColors.Screen)) {
            DisclaimerOverlay(onAccept = viewModel::acceptDisclaimer)
        }
        return
    }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val activeInsulinEntries by viewModel.activeInsulinEntries.collectAsState()
    val insulinDurationHours by viewModel.insulinDurationHours.collectAsState()
    val overlays = remember { OverlayController() }
    var insulinExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val toastState = rememberToastState()
    var navBarHeightPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    CompositionLocalProvider(LocalToast provides toastState) {
    val toasts = rememberAppToasts()
    LaunchedEffect(viewModel) {
        viewModel.errorEvents.collect { messageRes -> toasts.error(context.getString(messageRes)) }
    }

    Box(Modifier.fillMaxSize().background(GlukoColors.Screen)) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.navigationBars)
        ) { padding ->
            // Scaffold no longer consumes navigationBars itself (see contentWindowInsets
            // above), so the live inset is applied here as bottom padding on the whole
            // column instead - this is what actually keeps GlukoNavBar, the last child,
            // clear of the system bar. The fixed 10.dp spacer below it is unrelated: it's
            // just the visual gap between scrolled content and the navbar itself.
            val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            Column(Modifier.fillMaxSize().padding(padding).padding(bottom = navBarInset)) {
                InsulinBanner(
                    entries = activeInsulinEntries,
                    durationHours = insulinDurationHours,
                    expanded = insulinExpanded,
                    onToggle = { insulinExpanded = !insulinExpanded }
                )

                Box(Modifier.weight(1f)) {
                    NavHost(navController = navController, startDestination = ROUTE_ADD_ENTRY) {
                        composable(ROUTE_ADD_ENTRY) { AddEntryScreen(viewModel, overlays) }
                        composable(ROUTE_MEAL_PRESETS) { MealPresetsScreen(viewModel, overlays) }
                        composable(ROUTE_HISTORY) { HistoryScreen(viewModel, overlays) }
                        composable(ROUTE_SETTINGS) { SettingsScreen(viewModel) }
                    }
                }

                Spacer(Modifier.height(10.dp))

                GlukoNavBar(
                    modifier = Modifier.onSizeChanged { navBarHeightPx = it.height },
                    items = navBarItems,
                    currentRoute = currentRoute,
                    onSelect = { item ->
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }

        // Toasts float above the screen and its sheets, but under the photo action menu.
        OverlayHost(viewModel, overlays, navController) { it !is Overlay.PhotoActions }
        // Above the nav bar (and the spacer over it) with a small gap; a sheet covers the bar, so drop to the bottom.
        val aboveNavBar = with(density) { navBarHeightPx.toDp() } + 10.dp + 12.dp
        ToastHost(toastState, bottomOffset = if (overlays.stack.isEmpty()) aboveNavBar else 24.dp)
        OverlayHost(viewModel, overlays, navController) { it is Overlay.PhotoActions }
    }
    }
}

@Composable
private fun OverlayHost(
    viewModel: DiaryViewModel,
    overlays: OverlayController,
    navController: NavHostController,
    include: (Overlay) -> Boolean
) {
    // Render every layer in the stack (not just the top one) so a picker pushed on top of a
    // sheet like RecordEdit leaves that sheet mounted underneath instead of tearing it down.
    for (overlay in overlays.stack) {
        if (!include(overlay)) continue
        OverlayLayer(overlay, viewModel, overlays, navController)
    }
}

@Composable
private fun OverlayLayer(
    overlay: Overlay,
    viewModel: DiaryViewModel,
    overlays: OverlayController,
    navController: NavHostController
) {
    val mealPresets by viewModel.mealPresets.collectAsState()
    val toasts = rememberAppToasts()

    when (overlay) {
        is Overlay.DatePicker -> DateSheet(
            initialDate = overlay.initial,
            onDone = { picked -> overlays.dismiss(); overlay.onPick(picked) },
            onDismiss = overlays::dismiss
        )

        is Overlay.TimePicker -> TimeSheet(
            initial = overlay.initial,
            onDone = { picked -> overlays.dismiss(); overlay.onPick(picked) },
            onDismiss = overlays::dismiss
        )

        Overlay.Notifications -> NotificationSheet(onDismiss = overlays::dismiss)

        is Overlay.PhotoActions -> PhotoActionMenu(
            hasPhoto = overlay.hasPhoto,
            onCamera = overlay.onCamera,
            onGallery = overlay.onGallery,
            onRemove = overlay.onRemove,
            onDismiss = overlays::dismiss
        )

        is Overlay.Photo -> PhotoPreview(overlay.photoPath, overlay.caption, onDismiss = overlays::dismiss)

        is Overlay.RecordEdit -> RecordEditSheet(
            viewModel = viewModel,
            entryId = overlay.entryId,
            overlays = overlays,
            onDismiss = overlays::dismiss
        )

        is Overlay.RecordComposition -> RecordCompositionSheet(
            viewModel = viewModel,
            entryId = overlay.entryId,
            onDismiss = overlays::dismiss
        )

        is Overlay.PresetDetail -> {
            val preset = mealPresets.find { it.preset.id == overlay.presetId }
            if (preset != null) {
                PresetDetailSheet(
                    preset = preset,
                    onPick = {
                        overlays.selectPresetForEntry(preset)
                        overlays.dismiss()
                        navController.navigate(ROUTE_ADD_ENTRY) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onEdit = { overlays.openPresetEdit(preset.preset.id) },
                    onDelete = {
                        viewModel.deleteMealPreset(preset) { undo -> toasts.presetDeleted(preset.preset.name, undo) }
                        overlays.dismiss()
                    },
                    onDismiss = overlays::dismiss
                )
            } else {
                overlays.dismiss()
            }
        }

        is Overlay.PresetEdit -> {
            val existing = overlay.presetId?.let { id -> mealPresets.find { it.preset.id == id } }
            PresetEditSheet(
                preset = existing,
                onConfirm = { name, comment, products ->
                    viewModel.saveMealPreset(id = existing?.preset?.id ?: 0, name = name, comment = comment, products = products)
                    toasts.presetSaved(name, isNew = existing == null)
                    overlays.dismiss()
                },
                onDismiss = overlays::dismiss
            )
        }
    }
}
