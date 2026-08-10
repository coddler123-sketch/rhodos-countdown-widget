package com.example.rhodoswidget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KolymbiaTimetableTest {
    @Test
    fun `contains the official routes from and to Kolymbia`() {
        assertEquals(
            setOf("Rhodos-Stadt", "Lindos", "Tsambika-Strand", "Sieben Quellen", "Kallithea-Thermen", "Prasonisi"),
            KolymbiaTimetable.fromKolymbia.map { it.place }.toSet()
        )
        assertEquals(
            KolymbiaTimetable.fromKolymbia.map { it.place }.toSet(),
            KolymbiaTimetable.toKolymbia.map { it.place }.toSet()
        )
    }

    @Test
    fun `contains valid prices and departure times`() {
        val routes = KolymbiaTimetable.fromKolymbia + KolymbiaTimetable.toKolymbia

        assertTrue(routes.all { it.price.matches(Regex("\\d,\\d{2} €")) })
        assertTrue(routes.flatMap { it.departureTimes }.all { it.matches(Regex("(?:[01]?\\d|2[0-3]):[0-5]\\d")) })
    }
}
