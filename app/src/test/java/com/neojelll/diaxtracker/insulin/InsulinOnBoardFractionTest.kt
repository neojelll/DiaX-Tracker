package com.neojelll.diaxtracker.insulin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InsulinOnBoardFractionTest {

    @Test
    fun `at the moment of the dose the full amount is on board`() {
        assertEquals(1f, insulinOnBoardFraction(0f, 75f, 300f), 0f)
    }

    @Test
    fun `before the dose is taken the full amount is still reported on board`() {
        assertEquals(1f, insulinOnBoardFraction(-5f, 75f, 300f), 0f)
    }

    @Test
    fun `at the configured duration nothing is left on board`() {
        assertEquals(0f, insulinOnBoardFraction(300f, 75f, 300f), 0f)
    }

    @Test
    fun `past the configured duration nothing is left on board`() {
        assertEquals(0f, insulinOnBoardFraction(400f, 75f, 300f), 0f)
    }

    @Test
    fun `the fraction only ever decreases as time passes`() {
        var previous = insulinOnBoardFraction(0f, 75f, 300f)
        var t = 10f
        while (t <= 300f) {
            val current = insulinOnBoardFraction(t, 75f, 300f)
            assertTrue("fraction rose between t=${t - 10f} and t=$t", current <= previous)
            previous = current
            t += 10f
        }
    }

    @Test
    fun `the fraction never leaves the 0 to 1 range`() {
        var t = -50f
        while (t <= 350f) {
            val fraction = insulinOnBoardFraction(t, 75f, 300f)
            assertTrue("fraction $fraction at t=$t out of range", fraction in 0f..1f)
            t += 5f
        }
    }

    @Test
    fun `is a scalable curve - same elapsed proportion gives the same fraction at any duration`() {
        // 150/300 and 100/200 are both 50% elapsed, with peak held at the same 25% ratio.
        val atHalfOfFiveHours = insulinOnBoardFraction(150f, 75f, 300f)
        val atHalfOfThreeAndAHalfHours = insulinOnBoardFraction(100f, 50f, 200f)
        assertEquals(atHalfOfFiveHours, atHalfOfThreeAndAHalfHours, 0.0001f)
    }

    @Test
    fun `matches a hand-derived reference value at the peak`() {
        // Worked by hand from the formula itself (tau=112.5, a=0.75, S=~2.6911) at t=peak=75,
        // duration=300 - not from an independent source, but a concrete pinned number that a
        // structurally-valid-but-wrong formula (still monotonic, still bounded, still scalable)
        // would not coincidentally reproduce. Catches a corrupted formula that the property
        // tests above could miss.
        assertEquals(0.6726f, insulinOnBoardFraction(75f, 75f, 300f), 0.001f)
    }

    @Test
    fun `changing the peak while holding duration fixed actually changes the curve`() {
        // Guards against peakMinutes silently being a dead parameter.
        val earlierPeak = insulinOnBoardFraction(100f, 50f, 300f)
        val laterPeak = insulinOnBoardFraction(100f, 100f, 300f)
        assertTrue(earlierPeak != laterPeak)
    }

    @Test
    fun `a peak at exactly half the duration is a known unsafe input`() {
        // tau's denominator is (1 - 2*peakMinutes/durationMinutes), which is exactly zero here -
        // division by zero. activeInsulin() never hits this (PEAK_RATIO is fixed at 0.25, i.e.
        // peak is always a quarter of duration, never half), but insulinOnBoardFraction is a
        // public function now - this test documents the one input shape any future caller must
        // never pass, rather than leaving it as a silent landmine.
        val fraction = insulinOnBoardFraction(150f, 150f, 300f)
        assertTrue(fraction.isNaN() || fraction.isInfinite())
    }
}
