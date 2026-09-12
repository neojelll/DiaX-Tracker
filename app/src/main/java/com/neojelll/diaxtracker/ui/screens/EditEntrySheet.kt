package com.neojelll.diaxtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.DiaryEntryProduct
import com.neojelll.diaxtracker.data.SugarSource
import com.neojelll.diaxtracker.ui.components.BottomSheetSurface
import com.neojelll.diaxtracker.ui.components.DateSheet
import com.neojelll.diaxtracker.ui.components.DateTimeButtonsRow
import com.neojelll.diaxtracker.ui.components.MealFoodField
import com.neojelll.diaxtracker.ui.components.OutlinedPillButton
import com.neojelll.diaxtracker.ui.components.PrimaryPillButton
import com.neojelll.diaxtracker.ui.components.SheetBackdrop
import com.neojelll.diaxtracker.ui.components.SheetHandle
import com.neojelll.diaxtracker.ui.components.SheetHeader
import com.neojelll.diaxtracker.ui.components.SheetScrollColumn
import com.neojelll.diaxtracker.ui.components.TimeSheet
import com.neojelll.diaxtracker.ui.theme.FieldTile
import com.neojelll.diaxtracker.ui.theme.GlucoIcons
import com.neojelll.diaxtracker.ui.theme.Ink
import com.neojelll.diaxtracker.ui.theme.Kicker
import com.neojelll.diaxtracker.ui.theme.TextLabel
import com.neojelll.diaxtracker.ui.theme.TextTertiary
import com.neojelll.diaxtracker.ui.viewmodel.DiaryViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.Locale

@Composable
fun EditEntrySheet(
    viewModel: DiaryViewModel,
    entryId: Long,
    onClose: () -> Unit
) {
    val entries by viewModel.entries.collectAsState()
    val entry = entries.find { it.id == entryId } ?: return
    val mealPresets by viewModel.mealPresets.collectAsState()
    val manualLabelFormat = stringResource(R.string.meal_label_manual_format)

    val initialBloodSugarText = remember(entryId) {
        entry.bloodSugar?.let { String.format(Locale.US, "%.1f", it) } ?: ""
    }
    var formState by remember(entryId) {
        mutableStateOf(
            EntryFormState(
                date = entry.createdAt.toLocalDate(),
                time = entry.createdAt.toLocalTime(),
                bloodSugar = initialBloodSugarText,
                breadUnits = entry.breadUnits?.let { formatAmount(it) } ?: "",
                foodLabel = entry.mealLabel ?: entry.breadUnits?.let { manualLabelFormat.format(formatAmount(it)) } ?: "",
                mealLabel = entry.mealLabel,
                shortInsulinDose = entry.shortInsulinDose?.let { formatAmount(it) } ?: "",
                longInsulinDose = entry.longInsulinDose?.let { formatAmount(it) } ?: "",
                notes = entry.notes,
                photoPath = entry.photoPath
            )
        )
    }
    var foodOpen by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(entryId) {
        if (entry.mealLabel != null) {
            val products = viewModel.getEntryProducts(entryId)
            if (products.isNotEmpty()) {
                formState = formState.copy(
                    mealProducts = products.sortedBy { it.sortOrder }.map { MealProductEntry(it.name, formatAmount(it.breadUnits)) }
                )
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        BottomSheetSurface(modifier = Modifier.align(Alignment.BottomCenter)) {
            SheetHandle()
            SheetHeader(
                title = stringResource(R.string.edit_entry_sheet_title),
                onClose = onClose
            )
            SheetScrollColumn {
                DateTimeButtonsRow(
                    date = formState.date,
                    time = formState.time,
                    onDateClick = { showDatePicker = true },
                    onTimeClick = { showTimePicker = true }
                )

                Row(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(GlucoIcons.Droplet, contentDescription = null, tint = TextLabel, modifier = Modifier.padding(end = 7.dp).size(14.dp))
                    Text(stringResource(R.string.blood_sugar_field_label), style = Kicker, color = TextLabel)
                }
                PlainFieldTile(
                    value = formState.bloodSugar,
                    onValueChange = { formState = formState.copy(bloodSugar = it) }
                )

                Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.short_insulin_stat_label),
                            style = Kicker,
                            color = TextLabel,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        PlainFieldTile(value = formState.shortInsulinDose, onValueChange = { formState = formState.copy(shortInsulinDose = it) })
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.long_insulin_stat_label),
                            style = Kicker,
                            color = TextLabel,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        PlainFieldTile(value = formState.longInsulinDose, onValueChange = { formState = formState.copy(longInsulinDose = it) })
                    }
                }

                Text(
                    stringResource(R.string.food_section_label),
                    style = Kicker,
                    color = TextLabel,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                MealFoodField(
                    label = formState.foodLabel,
                    placeholder = stringResource(R.string.food_placeholder_edit),
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
                    onCreatePreset = { }
                )

                Text(
                    stringResource(R.string.comment_kicker),
                    style = Kicker,
                    color = TextLabel,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(FieldTile)
                ) {
                    if (formState.notes.isEmpty()) {
                        Text(
                            stringResource(R.string.note_placeholder),
                            fontSize = 13.5.sp,
                            color = TextTertiary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp)
                        )
                    }
                    BasicTextField(
                        value = formState.notes,
                        onValueChange = { formState = formState.copy(notes = it) },
                        textStyle = TextStyle(fontSize = 13.5.sp, color = Ink, lineHeight = 21.sp),
                        cursorBrush = SolidColor(Ink),
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp)
                    )
                }

                OutlinedPillButton(
                    text = stringResource(R.string.add_photo_short),
                    leadingIcon = GlucoIcons.Camera,
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp)
                )

                PrimaryPillButton(
                    text = stringResource(R.string.save_entry_button),
                    enabled = formState.isFillable,
                    onClick = {
                        val newCreatedAt = LocalDateTime.of(formState.date, formState.time)
                        val typedBloodSugar = formState.bloodSugar.toFloatOrNull()
                        val sugarFieldUntouched = formState.bloodSugar == initialBloodSugarText
                        val timeChanged = newCreatedAt != entry.createdAt

                        coroutineScope.launch {
                            val newBloodSugar: Float?
                            val newSugarSource: SugarSource?
                            if (entry.sugarSource == SugarSource.SENSOR && sugarFieldUntouched && timeChanged) {
                                val resynced = viewModel.sensorReadingNear(newCreatedAt)
                                newBloodSugar = resynced
                                newSugarSource = resynced?.let { SugarSource.SENSOR }
                            } else if (sugarFieldUntouched) {
                                newBloodSugar = typedBloodSugar
                                newSugarSource = entry.sugarSource
                            } else {
                                newBloodSugar = typedBloodSugar
                                newSugarSource = typedBloodSugar?.let { SugarSource.MANUAL }
                            }

                            viewModel.updateEntry(
                                entry.copy(
                                    bloodSugar = newBloodSugar,
                                    sugarSource = newSugarSource,
                                    breadUnits = formState.breadUnits.toFloatOrNull(),
                                    mealLabel = formState.mealLabel,
                                    shortInsulinDose = formState.shortInsulinDose.toFloatOrNull(),
                                    longInsulinDose = formState.longInsulinDose.toFloatOrNull(),
                                    notes = formState.notes.trim(),
                                    photoPath = formState.photoPath,
                                    createdAt = newCreatedAt
                                ),
                                formState.mealProducts.mapIndexed { index, product ->
                                    DiaryEntryProduct(
                                        diaryEntryId = entry.id,
                                        name = product.name,
                                        breadUnits = product.breadUnits.toFloatOrNull() ?: 0f,
                                        sortOrder = index
                                    )
                                }
                            )
                            onClose()
                        }
                    },
                    modifier = Modifier.padding(top = 14.dp)
                )
                OutlinedPillButton(
                    text = stringResource(R.string.delete_entry_button),
                    textColor = TextLabel,
                    onClick = {
                        viewModel.deleteEntry(entry)
                        onClose()
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 9.dp)
                )
            }
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
private fun PlainFieldTile(value: String, onValueChange: (String) -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FieldTile)
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(it.filter { c -> c.isDigit() || c == '.' || c == ',' }) },
            textStyle = TextStyle(fontSize = 13.5.sp, color = Ink),
            cursorBrush = SolidColor(Ink),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
