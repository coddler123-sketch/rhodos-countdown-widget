package com.example.rhodoswidget

import java.util.Calendar
import java.util.concurrent.TimeUnit

data class RemainingTime(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long = 0L,
    val isReached: Boolean = false,
    val isOnVacation: Boolean = false
)

object CountdownCalculator {

    const val DEPARTURE_YEAR = 2026
    const val DEPARTURE_MONTH = Calendar.SEPTEMBER
    const val DEPARTURE_DAY = 20
    const val DEPARTURE_HOUR = 14
    const val DEPARTURE_MINUTE = 30

    // Ankunft Rhodos ca. 19:00 Ortszeit (= deutsche Ortszeit, Zeitzone identisch im Sommer)
    private const val ARRIVAL_HOUR = 19
    private const val ARRIVAL_MINUTE = 0

    // Startpunkt des Countdowns (genau 1 Jahr vor Abflug)
    private const val START_YEAR = 2025
    private const val START_MONTH = Calendar.SEPTEMBER
    private const val START_DAY = 20

    fun progressFraction(): Float {
        val start = Calendar.getInstance().apply {
            set(Calendar.YEAR, START_YEAR)
            set(Calendar.MONTH, START_MONTH)
            set(Calendar.DAY_OF_MONTH, START_DAY)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val total = (departureCalendar().timeInMillis - start.timeInMillis).toFloat()
        val elapsed = (Calendar.getInstance().timeInMillis - start.timeInMillis).toFloat()
        return (elapsed / total).coerceIn(0f, 1f)
    }

    fun calculate(): RemainingTime {
        val now = Calendar.getInstance()
        val target = departureCalendar()
        val arrivalDayStart = midnight(target.clone() as Calendar)
        val remainingMillis = (target.timeInMillis - now.timeInMillis).coerceAtLeast(0L)
        val days = TimeUnit.MILLISECONDS.toDays(remainingMillis)
        val afterDays = remainingMillis - TimeUnit.DAYS.toMillis(days)
        val hours = TimeUnit.MILLISECONDS.toHours(afterDays)
        val afterHours = afterDays - TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(afterHours)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(afterHours - TimeUnit.MINUTES.toMillis(minutes))
        return RemainingTime(
            days = days,
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            isReached = now.timeInMillis >= arrivalDayStart.timeInMillis,
            isOnVacation = now.timeInMillis >= arrivalCalendar().timeInMillis
        )
    }

    fun daysUntilDeparture(): Int {
        val today = midnight(Calendar.getInstance())
        val departure = midnight(departureCalendar())
        val diffDays = (departure.timeInMillis - today.timeInMillis) / 86_400_000.0
        return Math.round(diffDays).toInt()
    }

    fun midnight(cal: Calendar): Calendar = cal.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private fun arrivalCalendar() = Calendar.getInstance().apply {
        set(Calendar.YEAR, DEPARTURE_YEAR)
        set(Calendar.MONTH, DEPARTURE_MONTH)
        set(Calendar.DAY_OF_MONTH, DEPARTURE_DAY)
        set(Calendar.HOUR_OF_DAY, ARRIVAL_HOUR)
        set(Calendar.MINUTE, ARRIVAL_MINUTE)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private fun departureCalendar() = Calendar.getInstance().apply {
        set(Calendar.YEAR, DEPARTURE_YEAR)
        set(Calendar.MONTH, DEPARTURE_MONTH)
        set(Calendar.DAY_OF_MONTH, DEPARTURE_DAY)
        set(Calendar.HOUR_OF_DAY, DEPARTURE_HOUR)
        set(Calendar.MINUTE, DEPARTURE_MINUTE)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}
