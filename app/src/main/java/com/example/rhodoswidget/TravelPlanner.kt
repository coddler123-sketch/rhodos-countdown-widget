package com.example.rhodoswidget

enum class DayPlanKind {
    LINDOS_EARLY,
    SHADE,
    INLAND,
    OLD_TOWN,
    BEACH,
    EVENING
}

fun recommendDayPlan(
    weather: RhodosWeather?,
    marineWeather: MarineWeather?,
    hourOfDay: Int
): DayPlanKind = when {
    (weather?.precipitationMm ?: 0.0) >= 0.5 -> DayPlanKind.OLD_TOWN
    isMarineCaution(weather, marineWeather) -> DayPlanKind.INLAND
    (weather?.uvIndex ?: 0.0) >= 7.0 && hourOfDay in 11..16 -> DayPlanKind.SHADE
    hourOfDay <= 10 -> DayPlanKind.LINDOS_EARLY
    hourOfDay >= 17 -> DayPlanKind.EVENING
    else -> DayPlanKind.BEACH
}

fun isMarineCaution(weather: RhodosWeather?, marineWeather: MarineWeather?): Boolean =
    (weather?.windSpeedKmh ?: 0) >= 28 ||
        (marineWeather?.waveHeightMeters ?: 0.0) >= 1.2
