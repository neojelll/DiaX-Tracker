package com.neojelll.diaxtracker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.MealPresetWithProducts
import com.neojelll.diaxtracker.photo.PhotoStore
import com.neojelll.diaxtracker.ui.components.LucideIcon
import com.neojelll.diaxtracker.ui.components.LucidePaths
import com.neojelll.diaxtracker.ui.components.PresetOption
import com.neojelll.diaxtracker.ui.components.dashedBorder
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoRadius
import com.neojelll.diaxtracker.ui.theme.GlukoType
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal fun formatAmount(value: Float): String =
    if (value == value.toInt().toFloat()) value.toInt().toString() else value.toString()

/** Applies a picked preset to the form: real bread units/products, display label from the design. */
internal fun applyPresetPick(state: EntryFormState, preset: MealPresetWithProducts, option: PresetOption): EntryFormState =
    state.copy(
        breadUnits = formatAmount(preset.totalBreadUnits),
        foodLabel = "${preset.preset.name} · ${option.xeLabel}",
        mealLabel = preset.preset.name,
        mealProducts = preset.products.sortedBy { it.sortOrder }.map { MealProductEntry(it.name, formatAmount(it.breadUnits)) },
        foodExpanded = false
    )

/** Applies the manual bread-units entry; returns the state unchanged if the value doesn't parse. */
internal fun applyManualXe(state: EntryFormState, manualFormat: String): EntryFormState {
    val value = state.manualXe.replace(',', '.').toFloatOrNull() ?: return state
    return state.copy(
        breadUnits = formatAmount(value),
        foodLabel = manualFormat.format(formatAmount(value)),
        mealLabel = null,
        mealProducts = emptyList(),
        foodExpanded = false,
        manualXe = ""
    )
}

/**
 * Photo affordance shared by the entry form and the record-edit sheet: dashed row with a camera
 * icon, or the real thumbnail once a photo is attached (tap to replace, X to remove).
 */
@Composable
internal fun PhotoPickerRow(
    photoPath: String?,
    onPhotoChanged: (String?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val oldPath = photoPath
            scope.launch(Dispatchers.IO) {
                val newPath = PhotoStore.savePhoto(context, uri)
                if (newPath != null) {
                    withContext(Dispatchers.Main) { onPhotoChanged(newPath) }
                    oldPath?.let { PhotoStore.deletePhoto(it) }
                }
            }
        }
    }
    fun launchPicker() = launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

    Row(
        Modifier
            .fillMaxWidth()
            .dashedBorder()
            .clickable { launchPicker() }
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(42.dp).clip(RoundedCornerShape(GlukoRadius.strip)).background(GlukoColors.Tile),
            contentAlignment = Alignment.Center
        ) {
            if (photoPath != null) {
                AsyncImage(
                    model = File(photoPath),
                    contentDescription = stringResource(R.string.entry_photo),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(42.dp).clip(RoundedCornerShape(GlukoRadius.strip))
                )
            } else {
                LucideIcon(LucidePaths.Camera, 18.dp, strokeWidth = 1.7f)
            }
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(if (photoPath != null) R.string.photo_attached else R.string.photo_add_title),
                style = GlukoType.Body.copy(fontSize = 13.sp)
            )
            Spacer(Modifier.height(2.dp))
            Text(stringResource(R.string.photo_add_hint), style = GlukoType.CardLabel.copy(color = GlukoColors.TextTertiary))
        }
        if (photoPath != null) {
            Box(
                Modifier.size(24.dp).clickable {
                    PhotoStore.deletePhoto(photoPath)
                    onPhotoChanged(null)
                },
                contentAlignment = Alignment.Center
            ) {
                LucideIcon(LucidePaths.Close, 13.dp, GlukoColors.TextSecondary, strokeWidth = 2.2f)
            }
        }
    }
}
