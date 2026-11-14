package com.neojelll.diaxtracker.ui.screens

import com.neojelll.diaxtracker.data.DiaryEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class RecordRowTest {

    private fun entry(
        sugar: Float? = null,
        carbs: Float? = null,
        meal: String? = null,
        short: Float? = null,
        long: Float? = null
    ) = DiaryEntry(
        bloodSugar = sugar, carbsGrams = carbs, mealLabel = meal,
        shortInsulinDose = short, longInsulinDose = long, notes = ""
    )

    @Test
    fun `food makes it a meal even with insulin`() {
        assertEquals(RecordKind.MEAL, entry(carbs = 40f).recordKind())
        assertEquals(RecordKind.MEAL, entry(meal = "Гречка").recordKind())
        assertEquals(RecordKind.MEAL, entry(carbs = 40f, short = 4f, long = 18f, sugar = 6f).recordKind())
    }

    @Test
    fun `insulin without food is a shot`() {
        assertEquals(RecordKind.SHOT, entry(long = 18f).recordKind())
        assertEquals(RecordKind.SHOT, entry(short = 3f, sugar = 11.8f).recordKind())
    }

    @Test
    fun `sugar alone is a check`() {
        assertEquals(RecordKind.CHECK, entry(sugar = 6.4f).recordKind())
    }

    @Test
    fun `number and unit are joined by a narrow no-break space`() {
        assertEquals("4 ХЕ", numberWithUnit("%1\$s ХЕ", "4").text)
        assertEquals("18 ед", numberWithUnit("%1\$s ед", "18").text)
        assertEquals("2.5 BU", numberWithUnit("%1\$s BU", "2.5").text)
    }
}
