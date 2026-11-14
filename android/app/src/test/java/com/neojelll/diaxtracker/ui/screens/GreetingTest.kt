package com.neojelll.diaxtracker.ui.screens

import com.neojelll.diaxtracker.R
import org.junit.Assert.assertEquals
import org.junit.Test

class GreetingTest {
    @Test
    fun `the greeting changes at the edges of each part of the day`() {
        assertEquals(R.string.greeting_night, greetingFor(4))
        assertEquals(R.string.greeting_morning, greetingFor(5))
        assertEquals(R.string.greeting_morning, greetingFor(10))
        assertEquals(R.string.greeting_afternoon, greetingFor(11))
        assertEquals(R.string.greeting_afternoon, greetingFor(16))
        assertEquals(R.string.greeting_evening, greetingFor(17))
        assertEquals(R.string.greeting_evening, greetingFor(22))
        assertEquals(R.string.greeting_night, greetingFor(23))
        assertEquals(R.string.greeting_night, greetingFor(0))
    }
}
