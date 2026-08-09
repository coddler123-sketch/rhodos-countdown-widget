package com.example.rhodoswidget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LindosTimetableTest {
    @Test fun `official directions are not reversed`() {
        val fromLindosToRhodes = LindosTimetable.fromLindos.first { it.place == "Rhodos-Stadt" }
        val fromRhodesToLindos = LindosTimetable.toLindos.first { it.place == "Rhodos-Stadt" }

        assertEquals("6:45", fromLindosToRhodes.departureTimes.first())
        assertEquals("6:15", fromRhodesToLindos.departureTimes.first())
        assertFalse(LindosTimetable.toLindos.any { it.place == "Archangelos" })
    }

    @Test fun `all timetable values use German prices and valid times`() {
        val routes = LindosTimetable.fromLindos + LindosTimetable.toLindos
        assertTrue(routes.all { it.price.matches(Regex("\\d,\\d{2} €")) })
        assertTrue(routes.flatMap { it.departureTimes }.all { it.matches(Regex("(?:[01]?\\d|2[0-3]):[0-5]\\d")) })
    }

    @Test fun `complete official overview contains both directions`() {
        assertEquals(10, LindosTimetable.fromLindos.size)
        assertEquals(2, LindosTimetable.toLindos.size)
        assertEquals(listOf("10:30", "14:30"), LindosTimetable.fromLindos.first { it.place == "Prasonisi" }.departureTimes)
        assertEquals(listOf("13:00", "16:00"), LindosTimetable.toLindos.first { it.place == "Prasonisi" }.departureTimes)
    }
}
