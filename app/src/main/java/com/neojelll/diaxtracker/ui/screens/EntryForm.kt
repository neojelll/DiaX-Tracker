package com.neojelll.diaxtracker.ui.screens

import android.content.ActivityNotFoundException
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.neojelll.diaxtracker.ui.components.plainClickable
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

/**
 * Live preview while typing manual bread units: updates the draft text and, once it parses,
 * the committed food label too - the picker stays open so typing can continue.
 */
internal fun updateManualXe(state: EntryFormState, rawInput: String, manualFormat: String): EntryFormState {
    val updated = state.copy(manualXe = rawInput)
    val value = rawInput.replace(',', '.').toFloatOrNull() ?: return updated
    return updated.copy(
        breadUnits = formatAmount(value),
        foodLabel = manualFormat.format(formatAmount(value)),
        mealLabel = null,
        mealProducts = emptyList()
    )
}

/** Closes the picker and clears the draft input once the keyboard's Done action commits a valid value. */
internal fun finishManualXe(state: EntryFormState): EntryFormState =
    if (state.manualXe.replace(',', '.').toFloatOrNull() != null) {
        state.copy(foodExpanded = false, manualXe = "")
    } else {
        state
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
    var menuOpen by remember { mutableStateOf(false) }
    // The camera writes into a temp file that outlives this composition if the process is
    // killed while the camera app is in front, so remember its path across recreation.
    var pendingCapture by rememberSaveable { mutableStateOf<String?>(null) }

    fun replacePhoto(save: () -> String?) {
        val oldPath = photoPath
        scope.launch(Dispatchers.IO) {
            val newPath = save()
            if (newPath != null) {
                withContext(Dispatchers.Main) { onPhotoChanged(newPath) }
                oldPath?.let { PhotoStore.deletePhoto(it) }
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) replacePhoto { PhotoStore.savePhoto(context, uri) }
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { captured: Boolean ->
        val capture = pendingCapture?.let(::File)
        pendingCapture = null
        if (capture != null) {
            replacePhoto {
                try {
                    if (captured) PhotoStore.saveCapture(context, capture) else null
                } finally {
                    capture.delete()
                }
            }
        }
    }

    fun launchGallery() = galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    fun launchCamera() {
        val (file, uri) = PhotoStore.newCaptureTarget(context)
        pendingCapture = file.absolutePath
        try {
            cameraLauncher.launch(uri)
        } catch (e: ActivityNotFoundException) {
            pendingCapture = null
            file.delete()
            Toast.makeText(context, R.string.photo_camera_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    Box {
        Row(
            Modifier
                .fillMaxWidth()
                .dashedBorder()
                .plainClickable { menuOpen = true }
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
                    Modifier.size(24.dp).plainClickable {
                        PhotoStore.deletePhoto(photoPath)
                        onPhotoChanged(null)
                    },
                    contentAlignment = Alignment.Center
                ) {
                    LucideIcon(LucidePaths.Close, 13.dp, GlukoColors.TextSecondary, strokeWidth = 2.2f)
                }
            }
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.photo_take), style = GlukoType.Body) },
                onClick = { menuOpen = false; launchCamera() }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.photo_choose), style = GlukoType.Body) },
                onClick = { menuOpen = false; launchGallery() }
            )
        }
    }
}
