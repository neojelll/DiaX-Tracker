package com.neojelll.diaxtracker.insulin

import com.neojelll.diaxtracker.data.DiaryEntry
import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.exp

// Peak-to-duration ratio, not an absolute peak time: AndroidAPS's own "rapid-acting" curve pairs
// a 75min peak with a 300min (5h) duration and requires duration >= 5h specifically because that
// pairing distorts (front-loads) once duration shrinks toward 2x the peak. Scaling peak with
// whatever duration the person configures keeps the curve's shape (the `a` parameter below)
// identical to that validated ratio at every duration, rather than assuming one specific
// insulin's timing (Humalog/NovoRapid) regardless of what's actually configured - e.g. Apidra's
// genuinely shorter ~4h action gets a proportionally shorter, still well-conditioned peak instead
// of a mismatched fixed 75min forced onto a duration it wasn't calibrated for.
private const val PEAK_RATIO = 75f / 300f

data class InsulinOnBoard(val units: Float, val minutesLeft: Long, val fromTime: LocalDateTime, val durationMinutes: Float) {
    val progress: Float get() = (minutesLeft / durationMinutes).coerceIn(0f, 1f)
}

/**
 * Fraction of a single dose still on board `elapsedMinutes` after it was taken, using the
 * "scalable exponential" model (Maksimovic; used by Loop/AndroidAPS/OpenAPS) rather than a flat
 * cutoff: activity peaks at [peakMinutes] and both activity and IOB reach exactly zero at
 * [durationMinutes], giving a realistic decay curve instead of a cliff-edge.
 */
fun insulinOnBoardFraction(elapsedMinutes: Float, peakMinutes: Float, durationMinutes: Float): Float {
    if (elapsedMinutes <= 0f) return 1f
    if (elapsedMinutes >= durationMinutes) return 0f
    val tau = peakMinutes * (1 - peakMinutes / durationMinutes) / (1 - 2 * peakMinutes / durationMinutes)
    val a = 2 * tau / durationMinutes
    val s = 1 / (1 - a + (1 + a) * exp(-durationMinutes / tau))
    val t = elapsedMinutes
    return (1 - s * (1 - a) * ((t * t / (tau * durationMinutes * (1 - a)) - t / tau - 1) * exp(-t / tau) + 1))
        .coerceIn(0f, 1f)
}

/** Sums [insulinOnBoardFraction] across every dose still active `durationHours` after it was taken. */
fun activeInsulin(entries: List<DiaryEntry>, now: LocalDateTime, durationHours: Float): InsulinOnBoard? {
    if (entries.isEmpty()) return null
    val durationMinutes = durationHours * 60f
    val active = entries.mapNotNull { entry ->
        val dose = entry.shortInsulinDose ?: return@mapNotNull null
        val elapsed = Duration.between(entry.createdAt, now).toMinutes().toFloat()
        if (elapsed < 0f || elapsed >= durationMinutes) return@mapNotNull null
        val onBoard = dose * insulinOnBoardFraction(elapsed, durationMinutes * PEAK_RATIO, durationMinutes)
        Triple(entry.createdAt, onBoard, durationMinutes - elapsed)
    }
    if (active.isEmpty()) return null
    return InsulinOnBoard(
        units = active.sumOf { it.second.toDouble() }.toFloat(),
        minutesLeft = active.maxOf { it.third }.toLong(),
        fromTime = active.maxOf { it.first },
        durationMinutes = durationMinutes
    )
}
