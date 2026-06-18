package com.example.rhodoswidget

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class CountdownCalculatorTest {

    private fun getMockToday(year: Int, month: Int, day: Int, hour: Int = 12, minute: Int = 0): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    @Test
    fun `daysUntilDeparture returns positive before departure`() {
        val today = getMockToday(2026, Calendar.JUNE, 15)
        val days = CountdownCalculator.daysUntilDeparture(today)
        assertTrue("Expected positive days, got $days", days > 0)
    }

    @Test
    fun `calculate returns non-negative time components`() {
        val today = getMockToday(2026, Calendar.JUNE, 15)
        val r = CountdownCalculator.calculate(today)
        assertTrue(r.days >= 0)
        assertTrue(r.hours in 0..23)
        assertTrue(r.minutes in 0..59)
        assertTrue(r.seconds in 0..59)
    }

    @Test
    fun `isReached is false before departure day`() {
        val today = getMockToday(2026, Calendar.SEPTEMBER, 19, 23, 59)
        val r = CountdownCalculator.calculate(today)
        assertFalse(r.isReached)
    }

    @Test
    fun `isOnVacation is false before departure`() {
        val today = getMockToday(2026, Calendar.SEPTEMBER, 20, 14, 29)
        val r = CountdownCalculator.calculate(today)
        assertFalse(r.isOnVacation)
    }

    @Test
    fun `progressFraction is between 0 and 1`() {
        val today = getMockToday(2026, Calendar.JUNE, 15)
        val p = CountdownCalculator.progressFraction(today)
        assertTrue("progressFraction=$p out of range", p in 0f..1f)
    }

    @Test
    fun `daysUntilDeparture matches calculate days roughly`() {
        val today = getMockToday(2026, Calendar.JUNE, 15)
        val days = CountdownCalculator.daysUntilDeparture(today).toLong()
        val r = CountdownCalculator.calculate(today)
        assertTrue(
            "daysUntilDeparture=$days vs calculate.days=${r.days}",
            kotlin.math.abs(days - r.days) <= 1
        )
    }

    @Test
    fun `isReached is true on departure day but isOnVacation is false before arrival`() {
        val departureDayNoon = getMockToday(2026, Calendar.SEPTEMBER, 20, 15, 0)
        val r = CountdownCalculator.calculate(departureDayNoon)
        assertTrue(r.isReached)
        assertFalse(r.isOnVacation)
    }

    @Test
    fun `isOnVacation is true after arrival time`() {
        val vacationTime = getMockToday(2026, Calendar.SEPTEMBER, 20, 19, 30)
        val r = CountdownCalculator.calculate(vacationTime)
        assertTrue(r.isReached)
        assertTrue(r.isOnVacation)
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
