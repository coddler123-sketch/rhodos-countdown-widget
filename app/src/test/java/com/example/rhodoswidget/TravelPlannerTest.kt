package com.example.rhodoswidget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TravelPlannerTest {
    @Test
    fun `planning tip rotates daily and repeats after all six tips`() {
        val firstCycle = (1..6).map(::planningDayPlan)

        assertEquals(DayPlanKind.entries.toSet(), firstCycle.toSet())
        assertEquals(planningDayPlan(1), planningDayPlan(7))
        assertEquals(planningDayPlan(365), planningDayPlan(371))
    }

    @Test
    fun `rain recommends protected culture program`() {
        assertEquals(
            DayPlanKind.OLD_TOWN,
            recommendDayPlan(weather(precipitation = 1.0), marine(), 12)
        )
    }

    @Test
    fun `strong wind or waves recommends inland destination`() {
        assertEquals(DayPlanKind.INLAND, recommendDayPlan(weather(wind = 30), marine(), 12))
        assertEquals(DayPlanKind.INLAND, recommendDayPlan(weather(), marine(waves = 1.3), 12))
        assertTrue(isMarineCaution(weather(), marine(waves = 1.3)))
    }

    @Test
    fun `high midday uv recommends shade`() {
        assertEquals(DayPlanKind.SHADE, recommendDayPlan(weather(uv = 8.0), marine(), 13))
    }

    @Test
    fun `time selects morning beach and evening ideas`() {
        assertEquals(DayPlanKind.LINDOS_EARLY, recommendDayPlan(weather(), marine(), 9))
        assertEquals(DayPlanKind.BEACH, recommendDayPlan(weather(), marine(), 14))
        assertEquals(DayPlanKind.EVENING, recommendDayPlan(weather(), marine(), 19))
        assertFalse(isMarineCaution(weather(), marine()))
    }

    private fun weather(
        wind: Int = 10,
        precipitation: Double = 0.0,
        uv: Double = 3.0
    ) = RhodosWeather(
        temperatureCelsius = 28,
        apparentTemperatureCelsius = 29,
        relativeHumidityPercent = 55,
        precipitationMm = precipitation,
        windSpeedKmh = wind,
        weatherCode = 0,
        isDay = true,
        uvIndex = uv
    )

    private fun marine(waves: Double = 0.4) = MarineWeather(
        seaSurfaceTemperatureCelsius = 26.0,
        waveHeightMeters = waves,
        wavePeriodSeconds = 4.0,
        waveDirectionDegrees = 100.0,
        fetchedAtMillis = 1L
    )
}
