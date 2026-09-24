package com.neojelll.diaxtracker.sensor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime

class PendingFollowUpsTest {

    private val entryTime = LocalDateTime.of(2026, 1, 1, 12, 0)

    @Test
    fun `a fresh entry schedules all four checks at exactly +1h to +4h`() {
        val followUps = pendingFollowUps(entryTime, now = entryTime)

        assertEquals(listOf(1L, 2L, 3L, 4L), followUps.map { it.hours })
        assertEquals(
            listOf(13, 14, 15, 16).map { LocalDateTime.of(2026, 1, 1, it, 0) },
            followUps.map { it.target }
        )
    }

    @Test
    fun `each delay is measured from now to the target, not from the entry`() {
        val followUps = pendingFollowUps(entryTime, now = entryTime.plusMinutes(30))

        assertEquals(
            listOf(Duration.ofMinutes(30), Duration.ofMinutes(90), Duration.ofMinutes(150), Duration.ofMinutes(210)),
            followUps.map { it.delay }
        )
    }

    @Test
    fun `checks whose target already passed are not scheduled again`() {
        val followUps = pendingFollowUps(entryTime, now = entryTime.plusMinutes(130))

        assertEquals(listOf(3L, 4L), followUps.map { it.hours })
    }

    @Test
    fun `a backdated entry only gets the checks that are still ahead`() {
        // Logged at 12:30 for a dose taken at 10:00: 11:00 and 12:00 are gone, 13:00 and 14:00 aren't.
        val followUps = pendingFollowUps(entryTime.minusHours(2), now = entryTime.plusMinutes(30))

        assertEquals(listOf(3L, 4L), followUps.map { it.hours })
        assertEquals(Duration.ofMinutes(30), followUps.first().delay)
    }

    @Test
    fun `a target that is exactly now counts as passed`() {
        val followUps = pendingFollowUps(entryTime, now = entryTime.plusHours(1))

        assertEquals(listOf(2L, 3L, 4L), followUps.map { it.hours })
    }

    @Test
    fun `an old entry schedules nothing at all`() {
        assertTrue(pendingFollowUps(entryTime, now = entryTime.plusDays(3)).isEmpty())
    }
}
