package com.neojelll.diaxtracker.insulin

import com.neojelll.diaxtracker.data.DiaryEntry
import java.time.LocalDate

/**
 * Insulin logged on a day, short- and long-acting kept apart: they do different jobs, so a combined
 * total would hide whether it's 14 + 10 or 4 + 20. [long] is null when no long-acting dose was
 * logged that day - the summary then hides its line.
 */
data class DayInsulin(val short: Float, val long: Float?)

fun insulinOn(day: LocalDate, entries: List<DiaryEntry>): DayInsulin {
    val ofDay = entries.filter { it.createdAt.toLocalDate() == day }
    val short = ofDay.sumOf { (it.shortInsulinDose ?: 0f).toDouble() }.toFloat()
    val longDoses = ofDay.mapNotNull { it.longInsulinDose }
    return DayInsulin(short, longDoses.takeIf { it.isNotEmpty() }?.sumOf { it.toDouble() }?.toFloat())
}
