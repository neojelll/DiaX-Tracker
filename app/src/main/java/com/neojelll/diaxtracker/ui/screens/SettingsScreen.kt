package com.neojelll.diaxtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.ui.components.BackupExportRow
import com.neojelll.diaxtracker.ui.components.BackupImportRow
import com.neojelll.diaxtracker.ui.components.BackupToggleRow
import com.neojelll.diaxtracker.ui.components.DeleteAllRecordsCard
import com.neojelll.diaxtracker.ui.components.GlucoseRangeSettingRow
import com.neojelll.diaxtracker.ui.components.LanguageSettingRow
import com.neojelll.diaxtracker.ui.theme.CardDivider
import com.neojelll.diaxtracker.ui.theme.Ink
import com.neojelll.diaxtracker.ui.theme.ScreenTitle
import com.neojelll.diaxtracker.ui.theme.TextLabel
import com.neojelll.diaxtracker.ui.theme.card
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel

@Composable
fun SettingsScreen(viewModel: DiaryViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 14.dp).padding(top = 18.dp, bottom = 130.dp)) {
        Column(Modifier.padding(horizontal = 8.dp).padding(bottom = 14.dp)) {
            Text(stringResource(R.string.settings_title), style = ScreenTitle, color = Ink)
            Text(stringResource(R.string.settings_subtitle), fontSize = 13.sp, color = TextLabel, modifier = Modifier.padding(top = 5.dp))
        }

        Column(Modifier.fillMaxWidth().card().padding(16.dp)) {
            Text(stringResource(R.string.language), fontSize = 13.sp, color = TextLabel, modifier = Modifier.padding(bottom = 11.dp))
            LanguageSettingRow()
        }

        Column(Modifier.fillMaxWidth().padding(top = 10.dp).card().padding(16.dp)) {
            GlucoseRangeSettingRow(viewModel = viewModel)
        }

        Column(Modifier.fillMaxWidth().padding(top = 10.dp).card().padding(16.dp)) {
            Text(stringResource(R.string.data_section_kicker), fontSize = 13.sp, color = TextLabel, modifier = Modifier.padding(bottom = 12.dp))
            BackupExportRow(snackbarHostState = snackbarHostState)
            Box(Modifier.fillMaxWidth().padding(vertical = 2.dp).height(1.dp).background(CardDivider))
            BackupImportRow(snackbarHostState = snackbarHostState)
            Box(Modifier.fillMaxWidth().padding(vertical = 2.dp).height(1.dp).background(CardDivider))
            BackupToggleRow()
        }

        Box(Modifier.padding(top = 10.dp)) {
            DeleteAllRecordsCard()
        }

        SnackbarHost(snackbarHostState)
    }
}
