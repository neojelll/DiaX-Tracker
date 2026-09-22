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
}
