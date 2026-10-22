package com.neojelll.diaxtracker.time

import java.io.File
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppClockTest {

    @After
    fun restore() = AppClock.reset()

    @Test
    fun `a fixed clock stops the app's time`() {
        val zone = ZoneId.of("Europe/Moscow")
        val moment = LocalDateTime.of(2026, 10, 21, 14, 30)
        AppClock.clock = Clock.fixed(moment.atZone(zone).toInstant(), zone)

        assertEquals(moment, AppClock.now())
        assertEquals(moment.toLocalDate(), AppClock.today())
        assertEquals(moment.atZone(zone).toInstant().toEpochMilli(), AppClock.millis())
        assertEquals(zone, AppClock.zone)
    }

    @Test
    fun `reset goes back to the system clock`() {
        AppClock.clock = Clock.fixed(LocalDateTime.of(2000, 1, 1, 0, 0).atZone(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"))
        AppClock.reset()
        assertTrue(AppClock.now().year >= 2026)
    }

    /** A new direct read of the system time would slip past a fixed clock unnoticed - keep them all here. */
    @Test
    fun `nothing reads the system time around the app clock`() {
        val forbidden = Regex("""\b(LocalDateTime|LocalDate|LocalTime|Instant|ZonedDateTime)\.now\(\s*\)|ZoneId\.systemDefault\(\)""")
        val allowed = setOf("AppClock.kt")
        val offenders = File("src/main/java").walkTopDown()
            .filter { it.isFile && it.extension == "kt" && it.name !in allowed }
            .flatMap { file -> file.readLines().mapIndexedNotNull { i, line -> if (forbidden.containsMatchIn(line)) "${file.name}:${i + 1}" else null } }
            .toList()
        assertEquals("Read the time through AppClock: $offenders", emptyList<String>(), offenders)
    }
}
