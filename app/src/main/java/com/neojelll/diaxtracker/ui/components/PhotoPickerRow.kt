package com.neojelll.diaxtracker.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.photo.PhotoStore
import com.neojelll.diaxtracker.ui.theme.FieldTile
import com.neojelll.diaxtracker.ui.theme.GlucoIcons
import com.neojelll.diaxtracker.ui.theme.Ink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun PhotoPickerButton(photoPath: String?, onPhotoPicked: (String?) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val oldPath = photoPath
            coroutineScope.launch(Dispatchers.IO) {
                val newPath = PhotoStore.savePhoto(context, uri)
                if (newPath != null) {
                    withContext(Dispatchers.Main) { onPhotoPicked(newPath) }
                    oldPath?.let { PhotoStore.deletePhoto(it) }
                }
            }
        }
    }

    if (photoPath != null) {
        Row(
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(FieldTile)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = File(photoPath),
                contentDescription = stringResource(R.string.entry_photo),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Text(
                stringResource(R.string.entry_photo),
                fontSize = 13.5.sp,
                color = Ink,
                modifier = Modifier.padding(start = 12.dp).weight(1f)
            )
            RoundIconButton(
                icon = GlucoIcons.Close,
                contentDescription = stringResource(R.string.remove_photo),
                onClick = { PhotoStore.deletePhoto(photoPath); onPhotoPicked(null) },
                size = 28.dp,
                background = Color.White
            )
        }
    } else {
        OutlinedPillButton(
            text = stringResource(R.string.add_photo_short),
            leadingIcon = GlucoIcons.Camera,
            onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            modifier = modifier.fillMaxWidth()
        )
    }
}
