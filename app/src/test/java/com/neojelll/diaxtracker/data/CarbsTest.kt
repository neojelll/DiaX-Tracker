package com.neojelll.diaxtracker.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CarbsTest {
    @Test
    fun `typed XE becomes grams at 10 g per XE and shows back the same`() {
        assertEquals(30f, parseXeToGrams("3")!!, 1e-4f)
        assertEquals(25f, parseXeToGrams("2.5")!!, 1e-4f)
        assertEquals(25f, parseXeToGrams("2,5")!!, 1e-4f)
        assertEquals("3", formatXe(30f))
        assertEquals("2.5", formatXe(25f))
    }

    @Test
    fun `XE are shown to one decimal without a trailing zero`() {
        assertEquals("2.1", formatXe(25f, gramsPerXe = 12f))
        assertEquals("3.3", formatXe(40f, gramsPerXe = 12f))
        assertEquals("1", formatXe(10.4f))
        assertEquals("0.5", formatXe(5f))
        assertEquals("0", formatXe(0f))
    }

    @Test
    fun `totals are summed in grams and rounded once`() {
        val products = listOf(10.4f, 10.4f, 10.4f)
        assertEquals("3.1", formatXe(products.sum()))
    }

    @Test
    fun `not a number parses to null`() {
        assertNull(parseXeToGrams(""))
        assertNull(parseXeToGrams("abc"))
    }
}
