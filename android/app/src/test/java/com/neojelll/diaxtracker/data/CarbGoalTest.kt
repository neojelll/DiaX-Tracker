package com.neojelll.diaxtracker.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CarbGoalTest {
    @Test
    fun `a goal is in effect only when switched on and above zero`() {
        assertEquals(150f, CarbGoal(enabled = true, grams = 150f).activeGrams)
        assertNull(CarbGoal(enabled = false, grams = 150f).activeGrams)
        assertNull(CarbGoal(enabled = true, grams = null).activeGrams)
        assertNull(CarbGoal(enabled = true, grams = 0f).activeGrams)
    }

    @Test
    fun `progress runs from empty to full and stays full past the goal`() {
        assertEquals(0f, carbGoalProgress(0f, 150f), 1e-4f)
        assertEquals(0.5f, carbGoalProgress(75f, 150f), 1e-4f)
        assertEquals(1f, carbGoalProgress(150f, 150f), 1e-4f)
        assertEquals(1f, carbGoalProgress(200f, 150f), 1e-4f)
    }

    @Test
    fun `the goal is reached at it and past it`() {
        assertFalse(carbGoalReached(149f, 150f))
        assertTrue(carbGoalReached(150f, 150f))
        assertTrue(carbGoalReached(180f, 150f))
    }

    @Test
    fun `a step is half an XE in XE and 5 g in grams`() {
        assertEquals(6f, carbGoalStepGrams(CarbDisplay(CarbUnit.XE, gramsPerXe = 12f)), 1e-4f)
        assertEquals(5f, carbGoalStepGrams(CarbDisplay(CarbUnit.XE, gramsPerXe = 10f)), 1e-4f)
        assertEquals(5f, carbGoalStepGrams(CarbDisplay(CarbUnit.GRAMS)), 1e-4f)
    }

    @Test
    fun `stepping starts from zero when unset and stays within bounds`() {
        val xe12 = CarbDisplay(CarbUnit.XE, gramsPerXe = 12f)
        assertEquals(6f, stepCarbGoal(null, 1, xe12), 1e-4f)
        assertEquals(156f, stepCarbGoal(150f, 1, xe12), 1e-4f)
        assertEquals(0f, stepCarbGoal(3f, -1, xe12), 1e-4f)
        assertEquals(MAX_CARB_GOAL_GRAMS, stepCarbGoal(998f, 1, CarbDisplay(CarbUnit.GRAMS)), 1e-4f)
    }
}
