package com.neojelll.diaxtracker.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.ui.components.AppBottomNavBar
import com.neojelll.diaxtracker.ui.components.InsulinBadge
import com.neojelll.diaxtracker.ui.components.NotificationPanel
import com.neojelll.diaxtracker.ui.components.PhotoPreviewOverlay
import com.neojelll.diaxtracker.ui.components.SheetBackdrop
import com.neojelll.diaxtracker.ui.screens.EditEntrySheet
import com.neojelll.diaxtracker.ui.screens.FoodScreen
import com.neojelll.diaxtracker.ui.screens.HistoryScreen
import com.neojelll.diaxtracker.ui.screens.HomeScreen
import com.neojelll.diaxtracker.ui.screens.PendingFoodPick
import com.neojelll.diaxtracker.ui.screens.PresetDetailSheet
import com.neojelll.diaxtracker.ui.screens.PresetEditorSheet
import com.neojelll.diaxtracker.ui.screens.SettingsScreen
import com.neojelll.diaxtracker.ui.theme.GlucoIcons
import com.neojelll.diaxtracker.ui.theme.Ink
import com.neojelll.diaxtracker.ui.theme.PageBackground
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel

@Composable
fun AppRoot() {
    val viewModel: DiaryViewModel = viewModel()
    var tab by remember { mutableStateOf(AppTab.HOME) }
    var overlay by remember { mutableStateOf<AppOverlay?>(null) }
    var pendingFoodPick by remember { mutableStateOf<PendingFoodPick?>(null) }
    val activeInsulinEntry by viewModel.activeInsulinEntry.collectAsState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.errorEvents.collect { messageRes ->
            snackbarHostState.showSnackbar(context.getString(messageRes))
        }
    }

    fun openPresetDetail(id: Long) {
        overlay = AppOverlay.PresetDetail(id)
    }

    Box(Modifier.fillMaxSize().background(PageBackground)) {
        Column(Modifier.fillMaxSize()) {
            activeInsulinEntry?.let { entry -> InsulinBadge(entry = entry) }

            Box(Modifier.weight(1f, fill = true)) {
                when (tab) {
                    AppTab.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onOpenNotifications = { overlay = AppOverlay.Notifications },
                        onCreatePreset = {
                            tab = AppTab.FOOD
                            overlay = AppOverlay.PresetEditor(null)
                        },
                        pendingFoodPick = pendingFoodPick,
                        onPendingFoodPickConsumed = { pendingFoodPick = null }
                    )
                    AppTab.FOOD -> FoodScreen(
                        viewModel = viewModel,
                        onOpenNotifications = { overlay = AppOverlay.Notifications },
                        onOpenPresetDetail = ::openPresetDetail,
                        onCreatePreset = { overlay = AppOverlay.PresetEditor(null) }
                    )
                    AppTab.HISTORY -> HistoryScreen(
                        viewModel = viewModel,
                        onEditEntry = { id -> overlay = AppOverlay.EditEntry(id) },
                        onOpenNotifications = { overlay = AppOverlay.Notifications },
                        onOpenPresetDetail = ::openPresetDetail,
                        onOpenPhoto = { path, caption -> overlay = AppOverlay.Photo(path, caption) }
                    )
                    AppTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
                }

                if (tab == AppTab.FOOD) {
                    FoodFab(
                        onClick = { overlay = AppOverlay.PresetEditor(null) },
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
            }

            AppBottomNavBar(currentTab = tab, onSelect = { tab = it })
        }

        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))

        when (val current = overlay) {
            is AppOverlay.EditEntry -> {
                SheetBackdrop(onDismiss = { overlay = null })
                EditEntrySheet(viewModel = viewModel, entryId = current.entryId, onClose = { overlay = null })
            }
            is AppOverlay.PresetDetail -> {
                SheetBackdrop(onDismiss = { overlay = null })
                PresetDetailSheet(
                    viewModel = viewModel,
                    presetId = current.presetId,
                    onPickForEntry = { mealLabel, breadUnits, products ->
                        pendingFoodPick = PendingFoodPick(mealLabel, breadUnits, products)
                        tab = AppTab.HOME
                        overlay = null
                    },
                    onEdit = { id -> overlay = AppOverlay.PresetEditor(id) },
                    onClose = { overlay = null }
                )
            }
            is AppOverlay.PresetEditor -> {
                SheetBackdrop(onDismiss = { overlay = null })
                PresetEditorSheet(viewModel = viewModel, presetId = current.presetId, onClose = { overlay = null })
            }
            is AppOverlay.Photo -> PhotoPreviewOverlay(photoPath = current.photoPath, caption = current.caption, onDismiss = { overlay = null })
            AppOverlay.Notifications -> NotificationPanel(onClose = { overlay = null })
            null -> Unit
        }
    }
}

@Composable
private fun FoodFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .offset(x = (-20).dp, y = (-16).dp)
            .size(56.dp)
            .shadow(8.dp, CircleShape, clip = false, ambientColor = Color(0x38000000), spotColor = Color(0x38000000))
            .clip(CircleShape)
            .background(Ink)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(GlucoIcons.Plus, contentDescription = stringResource(R.string.add_meal_preset), tint = Color.White)
    }
}
