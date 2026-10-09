package com.neojelll.diaxtracker.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

/**
 * A tap on an empty spot of this area closes the keyboard and drops text focus - Compose doesn't do
 * that on its own. Children get the tap first: buttons and text fields consume it (so a tap on
 * another field moves focus there), and a scroll or drag isn't a tap. No click semantics, so a
 * screen reader doesn't announce the whole area as a button.
 */
fun Modifier.clearFocusOnTap(): Modifier = composed {
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    pointerInput(focusManager, keyboard) {
        detectTapGestures(onTap = {
            keyboard?.hide()
            focusManager.clearFocus()
        })
    }
}
