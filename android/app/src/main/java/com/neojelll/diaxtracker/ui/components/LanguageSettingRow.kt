package com.neojelll.diaxtracker.ui.components

import android.app.Activity
import android.app.LocaleManager
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.neojelll.diaxtracker.R
import java.util.Locale

@Composable
fun LanguageSettingRow() {
    val context = LocalContext.current

    fun setLocale(tag: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java).applicationLocales =
                LocaleList.forLanguageTags(tag)
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
        }
        (context as? Activity)?.recreate()
    }

    val currentLanguage = AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault()
    val isEnglish = currentLanguage.language == "en"

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ChoiceTile(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.language_russian),
            selected = !isEnglish,
            onClick = { setLocale("ru") }
        )
        ChoiceTile(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.language_english),
            selected = isEnglish,
            onClick = { setLocale("en") }
        )
    }
}
