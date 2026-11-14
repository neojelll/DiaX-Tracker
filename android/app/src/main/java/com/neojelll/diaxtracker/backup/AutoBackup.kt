package com.neojelll.diaxtracker.backup

import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** How many automatic backups to keep; the oldest of ours beyond this are deleted after each new one. */
internal const val AUTO_BACKUP_KEEP = 7

/** Local hour the nightly backup aims for. */
internal const val AUTO_BACKUP_HOUR = 21

private val AUTO_BACKUP_NAME = Regex("^diax-auto-\\d{4}-\\d{2}-\\d{2}-\\d{4}\\.zip$")
private val NAME_STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm")

internal fun autoBackupFileName(now: LocalDateTime): String = "diax-auto-${now.format(NAME_STAMP)}.zip"

/**
 * Which of [names] to delete so only the newest [keep] automatic backups remain. Only files named
 * exactly like ours are ever considered, so anything else in the folder (manual exports, the
 * person's own files) can't be touched. The stamp in the name sorts chronologically.
 */
internal fun staleAutoBackups(names: List<String>, keep: Int = AUTO_BACKUP_KEEP): List<String> =
    names.filter { AUTO_BACKUP_NAME.matches(it) }.sortedDescending().drop(keep)

/** Time until the next [hour]:00 - today's if it hasn't come yet, otherwise tomorrow's. */
internal fun delayUntilNextEvening(now: LocalDateTime, hour: Int = AUTO_BACKUP_HOUR): Duration {
    val today = now.toLocalDate().atTime(hour, 0)
    val next = if (now.isBefore(today)) today else today.plusDays(1)
    return Duration.between(now, next)
}
