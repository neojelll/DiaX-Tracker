package com.neojelll.diaxtracker.insulin

import com.neojelll.diaxtracker.data.DiaryEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

private val NOW: LocalDateTime = LocalDateTime.of(2026, 1, 1, 12, 0)
private const val DURATION_HOURS = 5f

private fun doseEntry(dose: Float?, minutesAgo: Long) = DiaryEntry(
    bloodSugar = null,
    shortInsulinDose = dose,
    longInsulinDose = null,
    notes = "",
    createdAt = NOW.minusMinutes(minutesAgo)
)

class ActiveInsulinTest {

    @Test
    fun `no entries means nothing on board`() {
        assertNull(activeInsulin(emptyList(), NOW, DURATION_HOURS))
    }

    @Test
    fun `entries without a short insulin dose are ignored`() {
        val sugarOnly = doseEntry(dose = null, minutesAgo = 0)
        assertNull(activeInsulin(listOf(sugarOnly), NOW, DURATION_HOURS))
    }

    @Test
    fun `a dose older than the configured duration is fully decayed and ignored`() {
        val stale = doseEntry(dose = 4f, minutesAgo = (DURATION_HOURS * 60).toLong())
        assertNull(activeInsulin(listOf(stale), NOW, DURATION_HOURS))
    }

    @Test
    fun `a dose timestamped in the future is ignored`() {
        val fromTheFuture = doseEntry(dose = 4f, minutesAgo = -10)
        assertNull(activeInsulin(listOf(fromTheFuture), NOW, DURATION_HOURS))
    }

    @Test
    fun `a single fresh dose reports its full amount as on board`() {
        val result = activeInsulin(listOf(doseEntry(dose = 4f, minutesAgo = 0)), NOW, DURATION_HOURS)
        assertEquals(4f, result!!.units, 0.001f)
    }

    @Test
    fun `overlapping doses stack instead of replacing each other`() {
        val older = doseEntry(dose = 2f, minutesAgo = 60)
        val newer = doseEntry(dose = 3f, minutesAgo = 0)
        val durationMinutes = DURATION_HOURS * 60f
        val peakMinutes = durationMinutes * (75f / 300f)
        val expected = 2f * insulinOnBoardFraction(60f, peakMinutes, durationMinutes) +
            3f * insulinOnBoardFraction(0f, peakMinutes, durationMinutes)

        val result = activeInsulin(listOf(older, newer), NOW, DURATION_HOURS)

        assertEquals(expected, result!!.units, 0.001f)
    }

    @Test
    fun `minutesLeft and fromTime track the most recently taken active dose`() {
        val older = doseEntry(dose = 1f, minutesAgo = 120)
        val newer = doseEntry(dose = 1f, minutesAgo = 30)

        val result = activeInsulin(listOf(older, newer), NOW, DURATION_HOURS)!!

        assertEquals(NOW.minusMinutes(30), result.fromTime)
        assertEquals((DURATION_HOURS * 60 - 30).toLong(), result.minutesLeft)
    }

    @Test
    fun `filters correctly when valid and invalid entries are mixed in one list`() {
        val sugarOnly = doseEntry(dose = null, minutesAgo = 5)
        val stale = doseEntry(dose = 9f, minutesAgo = (DURATION_HOURS * 60).toLong() + 10)
        val fromTheFuture = doseEntry(dose = 9f, minutesAgo = -5)
        val valid = doseEntry(dose = 4f, minutesAgo = 0)

        val result = activeInsulin(listOf(sugarOnly, stale, fromTheFuture, valid), NOW, DURATION_HOURS)

        assertEquals(4f, result!!.units, 0.001f)
    }

    @Test
    fun `progress is coerced into 0 to 1 even if minutesLeft would fall outside it`() {
        val overLong = InsulinOnBoard(units = 1f, minutesLeft = 500L, fromTime = NOW, durationMinutes = 300f)
        val negative = InsulinOnBoard(units = 1f, minutesLeft = -10L, fromTime = NOW, durationMinutes = 300f)

        assertEquals(1f, overLong.progress, 0f)
        assertEquals(0f, negative.progress, 0f)
    }
}
