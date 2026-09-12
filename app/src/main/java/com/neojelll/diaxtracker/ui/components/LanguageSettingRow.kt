package com.neojelll.diaxtracker.ui.components

import android.app.Activity
import android.app.LocaleManager
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.ui.theme.FieldTile
import com.neojelll.diaxtracker.ui.theme.Ink
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

    val currentLocale = AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault()
    val isEnglish = currentLocale.language == "en"

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LanguageTile(
            label = stringResource(R.string.language_russian),
            selected = !isEnglish,
            onClick = { setLocale("ru") },
            modifier = Modifier.weight(1f)
        )
        LanguageTile(
            label = stringResource(R.string.language_english),
            selected = isEnglish,
            onClick = { setLocale("en") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LanguageTile(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Ink else FieldTile)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 10.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(label, fontSize = 13.5.sp, color = if (selected) Color.White else Ink)
    }
}
