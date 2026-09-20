package com.example.rhodoswidget

import com.example.rhodoswidget.R
import com.example.rhodoswidget.MainActivity
import com.example.rhodoswidget.ui.home.*
import com.example.rhodoswidget.ui.compass.*
import com.example.rhodoswidget.ui.travel.*
import com.example.rhodoswidget.ui.news.*
import com.example.rhodoswidget.ui.weather.*
import com.example.rhodoswidget.ui.settings.*
import com.example.rhodoswidget.ui.theme.*
import com.example.rhodoswidget.widget.*

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MarineWeatherTest {
    @Test
    fun `parser maps current marine values`() {
        val result = MarineWeatherParser.parse(
            """{"current":{"sea_surface_temperature":26.4,"wave_height":0.7,"wave_period":4.5,"wave_direction":123}}""",
            fetchedAtMillis = 42L
        )

        assertEquals(26.4, result.seaSurfaceTemperatureCelsius ?: 0.0, 0.001)
        assertEquals(0.7, result.waveHeightMeters ?: 0.0, 0.001)
        assertEquals(4.5, result.wavePeriodSeconds ?: 0.0, 0.001)
        assertEquals(123.0, result.waveDirectionDegrees ?: 0.0, 0.001)
        assertEquals(42L, result.fetchedAtMillis)
    }

    @Test
    fun `parser tolerates missing optional marine values`() {
        val result = MarineWeatherParser.parse("""{"current":{}}""", 7L)

        assertNull(result.seaSurfaceTemperatureCelsius)
        assertNull(result.waveHeightMeters)
        assertNull(result.wavePeriodSeconds)
        assertNull(result.waveDirectionDegrees)
    }
}
