package com.neojelll.diaxtracker.sensor

import org.junit.Assert.assertEquals
import org.junit.Test

class GlucoseUnitsTest {

    @Test
    fun `converts a typical mg over dL reading to mmol per L`() {
        assertEquals(5.55f, mgdlToMmol(100.0), 0.01f)
    }

    @Test
    fun `zero stays zero`() {
        assertEquals(0f, mgdlToMmol(0.0), 0.0f)
    }
}
