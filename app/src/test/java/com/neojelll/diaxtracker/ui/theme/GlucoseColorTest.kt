package com.neojelll.diaxtracker.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class GlucoseColorTest {

    private val low = 4.0f
    private val high = 9.0f

    @Test
    fun `below the fixed very-low threshold is critical red`() {
        assertEquals(CriticalRed, glucoseColor(2.9f, low, high))
    }

    @Test
    fun `between very-low and low thresholds is danger red`() {
        assertEquals(DangerRed, glucoseColor(3.0f, low, high))
        assertEquals(DangerRed, glucoseColor(3.9f, low, high))
    }

    @Test
    fun `the fixed 3-9 low threshold wins even if the user's target range is looser`() {
        // low bound is 3.5, but the clinical 3.9 threshold must still apply
        assertEquals(DangerRed, glucoseColor(3.8f, 3.5f, 8.0f))
    }

    @Test
    fun `within the target range is green`() {
        assertEquals(SproutGreen, glucoseColor(low, low, high))
        assertEquals(SproutGreen, glucoseColor(high, low, high))
    }

    @Test
    fun `just above the high bound is warning yellow`() {
        assertEquals(WarningYellow, glucoseColor(9.1f, low, high))
        assertEquals(WarningYellow, glucoseColor(12.9f, low, high))
    }

    @Test
    fun `far above the high bound is warning orange`() {
        assertEquals(WarningOrange, glucoseColor(13.0f, low, high))
    }
}
