package com.neojelll.diaxtracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.ui.components.BackupExportRow
import com.neojelll.diaxtracker.ui.components.CollapsibleTopBar
import com.neojelll.diaxtracker.ui.components.GlucoseRangeSettingRow
import com.neojelll.diaxtracker.ui.components.LanguageSettingRow
import com.neojelll.diaxtracker.ui.components.rememberCollapsibleTopBarState
import com.neojelll.diaxtracker.ui.theme.TextPrimary
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: DiaryViewModel) {
    val topBarState = rememberCollapsibleTopBarState(contentHeight = 40.dp)
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
        ) {
            CollapsibleTopBar(
                state = topBarState,
                title = { Text(stringResource(R.string.settings_title), color = TextPrimary) }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LanguageSettingRow()
                GlucoseRangeSettingRow(viewModel = viewModel)
                BackupExportRow(snackbarHostState = snackbarHostState)
            }
        }
    }
}
