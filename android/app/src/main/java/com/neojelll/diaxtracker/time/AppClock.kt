package com.neojelll.diaxtracker.time

import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * The one place the app reads the current time from. The system clock in normal use; a test fixes it
 * to a chosen moment (screenshots that look the same on every run, logic checked on any date).
 * Code never calls `LocalDateTime.now()` / `System.currentTimeMillis()` directly - it asks here.
 */
object AppClock {
    @Volatile
    var clock: Clock = Clock.systemDefaultZone()

    fun now(): LocalDateTime = LocalDateTime.now(clock)

    fun today(): LocalDate = LocalDate.now(clock)

    fun millis(): Long = clock.millis()

    /** The clock's time zone - for turning the app's local times into epoch millis and back. */
    val zone: ZoneId get() = clock.zone

    /** Back to the system clock - for tests that fixed it. */
    fun reset() {
        clock = Clock.systemDefaultZone()
    }
}
