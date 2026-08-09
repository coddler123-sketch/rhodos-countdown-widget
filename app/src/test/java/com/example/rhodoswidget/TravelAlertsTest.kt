package com.example.rhodoswidget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class TravelAlertsTest {
    @Test
    fun `high uv and rough sea create German alerts`() {
        val weather = weather(uv = 8.0, wind = 31)
        val marine = MarineWeather(null, 1.4, null, null, 0L)

        val alerts = travelAlerts(weather, marine, emptyList(), 1_800_000_000_000L)

        assertEquals(setOf("Hoher UV-Wert auf Rhodos", "Wind und Wellen beachten"), alerts.map { it.title }.toSet())
    }

    @Test
    fun `event within next day creates privacy safe German message`() {
        val now = 1_800_000_000_000L
        val start = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
            .format(now + TimeUnit.HOURS.toMillis(6))
        val event = RhodesEvent(7, "Μουσική βραδιά", start, start, null, "https://www.rhodes.gr/event/7/")

        val alert = travelAlerts(null, null, listOf(event), now).single()

        assertEquals("Veranstaltung auf Rhodos", alert.title)
        assertTrue(alert.message.startsWith("Eine Veranstaltung der Gemeinde"))
    }

    private fun weather(uv: Double, wind: Int) = RhodosWeather(
        temperatureCelsius = 30,
        apparentTemperatureCelsius = 32,
        relativeHumidityPercent = 50,
        precipitationMm = 0.0,
        windSpeedKmh = wind,
        weatherCode = 0,
        isDay = true,
        uvIndex = uv
    )
}
