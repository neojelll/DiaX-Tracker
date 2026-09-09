package com.neojelll.diaxtracker.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatAmountTest {

    @Test
    fun `a whole number drops the decimal point`() {
        assertEquals("3", formatAmount(3.0f))
    }

    @Test
    fun `a fractional value keeps its decimal point`() {
        assertEquals("3.5", formatAmount(3.5f))
    }

    @Test
    fun `zero drops the decimal point`() {
        assertEquals("0", formatAmount(0.0f))
    }
}
