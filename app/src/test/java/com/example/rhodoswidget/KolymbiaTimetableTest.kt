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

    @Test
    fun `search filters complete outbound and return connections`() {
        assertEquals(
            listOf("Lindos"),
            KolymbiaTimetable.searchConnections("  lindOS ").map { it.place }
        )
        assertEquals(
            listOf("Rhodos-Stadt"),
            KolymbiaTimetable.searchConnections("ΡΟΔΟΣ").map { it.place }
        )
        assertTrue(KolymbiaTimetable.searchConnections("Flughafen").isEmpty())
    }

    @Test
    fun `published timetable does not cover the trip start`() {
        assertTrue(KolymbiaTimetable.isValidOn(2026, 9, 10))
        assertTrue(!KolymbiaTimetable.isValidOn(2026, 9, 11))
        assertTrue(!KolymbiaTimetable.isValidForTrip)
    }

    @Test
    fun `detects only an unreviewed official pdf`() {
        assertTrue(!KolymbiaTimetable.hasUnreviewedUpdate(KolymbiaTimetable.REVIEWED_PDF_URL))
        assertTrue(KolymbiaTimetable.hasUnreviewedUpdate("https://www.ktelrodou.gr/wp-content/uploads/new.pdf"))
        assertTrue(!KolymbiaTimetable.hasUnreviewedUpdate(""))
    }

    @Test
    fun `next departure keeps the complete timetable intact`() {
        val departures = listOf("8:50", "9:20", "12:50")

        assertEquals("9:20", KolymbiaTimetable.nextDeparture(departures, 9, 5))
        assertEquals("9:20", KolymbiaTimetable.nextDeparture(departures, 9, 20))
        assertEquals(null, KolymbiaTimetable.nextDeparture(departures, 13, 0))
        assertEquals(3, departures.size)
    }
}
