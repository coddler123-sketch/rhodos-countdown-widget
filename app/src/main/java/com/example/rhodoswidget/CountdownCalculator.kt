package com.example.rhodoswidget

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
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

    fun progressFraction(now: Calendar = Calendar.getInstance()): Float {
        val start = Calendar.getInstance().apply {
            set(Calendar.YEAR, START_YEAR)
            set(Calendar.MONTH, START_MONTH)
            set(Calendar.DAY_OF_MONTH, START_DAY)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val total = (departureCalendar(now).timeInMillis - start.timeInMillis).toFloat()
        val elapsed = (now.timeInMillis - start.timeInMillis).toFloat()
        return (elapsed / total).coerceIn(0f, 1f)
    }

    fun calculate(now: Calendar = Calendar.getInstance()): RemainingTime {
        val target = departureCalendar(now)
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
            isOnVacation = now.timeInMillis >= arrivalCalendar(now).timeInMillis
        )
    }

    fun daysUntilDeparture(now: Calendar = Calendar.getInstance()): Int {
        val today = midnight(now.clone() as Calendar)
        val departure = midnight(departureCalendar(now))
        val diffDays = (departure.timeInMillis - today.timeInMillis) / 86_400_000.0
        return Math.round(diffDays).toInt()
    }

    fun midnight(cal: Calendar): Calendar = cal.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private fun arrivalCalendar(now: Calendar) = (now.clone() as Calendar).apply {
        set(Calendar.YEAR, DEPARTURE_YEAR)
        set(Calendar.MONTH, DEPARTURE_MONTH)
        set(Calendar.DAY_OF_MONTH, DEPARTURE_DAY)
        set(Calendar.HOUR_OF_DAY, ARRIVAL_HOUR)
        set(Calendar.MINUTE, ARRIVAL_MINUTE)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    private fun departureCalendar(now: Calendar) = (now.clone() as Calendar).apply {
        set(Calendar.YEAR, DEPARTURE_YEAR)
        set(Calendar.MONTH, DEPARTURE_MONTH)
        set(Calendar.DAY_OF_MONTH, DEPARTURE_DAY)
        set(Calendar.HOUR_OF_DAY, DEPARTURE_HOUR)
        set(Calendar.MINUTE, DEPARTURE_MINUTE)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}

/**
 * Hilfsfunktion, um ein ISO-Datum ("yyyy-MM-dd") in ein deutsches Wochentagskürzel ("Mo", "Di" etc.) umzuwandeln.
 */
fun String.toShortGermanWeekday(): String {
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(this) ?: return "?"
        val cal = Calendar.getInstance().apply { time = date }
        when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Mo"
            Calendar.TUESDAY -> "Di"
            Calendar.WEDNESDAY -> "Mi"
            Calendar.THURSDAY -> "Do"
            Calendar.FRIDAY -> "Fr"
            Calendar.SATURDAY -> "Sa"
            else -> "So"
        }
    } catch (e: Exception) {
        "?"
    }
}
