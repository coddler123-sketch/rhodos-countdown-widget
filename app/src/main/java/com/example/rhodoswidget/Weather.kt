package com.example.rhodoswidget

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Wetter fuer Kolymbia auf Rhodos (fest), bezogen von Open-Meteo (kein API-Key noetig).
 * Das zuletzt erfolgreiche Ergebnis wird lokal gecacht, damit das Widget auch
 * ohne frischen Abruf sofort etwas anzeigen kann.
 */
data class RhodosWeather(
    val temperatureCelsius: Int,
    val apparentTemperatureCelsius: Int,
    val relativeHumidityPercent: Int,
    val precipitationMm: Double,
    val windSpeedKmh: Int,
    val weatherCode: Int,
    val isDay: Boolean,
    val forecast: List<RhodosForecastDay> = emptyList(),
    val sunsetIso: String? = null  // z. B. "2026-06-14T20:43"
) {
    val temperatureLabel: String
        get() = "$temperatureCelsius°"

    val apparentTemperatureLabel: String
        get() = "gefühlt $apparentTemperatureCelsius°"

    val humidityLabel: String
        get() = "$relativeHumidityPercent%"

    val precipitationLabel: String
        get() = if (precipitationMm < 0.05) "0 mm" else "${"%.1f".format(precipitationMm)} mm"

    val windSpeedLabel: String
        get() = "$windSpeedKmh km/h"
}

data class RhodosForecastDay(
    val dateIso: String,
    val minTemperatureCelsius: Int,
    val maxTemperatureCelsius: Int,
    val precipitationProbabilityPercent: Int?,
    val precipitationMm: Double,
    val weatherCode: Int
)

object WeatherReportFormatter {
    fun compactForecastLabel(day: RhodosForecastDay): String =
        "${shortWeekday(day.dateIso)} ${day.minTemperatureCelsius}/${day.maxTemperatureCelsius}°"

    fun spokenReport(weather: RhodosWeather): String {
        val rainText = if (weather.precipitationMm < 0.05) {
            "Es wird kein Regen gemeldet."
        } else {
            "Es werden ${formatMm(weather.precipitationMm)} Millimeter Regen gemeldet."
        }
        val forecastText = weather.forecast.take(3).joinToString(" ") { day ->
            "${longWeekday(day.dateIso)} ${day.minTemperatureCelsius} bis ${day.maxTemperatureCelsius} Grad, ${spokenCondition(day.weatherCode)}."
        }
        val forecastSentence = if (forecastText.isBlank()) "" else " Die Vorschau: $forecastText"
        return "Wetterbericht fuer Kolymbia auf Rhodos. Aktuell sind es ${weather.temperatureCelsius} Grad, " +
            "gefuehlt ${weather.apparentTemperatureCelsius} Grad. Die Luftfeuchtigkeit liegt bei " +
            "${weather.relativeHumidityPercent} Prozent. Der Wind weht mit ${weather.windSpeedKmh} Kilometern pro Stunde. " +
            rainText + forecastSentence
    }

    private fun shortWeekday(dateIso: String): String = when (weekdayIndex(dateIso)) {
        1 -> "Mo"
        2 -> "Di"
        3 -> "Mi"
        4 -> "Do"
        5 -> "Fr"
        6 -> "Sa"
        else -> "So"
    }

    private fun longWeekday(dateIso: String): String = when (weekdayIndex(dateIso)) {
        1 -> "Montag"
        2 -> "Dienstag"
        3 -> "Mittwoch"
        4 -> "Donnerstag"
        5 -> "Freitag"
        6 -> "Samstag"
        else -> "Sonntag"
    }

    private fun weekdayIndex(dateIso: String): Int {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateIso) ?: return 0
        val text = SimpleDateFormat("u", Locale.US).format(date)
        return text.toIntOrNull() ?: 0
    }

    private fun spokenCondition(code: Int): String = when (code) {
        0, 1 -> "meist sonnig"
        2 -> "leicht bewoelkt"
        3, 45, 48 -> "bewoelkt"
        in 51..67, in 80..82 -> "Regen moeglich"
        in 71..77, 85, 86 -> "Schnee oder Graupel moeglich"
        95, 96, 99 -> "Gewitter moeglich"
        else -> "wechselhaft"
    }

    private fun formatMm(value: Double): String =
        if (value % 1.0 == 0.0) value.roundToInt().toString() else "%.1f".format(value)
}

object WeatherRepository {

    // Kolymbia, Rhodos
    private const val LATITUDE = 36.2531
    private const val LONGITUDE = 28.1556

    private const val PREFS = "rhodos_weather"
    private const val KEY_TEMP = "temperature"
    private const val KEY_APPARENT_TEMP = "apparent_temperature"
    private const val KEY_HUMIDITY = "humidity"
    private const val KEY_PRECIPITATION = "precipitation"
    private const val KEY_WIND_SPEED = "wind_speed"
    private const val KEY_CODE = "weather_code"
    private const val KEY_IS_DAY = "is_day"
    private const val KEY_HAS_DATA = "has_data"
    private const val KEY_LAST_FETCH = "last_fetch"
    private const val KEY_FORECAST = "forecast"
    private const val KEY_SUNSET = "sunset"

    /** Live-Abruf bei Open-Meteo. Gibt null zurueck, wenn etwas schiefgeht. */
    fun fetch(): RhodosWeather? {
        val url = URL(
            "https://api.open-meteo.com/v1/forecast" +
                "?latitude=$LATITUDE&longitude=$LONGITUDE" +
                "&current=temperature_2m,apparent_temperature,relative_humidity_2m," +
                "precipitation,weather_code,wind_speed_10m,is_day" +
                "&daily=weather_code,temperature_2m_max,temperature_2m_min," +
                "precipitation_probability_max,precipitation_sum,sunset" +
                "&timezone=auto&forecast_days=8"
        )
        val connection = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 10_000
            requestMethod = "GET"
        }
        return try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(body)
            val current = root.getJSONObject("current")
            RhodosWeather(
                temperatureCelsius = current.getDouble("temperature_2m").roundToInt(),
                apparentTemperatureCelsius = current.getDouble("apparent_temperature").roundToInt(),
                relativeHumidityPercent = current.getInt("relative_humidity_2m"),
                precipitationMm = current.optDouble("precipitation", 0.0),
                windSpeedKmh = current.getDouble("wind_speed_10m").roundToInt(),
                weatherCode = current.getInt("weather_code"),
                isDay = current.optInt("is_day", 1) == 1,
                forecast = parseForecast(root.optJSONObject("daily")),
                sunsetIso = root.optJSONObject("daily")?.optJSONArray("sunset")?.optString(0)
            )
        } catch (error: Exception) {
            Log.w("RhodosWeather", "Weather fetch failed", error)
            null
        } finally {
            connection.disconnect()
        }
    }

    fun save(context: Context, weather: RhodosWeather) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putInt(KEY_TEMP, weather.temperatureCelsius)
            .putInt(KEY_APPARENT_TEMP, weather.apparentTemperatureCelsius)
            .putInt(KEY_HUMIDITY, weather.relativeHumidityPercent)
            .putFloat(KEY_PRECIPITATION, weather.precipitationMm.toFloat())
            .putInt(KEY_WIND_SPEED, weather.windSpeedKmh)
            .putInt(KEY_CODE, weather.weatherCode)
            .putBoolean(KEY_IS_DAY, weather.isDay)
            .putString(KEY_FORECAST, forecastToJson(weather.forecast).toString())
            .putString(KEY_SUNSET, weather.sunsetIso)
            .putBoolean(KEY_HAS_DATA, true)
            .putLong(KEY_LAST_FETCH, System.currentTimeMillis())
            .apply()
    }

    /** Zeitpunkt des letzten erfolgreichen Abrufs (0, wenn noch keiner). */
    fun lastFetchMillis(context: Context): Long =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_FETCH, 0L)

    /** Gecachtes Wetter oder null, wenn noch nie erfolgreich abgerufen wurde. */
    fun cached(context: Context): RhodosWeather? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_HAS_DATA, false)) return null
        return RhodosWeather(
            temperatureCelsius = prefs.getInt(KEY_TEMP, 0),
            apparentTemperatureCelsius = prefs.getInt(KEY_APPARENT_TEMP, prefs.getInt(KEY_TEMP, 0)),
            relativeHumidityPercent = prefs.getInt(KEY_HUMIDITY, 0),
            precipitationMm = prefs.getFloat(KEY_PRECIPITATION, 0f).toDouble(),
            windSpeedKmh = prefs.getInt(KEY_WIND_SPEED, 0),
            weatherCode = prefs.getInt(KEY_CODE, 0),
            isDay = prefs.getBoolean(KEY_IS_DAY, true),
            forecast = forecastFromJson(prefs.getString(KEY_FORECAST, null)),
            sunsetIso = prefs.getString(KEY_SUNSET, null)
        )
    }

    /** WMO-Wettercode auf ein passendes Widget-Symbol abbilden. */
    fun iconFor(weather: RhodosWeather): Int = when (weather.weatherCode) {
        0, 1 -> if (weather.isDay) R.drawable.ic_sun else R.drawable.ic_moon
        2, 3, 45, 48 -> R.drawable.ic_weather_cloud
        in 51..67, in 80..82 -> R.drawable.ic_weather_rain
        in 71..77, 85, 86 -> R.drawable.ic_weather_snow
        95, 96, 99 -> R.drawable.ic_weather_thunder
        else -> R.drawable.ic_weather_cloud
    }

    fun iconForCode(code: Int): Int = when (code) {
        0, 1 -> R.drawable.ic_sun
        2, 3, 45, 48 -> R.drawable.ic_weather_cloud
        in 51..67, in 80..82 -> R.drawable.ic_weather_rain
        in 71..77, 85, 86 -> R.drawable.ic_weather_snow
        95, 96, 99 -> R.drawable.ic_weather_thunder
        else -> R.drawable.ic_weather_cloud
    }

    private fun parseForecast(daily: JSONObject?): List<RhodosForecastDay> {
        if (daily == null) return emptyList()
        val dates = daily.getJSONArray("time")
        val maxTemps = daily.getJSONArray("temperature_2m_max")
        val minTemps = daily.getJSONArray("temperature_2m_min")
        val weatherCodes = daily.getJSONArray("weather_code")
        val probabilities = daily.optJSONArray("precipitation_probability_max")
        val precipitation = daily.optJSONArray("precipitation_sum")
        val days = mutableListOf<RhodosForecastDay>()
        for (index in 1 until minOf(dates.length(), 8)) {
            val probability = probabilities?.optInt(index, -1)?.takeIf { it >= 0 }
            days += RhodosForecastDay(
                dateIso = dates.getString(index),
                minTemperatureCelsius = minTemps.getDouble(index).roundToInt(),
                maxTemperatureCelsius = maxTemps.getDouble(index).roundToInt(),
                precipitationProbabilityPercent = probability,
                precipitationMm = precipitation?.optDouble(index, 0.0) ?: 0.0,
                weatherCode = weatherCodes.getInt(index)
            )
        }
        return days
    }

    private fun forecastToJson(forecast: List<RhodosForecastDay>): JSONArray {
        val array = JSONArray()
        forecast.forEach { day ->
            array.put(
                JSONObject()
                    .put("date", day.dateIso)
                    .put("min", day.minTemperatureCelsius)
                    .put("max", day.maxTemperatureCelsius)
                    .put("probability", day.precipitationProbabilityPercent)
                    .put("precipitation", day.precipitationMm)
                    .put("code", day.weatherCode)
            )
        }
        return array
    }

    private fun forecastFromJson(json: String?): List<RhodosForecastDay> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                RhodosForecastDay(
                    dateIso = item.getString("date"),
                    minTemperatureCelsius = item.getInt("min"),
                    maxTemperatureCelsius = item.getInt("max"),
                    precipitationProbabilityPercent = item.optInt("probability", -1).takeIf { it >= 0 },
                    precipitationMm = item.optDouble("precipitation", 0.0),
                    weatherCode = item.getInt("code")
                )
            }
        } catch (error: Exception) {
            Log.w("RhodosWeather", "Forecast cache parse failed", error)
            emptyList()
        }
    }
}
