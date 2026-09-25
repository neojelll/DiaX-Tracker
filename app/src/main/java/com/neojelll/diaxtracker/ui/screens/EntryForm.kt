package com.neojelll.diaxtracker.ui.screens

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.MealPresetWithProducts
import com.neojelll.diaxtracker.photo.PhotoStore
import com.neojelll.diaxtracker.ui.components.OverlayController
import com.neojelll.diaxtracker.ui.components.rememberAppToasts
import com.neojelll.diaxtracker.ui.components.PresetOption
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

/** What the photo tile/button need: open the action menu, or drop the current photo. */
internal class PhotoPicker(val open: () -> Unit, val remove: () -> Unit)

/**
 * Camera + gallery launchers and the action-menu wiring shared by the entry form and the
 * record-edit sheet. The UI itself (tile / button / menu) lives in ui.components.PhotoPicker.
 */
@Composable
internal fun rememberPhotoPicker(
    photoPath: String?,
    onPhotoChanged: (String?) -> Unit,
    overlays: OverlayController
): PhotoPicker {
    val context = LocalContext.current
    val toasts = rememberAppToasts()
    val scope = rememberCoroutineScope()
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
            toasts.error(context.getString(R.string.photo_camera_unavailable))
        }
    }

    fun removePhoto() {
        PhotoStore.deletePhoto(photoPath)
        onPhotoChanged(null)
    }

    return PhotoPicker(
        open = {
            overlays.openPhotoActions(
                hasPhoto = photoPath != null,
                onCamera = ::launchCamera,
                onGallery = ::launchGallery,
                onRemove = ::removePhoto
            )
        },
        remove = ::removePhoto
    )
}
