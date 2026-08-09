package com.example.rhodoswidget

import android.content.Context
import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class MarineWeather(
    val seaSurfaceTemperatureCelsius: Double?,
    val waveHeightMeters: Double?,
    val wavePeriodSeconds: Double?,
    val waveDirectionDegrees: Double?,
    val fetchedAtMillis: Long
)

object MarineWeatherParser {
    fun parse(json: String, fetchedAtMillis: Long): MarineWeather {
        val current = JSONObject(json).getJSONObject("current")
        return MarineWeather(
            seaSurfaceTemperatureCelsius = current.finiteDouble("sea_surface_temperature"),
            waveHeightMeters = current.finiteDouble("wave_height"),
            wavePeriodSeconds = current.finiteDouble("wave_period"),
            waveDirectionDegrees = current.finiteDouble("wave_direction"),
            fetchedAtMillis = fetchedAtMillis
        )
    }

    private fun JSONObject.finiteDouble(key: String): Double? =
        optDouble(key, Double.NaN).takeIf { it.isFinite() }
}

object MarineWeatherRepository {
    private const val PREFS = "rhodos_marine_weather"
    private const val KEY_HAS_DATA = "has_data"
    private const val KEY_FETCHED_AT = "fetched_at"
    private const val KEY_SEA_TEMP = "sea_temperature"
    private const val KEY_WAVE_HEIGHT = "wave_height"
    private const val KEY_WAVE_PERIOD = "wave_period"
    private const val KEY_WAVE_DIRECTION = "wave_direction"
    private const val MISSING_VALUE = -999f

    private const val ENDPOINT =
        "https://marine-api.open-meteo.com/v1/marine" +
            "?latitude=36.2531&longitude=28.1556" +
            "&current=wave_height,wave_direction,wave_period,sea_surface_temperature" +
            "&timezone=Europe%2FAthens"

    fun fetch(nowMillis: Long = System.currentTimeMillis()): MarineWeather? {
        val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 10_000
            requestMethod = "GET"
        }
        return try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            MarineWeatherParser.parse(body, nowMillis)
        } catch (error: Exception) {
            Log.w("RhodosMarine", "Marine weather fetch failed", error)
            null
        } finally {
            connection.disconnect()
        }
    }

    fun save(context: Context, weather: MarineWeather) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_HAS_DATA, true)
            .putLong(KEY_FETCHED_AT, weather.fetchedAtMillis)
            .putFloat(KEY_SEA_TEMP, weather.seaSurfaceTemperatureCelsius.asStoredFloat())
            .putFloat(KEY_WAVE_HEIGHT, weather.waveHeightMeters.asStoredFloat())
            .putFloat(KEY_WAVE_PERIOD, weather.wavePeriodSeconds.asStoredFloat())
            .putFloat(KEY_WAVE_DIRECTION, weather.waveDirectionDegrees.asStoredFloat())
            .apply()
    }

    fun cached(context: Context): MarineWeather? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_HAS_DATA, false)) return null
        return MarineWeather(
            seaSurfaceTemperatureCelsius = prefs.getFloat(KEY_SEA_TEMP, MISSING_VALUE).storedDouble(),
            waveHeightMeters = prefs.getFloat(KEY_WAVE_HEIGHT, MISSING_VALUE).storedDouble(),
            wavePeriodSeconds = prefs.getFloat(KEY_WAVE_PERIOD, MISSING_VALUE).storedDouble(),
            waveDirectionDegrees = prefs.getFloat(KEY_WAVE_DIRECTION, MISSING_VALUE).storedDouble(),
            fetchedAtMillis = prefs.getLong(KEY_FETCHED_AT, 0L)
        )
    }

    private fun Double?.asStoredFloat(): Float = this?.toFloat() ?: MISSING_VALUE

    private fun Float.storedDouble(): Double? =
        takeUnless { it == MISSING_VALUE }?.toDouble()
}
