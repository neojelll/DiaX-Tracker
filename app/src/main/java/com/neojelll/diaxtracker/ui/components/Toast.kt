package com.neojelll.diaxtracker.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.ui.theme.GlukoColors
import com.neojelll.diaxtracker.ui.theme.GlukoType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

/** How long a plain toast stays; one with an undo action stays longer so there's time to react. */
const val TOAST_MS = 3000
const val TOAST_WITH_UNDO_MS = 5000

private val ToastRadius = 20.dp
private val ToastShadow = Color(0x380D0D0D)
private val ToastEase = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)

enum class ToastKind { Ok, Delete, Error }

data class ToastData(
    val text: String,
    val sub: String? = null,
    val kind: ToastKind = ToastKind.Ok,
    val onUndo: (() -> Unit)? = null,
    internal val id: Long = System.nanoTime()
) {
    val durationMs: Int get() = if (onUndo != null) TOAST_WITH_UNDO_MS else TOAST_MS
}

/** Toasts from any screen: `LocalToast.current.show(...)`. */
val LocalToast = compositionLocalOf<ToastState> { error("ToastState is not provided") }

@Composable
fun rememberToastState(): ToastState {
    val restored = stringResource(R.string.toast_restored)
    return remember(restored) { ToastState(restored) }
}

/** One toast at a time: a new one replaces the current one (no queue) and animates in again. */
@Stable
class ToastState(private val restoredText: String) {
    var current by mutableStateOf<ToastData?>(null)
        private set

    fun show(toast: ToastData) {
        current = toast.copy(id = System.nanoTime())
    }

    fun show(text: String, sub: String? = null, kind: ToastKind = ToastKind.Ok, onUndo: (() -> Unit)? = null) =
        show(ToastData(text, sub, kind, onUndo))

    fun hide() {
        current = null
    }

    internal fun undo() {
        val undo = current?.onUndo ?: return
        hide()
        undo()
        show(restoredText)
    }
}

/**
 * Sits above content and sheets. [bottomOffset] is the space to keep between the system
 * navigation bar and the toast - the caller derives it from the real layout (the app's bottom
 * bar, or a small gap while a sheet covers it); the system bar itself is added here from the live
 * inset, so it holds in gesture and 3-button navigation alike.
 */
@Composable
fun BoxScope.ToastHost(state: ToastState, bottomOffset: Dp) {
    val toast = state.current
    val bottom by animateDpAsState(bottomOffset, tween(220), label = "toastBottom")

    LaunchedEffect(toast?.id) {
        if (toast != null) {
            delay(toast.durationMs.toLong())
            if (state.current?.id == toast.id) state.hide()
        }
    }

    AnimatedContent(
        targetState = toast,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(start = 14.dp, end = 14.dp, bottom = bottom),
        transitionSpec = {
            (fadeIn(tween(260, easing = ToastEase)) +
                slideInVertically(tween(260, easing = ToastEase)) { 14 } +
                scaleIn(tween(260, easing = ToastEase), initialScale = 0.98f)) togetherWith
                (fadeOut(tween(160)) + slideOutVertically(tween(160)) { 10 })
        },
        contentKey = { it?.id },
        label = "toast"
    ) { t ->
        if (t != null) ToastCard(t, onClick = state::hide, onUndo = state::undo) else Spacer(Modifier.fillMaxWidth())
    }
}

@Composable
private fun ToastCard(t: ToastData, onClick: () -> Unit, onUndo: () -> Unit) {
    val progress = remember(t.id) { Animatable(1f) }
    LaunchedEffect(t.id) { progress.animateTo(0f, tween(t.durationMs, easing = LinearEasing)) }

    Box(
        Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite }
            .shadow(20.dp, RoundedCornerShape(ToastRadius), ambientColor = ToastShadow, spotColor = ToastShadow)
            .clip(RoundedCornerShape(ToastRadius))
            .background(GlukoColors.Ink)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
    ) {
        Row(
            Modifier.padding(start = 11.dp, top = 11.dp, bottom = 11.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            ToastIcon(t.kind)
            Column(Modifier.weight(1f)) {
                Text(t.text, style = GlukoType.Body.copy(fontSize = 13.sp, lineHeight = 17.sp, color = Color.White))
                if (!t.sub.isNullOrEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        t.sub,
                        style = GlukoType.Body.copy(fontSize = 11.sp, color = GlukoColors.TextOnDark),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (t.onUndo != null) {
                // The pill stays its designed size; the padded wrapper only widens the touch target.
                Box(
                    Modifier
                        .minimumInteractiveComponentSize()
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onUndo),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White)
                            .padding(horizontal = 13.dp, vertical = 8.dp)
                    ) {
                        Text(stringResource(R.string.toast_undo), style = GlukoType.Body.copy(fontSize = 12.sp, color = GlukoColors.Ink))
                    }
                }
            }
        }
        // Time left, draining left to right.
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(2.dp)
                .graphicsLayer {
                    scaleX = progress.value
                    transformOrigin = TransformOrigin(0f, 0.5f)
                }
                .background(Color.White.copy(alpha = 0.28f))
        )
    }
}

@Composable
private fun ToastIcon(kind: ToastKind) {
    val background = if (kind == ToastKind.Delete) Color.White.copy(alpha = 0.14f) else Color.White
    Box(Modifier.size(30.dp).clip(RoundedCornerShape(999.dp)).background(background), contentAlignment = Alignment.Center) {
        when (kind) {
            ToastKind.Ok -> LucideIcon(LucidePaths.Check, 15.dp, GlukoColors.Ink, strokeWidth = 2.4f)
            ToastKind.Delete -> LucideIcon(LucidePaths.Trash, 15.dp, Color.White, strokeWidth = 1.9f)
            ToastKind.Error -> LucideIcon(LucidePaths.Alert, 15.dp, GlukoColors.Ink, strokeWidth = 2.4f)
        }
    }
}

/** "Today, 09:41" for today's moments, "10 Sep, 13:20" otherwise. */
internal fun toastMoment(moment: LocalDateTime, today: LocalDate, todayFormat: String, locale: Locale): String {
    val time = moment.format(DateTimeFormatter.ofPattern("HH:mm"))
    return if (moment.toLocalDate() == today) todayFormat.format(time)
    else moment.format(DateTimeFormatter.ofPattern("d MMM, HH:mm", locale))
}

/** The texts of the app's toasts, resolved from string resources. */
class AppToasts(private val context: android.content.Context, private val state: ToastState) {
    private fun s(id: Int, vararg args: Any) = context.getString(id, *args)

    private fun moment(at: LocalDateTime) =
        toastMoment(at, LocalDate.now(), context.getString(R.string.toast_when_today, "%1\$s"), Locale.getDefault())

    fun recordSaved(at: LocalDateTime) = state.show(s(R.string.toast_record_saved), moment(at))
    fun recordEdited() = state.show(s(R.string.toast_record_edited))
    fun recordDeleted(at: LocalDateTime, undo: () -> Unit) =
        state.show(s(R.string.toast_record_deleted), moment(at), ToastKind.Delete, undo)

    fun presetSaved(name: String, isNew: Boolean) =
        state.show(s(if (isNew) R.string.toast_preset_created else R.string.toast_preset_updated), name)
    fun presetDeleted(name: String, undo: () -> Unit) =
        state.show(s(R.string.toast_preset_deleted), name, ToastKind.Delete, undo)

    fun allDeleted(undo: () -> Unit) = state.show(s(R.string.toast_all_deleted), kind = ToastKind.Delete, onUndo = undo)

    fun exported(fileName: String?) = state.show(s(R.string.toast_export_done), fileName)
    fun imported(summary: String) = state.show(s(R.string.toast_import_done), summary)
    fun error(text: String, reason: String? = null) = state.show(text, reason, ToastKind.Error)

    fun backup(enabled: Boolean, folder: String? = null) =
        if (enabled) state.show(s(R.string.toast_backup_on), folder?.let { s(R.string.toast_backup_every_evening, it) })
        else state.show(s(R.string.toast_backup_off))
}

@Composable
fun rememberAppToasts(): AppToasts {
    val context = LocalContext.current
    val state = LocalToast.current
    return remember(context, state) { AppToasts(context, state) }
}
