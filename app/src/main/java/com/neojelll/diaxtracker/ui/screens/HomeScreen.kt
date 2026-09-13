package com.neojelll.diaxtracker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.DiaryEntryProduct
import com.neojelll.diaxtracker.photo.PhotoStore
import com.neojelll.diaxtracker.ui.components.DateSheet
import com.neojelll.diaxtracker.ui.components.DateTimeButtonsRow
import com.neojelll.diaxtracker.ui.components.MealFoodField
import com.neojelll.diaxtracker.ui.components.NotificationBellButton
import com.neojelll.diaxtracker.ui.components.OutlinedPillButton
import com.neojelll.diaxtracker.ui.components.PrimaryPillButton
import com.neojelll.diaxtracker.ui.components.RoundIconButton
import com.neojelll.diaxtracker.ui.components.SheetBackdrop
import com.neojelll.diaxtracker.ui.components.TimeSheet
import com.neojelll.diaxtracker.ui.theme.CardDivider
import com.neojelll.diaxtracker.ui.theme.FieldTile
import com.neojelll.diaxtracker.ui.theme.GlucoIcons
import com.neojelll.diaxtracker.ui.theme.Ink
import com.neojelll.diaxtracker.ui.theme.PlaceholderText
import com.neojelll.diaxtracker.ui.theme.ScreenTitle
import com.neojelll.diaxtracker.ui.theme.TextLabel
import com.neojelll.diaxtracker.ui.theme.TextTertiary
import com.neojelll.diaxtracker.ui.theme.card
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun HomeScreen(
    viewModel: DiaryViewModel,
    onCreatePreset: () -> Unit,
    pendingFoodPick: PendingFoodPick? = null,
    onPendingFoodPickConsumed: () -> Unit = {}
) {
    var formState by remember { mutableStateOf(EntryFormState()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var foodOpen by remember { mutableStateOf(false) }
    var detailsOpen by remember { mutableStateOf(false) }
    val mealPresets by viewModel.mealPresets.collectAsState()
    val entries by viewModel.entries.collectAsState()

    androidx.compose.runtime.LaunchedEffect(pendingFoodPick) {
        val pick = pendingFoodPick ?: return@LaunchedEffect
        formState = formState.copy(
            breadUnits = formatAmount(pick.breadUnits),
            foodLabel = pick.mealLabel,
            mealLabel = pick.mealLabel,
            mealProducts = pick.products
        )
        onPendingFoodPickConsumed()
    }

    val todayCount = remember(entries) {
        val today = LocalDate.now()
        entries.count { it.createdAt.toLocalDate() == today }
    }

    Box(Modifier.fillMaxSize()) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp)
            .padding(top = 18.dp, bottom = 130.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(stringResource(R.string.greeting), style = ScreenTitle, color = Ink)
                Text(
                    pluralStringResource(R.plurals.today_entries_count, todayCount, todayCount),
                    fontSize = 13.sp,
                    color = TextLabel,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
            NotificationBellButton()
        }

        Box(Modifier.fillMaxWidth().card().padding(10.dp)) {
            DateTimeButtonsRow(
                date = formState.date,
                time = formState.time,
                onDateClick = { showDatePicker = true },
                onTimeClick = { showTimePicker = true }
            )
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 9.dp)
                .card()
                .padding(horizontal = 16.dp, vertical = 15.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(GlucoIcons.Droplet, contentDescription = null, tint = TextLabel, modifier = Modifier.size(14.dp))
                Text(
                    stringResource(R.string.blood_sugar_field_label),
                    fontSize = 13.sp,
                    color = TextLabel,
                    modifier = Modifier.padding(start = 7.dp)
                )
            }
            Row(Modifier.padding(top = 2.dp), verticalAlignment = Alignment.Bottom) {
                PlainNumberField(
                    value = formState.bloodSugar,
                    onValueChange = { formState = formState.copy(bloodSugar = it) },
                    placeholder = "0.0",
                    fontSize = 34.sp,
                    widthDp = 92.dp
                )
                Text(stringResource(R.string.mmol_unit), fontSize = 13.sp, color = TextLabel, modifier = Modifier.padding(start = 7.dp))
            }

            Box(Modifier.fillMaxWidth().padding(vertical = 13.dp).height(1.dp).background(CardDivider))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                InsulinTile(
                    label = stringResource(R.string.short_insulin_tile_label),
                    value = formState.shortInsulinDose,
                    onValueChange = { formState = formState.copy(shortInsulinDose = it) },
                    modifier = Modifier.weight(1f)
                )
                InsulinTile(
                    label = stringResource(R.string.long_insulin_tile_label),
                    value = formState.longInsulinDose,
                    onValueChange = { formState = formState.copy(longInsulinDose = it) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 9.dp)
                .card()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(bottom = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.food_section_label), fontSize = 13.sp, color = TextLabel)
                Text(
                    pluralStringResource(R.plurals.preset_count, mealPresets.size, mealPresets.size),
                    fontSize = 11.5.sp,
                    color = TextTertiary
                )
            }
            val manualLabelFormat = stringResource(R.string.meal_label_manual_format)
            MealFoodField(
                label = formState.foodLabel,
                placeholder = stringResource(R.string.food_placeholder_home),
                expanded = foodOpen,
                onExpandedChange = { foodOpen = it },
                mealPresets = mealPresets,
                onPresetPicked = { mealLabel, breadUnits, products ->
                    formState = formState.copy(
                        breadUnits = formatAmount(breadUnits),
                        foodLabel = mealLabel,
                        mealLabel = mealLabel,
                        mealProducts = products
                    )
                },
                onManualEntry = { value ->
                    formState = formState.copy(
                        breadUnits = formatAmount(value),
                        foodLabel = manualLabelFormat.format(formatAmount(value)),
                        mealLabel = null,
                        mealProducts = emptyList()
                    )
                },
                onCreatePreset = onCreatePreset
            )
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 9.dp)
                .card()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().clickable { detailsOpen = !detailsOpen },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(stringResource(R.string.add_details_title), fontSize = 13.5.sp, color = Ink)
                    Text(
                        stringResource(R.string.add_details_hint),
                        fontSize = 11.5.sp,
                        color = TextTertiary,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
                RoundIconButton(
                    icon = if (detailsOpen) GlucoIcons.ChevronUp else GlucoIcons.ChevronDown,
                    contentDescription = null,
                    onClick = { detailsOpen = !detailsOpen },
                    size = 26.dp
                )
            }

            AnimatedVisibility(visible = detailsOpen) {
                DetailsSection(
                    notes = formState.notes,
                    onNotesChange = { formState = formState.copy(notes = it) },
                    photoPath = formState.photoPath,
                    onPhotoPicked = { formState = formState.copy(photoPath = it) },
                    onCancel = { detailsOpen = false }
                )
            }
        }

        PrimaryPillButton(
            text = stringResource(R.string.save_entry_button),
            enabled = formState.isFillable,
            trailingIcon = GlucoIcons.ArrowRight,
            onClick = {
                viewModel.addEntry(
                    bloodSugar = formState.bloodSugar.toFloatOrNull(),
                    breadUnits = formState.breadUnits.toFloatOrNull(),
                    mealLabel = formState.mealLabel,
                    mealProducts = formState.mealProducts.mapIndexed { index, product ->
                        DiaryEntryProduct(
                            diaryEntryId = 0,
                            name = product.name,
                            breadUnits = product.breadUnits.toFloatOrNull() ?: 0f,
                            sortOrder = index
                        )
                    },
                    shortInsulinDose = formState.shortInsulinDose.toFloatOrNull(),
                    longInsulinDose = formState.longInsulinDose.toFloatOrNull(),
                    notes = formState.notes.trim(),
                    photoPath = formState.photoPath,
                    createdAt = LocalDateTime.of(formState.date, formState.time)
                )
                formState = EntryFormState()
            },
            modifier = Modifier.padding(top = 11.dp)
        )
    }

    if (showDatePicker) {
        SheetBackdrop(onDismiss = { showDatePicker = false })
        DateSheet(
            initialDate = formState.date,
            onConfirm = { formState = formState.copy(date = it); showDatePicker = false },
            onDismiss = { showDatePicker = false },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
    if (showTimePicker) {
        SheetBackdrop(onDismiss = { showTimePicker = false })
        TimeSheet(
            initialTime = formState.time,
            onConfirm = { formState = formState.copy(time = it); showTimePicker = false },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
    }
}

@Composable
private fun InsulinTile(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(FieldTile)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(GlucoIcons.Syringe, contentDescription = null, tint = TextLabel, modifier = Modifier.size(13.dp))
            Text(label, fontSize = 11.5.sp, color = TextLabel, modifier = Modifier.padding(start = 6.dp))
        }
        Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.Bottom) {
            PlainNumberField(value = value, onValueChange = onValueChange, placeholder = "0", fontSize = 20.sp, widthDp = null)
            Text(stringResource(R.string.units_short), fontSize = 11.sp, color = TextLabel, modifier = Modifier.padding(start = 5.dp))
        }
    }
}

@Composable
internal fun PlainNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    fontSize: TextUnit,
    widthDp: Dp?
) {
    Box(if (widthDp != null) Modifier.width(widthDp) else Modifier) {
        if (value.isEmpty()) {
            Text(placeholder, fontSize = fontSize, fontWeight = FontWeight.Medium, color = PlaceholderText)
        }
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(it.filter { c -> c.isDigit() || c == '.' || c == ',' }) },
            textStyle = TextStyle(
                fontSize = fontSize,
                fontWeight = FontWeight.Medium,
                color = Ink
            ),
            cursorBrush = SolidColor(Ink),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
    }
}

@Composable
internal fun DetailsSection(
    notes: String,
    onNotesChange: (String) -> Unit,
    photoPath: String?,
    onPhotoPicked: (String?) -> Unit,
    onCancel: () -> Unit
) {
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

    Column(Modifier.padding(top = 13.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(FieldTile)
        ) {
            if (notes.isEmpty()) {
                Text(
                    stringResource(R.string.note_placeholder),
                    fontSize = 13.5.sp,
                    color = TextTertiary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp)
                )
            }
            BasicTextField(
                value = notes,
                onValueChange = onNotesChange,
                textStyle = TextStyle(fontSize = 13.5.sp, color = Ink, lineHeight = 21.sp),
                cursorBrush = SolidColor(Ink),
                minLines = 3,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp)
            )
        }

        if (photoPath != null) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 11.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(FieldTile)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = File(photoPath),
                    contentDescription = stringResource(R.string.entry_photo),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(8.dp))
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
                    onClick = { onPhotoPicked(null) },
                    size = 28.dp,
                    background = Color.White
                )
            }
        }

        Row(Modifier.fillMaxWidth().padding(top = 11.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            OutlinedPillButton(
                text = stringResource(R.string.add_photo_short),
                leadingIcon = GlucoIcons.Camera,
                onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                modifier = Modifier.weight(1f)
            )
            OutlinedPillButton(
                text = stringResource(R.string.cancel),
                textColor = TextLabel,
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
