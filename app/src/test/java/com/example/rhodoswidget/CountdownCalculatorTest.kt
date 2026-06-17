package com.example.rhodoswidget

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class CountdownCalculatorTest {

    @Test
    fun `daysUntilDeparture returns positive before departure`() {
        val days = CountdownCalculator.daysUntilDeparture()
        assertTrue("Expected positive days, got $days", days > 0)
    }

    @Test
    fun `calculate returns non-negative time components`() {
        val r = CountdownCalculator.calculate()
        assertTrue(r.days >= 0)
        assertTrue(r.hours in 0..23)
        assertTrue(r.minutes in 0..59)
        assertTrue(r.seconds in 0..59)
    }

    @Test
    fun `isReached is false before departure day`() {
        val r = CountdownCalculator.calculate()
        // We are well before 20.09.2026 — isReached must be false
        assertFalse(r.isReached)
    }

    @Test
    fun `isOnVacation is false before departure`() {
        val r = CountdownCalculator.calculate()
        assertFalse(r.isOnVacation)
    }

    @Test
    fun `progressFraction is between 0 and 1`() {
        val p = CountdownCalculator.progressFraction()
        assertTrue("progressFraction=$p out of range", p in 0f..1f)
    }

    @Test
    fun `daysUntilDeparture matches calculate days roughly`() {
        val days = CountdownCalculator.daysUntilDeparture().toLong()
        val r = CountdownCalculator.calculate()
        // daysUntilDeparture counts whole calendar days; r.days is time-based — they may differ by 1
        assertTrue(
            "daysUntilDeparture=$days vs calculate.days=${r.days}",
            kotlin.math.abs(days - r.days) <= 1
        )
    }

    @Test
    fun `midnight zeroes out time fields`() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 15)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 45)
            set(Calendar.MILLISECOND, 999)
        }
        val result = CountdownCalculator.midnight(cal)
        assertEquals(0, result.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, result.get(Calendar.MINUTE))
        assertEquals(0, result.get(Calendar.SECOND))
        assertEquals(0, result.get(Calendar.MILLISECOND))
    }
}
