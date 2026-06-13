package com.example.rhodoswidget

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
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
    val isDay: Boolean
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

    /** Live-Abruf bei Open-Meteo. Gibt null zurueck, wenn etwas schiefgeht. */
    fun fetch(): RhodosWeather? {
        val url = URL(
            "https://api.open-meteo.com/v1/forecast" +
                "?latitude=$LATITUDE&longitude=$LONGITUDE" +
                "&current=temperature_2m,apparent_temperature,relative_humidity_2m," +
                "precipitation,weather_code,wind_speed_10m,is_day"
        )
        val connection = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 10_000
            requestMethod = "GET"
        }
        return try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val current = JSONObject(body).getJSONObject("current")
            RhodosWeather(
                temperatureCelsius = current.getDouble("temperature_2m").roundToInt(),
                apparentTemperatureCelsius = current.getDouble("apparent_temperature").roundToInt(),
                relativeHumidityPercent = current.getInt("relative_humidity_2m"),
                precipitationMm = current.optDouble("precipitation", 0.0),
                windSpeedKmh = current.getDouble("wind_speed_10m").roundToInt(),
                weatherCode = current.getInt("weather_code"),
                isDay = current.optInt("is_day", 1) == 1
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
            isDay = prefs.getBoolean(KEY_IS_DAY, true)
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
}
