package com.example.rhodoswidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * 4x2-Widget: Countdown auf das fixe Abflugdatum, Hintergrundbild und Spruch
 * wechseln taeglich, dazu das aktuelle Rhodos-Wetter. Tap auf "RHODOS" loest
 * eine sofortige Aktualisierung aus; Tap auf den Rest oeffnet die App.
 */
class RhodosCountdownLargeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id -> updateLargeWidget(context, appWidgetManager, id) }
        // 15-Minuten-Refresh sicherstellen und einmal sofort frisch holen.
        RhodosWidgetWorker.schedulePeriodic(context)
        RhodosWidgetWorker.refreshNow(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            RhodosWidgetWorker.refreshNow(context)
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.example.rhodoswidget.ACTION_REFRESH"

        // Fixes Abflugdatum: 20.09.2026, 14:30 Uhr (lokale Zeit).
        private const val DEPARTURE_YEAR = 2026
        private const val DEPARTURE_MONTH = Calendar.SEPTEMBER
        private const val DEPARTURE_DAY = 20
        private const val DEPARTURE_HOUR = 14
        private const val DEPARTURE_MINUTE = 30

        private const val ARRIVAL_BACKGROUND = R.drawable.arrival_day_rhodos

        fun updateLargeWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val remaining = calculateRemainingTime()
            val views = RemoteViews(context.packageName, R.layout.rhodos_countdown_widget_large)

            views.setTextViewText(R.id.widget_phrase_value, phraseOfTheDay(context))

            if (remaining.isReached) {
                views.setViewVisibility(R.id.widget_countdown_row, View.GONE)
                views.setViewVisibility(R.id.widget_arrival_message, View.VISIBLE)
                views.setImageViewResource(R.id.widget_background_image, ARRIVAL_BACKGROUND)
            } else {
                views.setViewVisibility(R.id.widget_countdown_row, View.VISIBLE)
                views.setViewVisibility(R.id.widget_arrival_message, View.GONE)
                views.setTextViewText(R.id.widget_days, remaining.days.toString())
                views.setTextViewText(R.id.widget_hours, remaining.hours.toString().padStart(2, '0'))
                views.setTextViewText(R.id.widget_minutes, remaining.minutes.toString().padStart(2, '0'))
                val image = Images.resourceOfTheDay(context)
                if (image != 0) {
                    views.setImageViewResource(R.id.widget_background_image, image)
                }
            }
            applyWeather(context, views)

            views.setOnClickPendingIntent(R.id.widget_root, openAppIntent(context))
            views.setOnClickPendingIntent(R.id.widget_large_title, refreshIntent(context))
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        /** Aktualisiert alle platzierten 4x2-Widgets (z. B. nach einem Wetter-Abruf). */
        fun updateAllLargeWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, RhodosCountdownLargeWidgetProvider::class.java)
            )
            ids.forEach { id -> updateLargeWidget(context, manager, id) }
        }

        private fun applyWeather(context: Context, views: RemoteViews) {
            val weather = WeatherRepository.cached(context) ?: return
            views.setTextViewText(R.id.widget_weather_temp, weather.temperatureLabel)
            views.setImageViewResource(
                R.id.widget_weather_icon,
                WeatherRepository.iconFor(weather)
            )
            // Veraltetes Wetter (Netzabruf > 6h her) leicht verblassen lassen.
            val ageMillis = System.currentTimeMillis() - WeatherRepository.lastFetchMillis(context)
            val alpha = if (ageMillis > STALE_AFTER_MILLIS) STALE_ALPHA else FRESH_ALPHA
            views.setFloat(R.id.widget_weather_icon, "setAlpha", alpha)
            views.setFloat(R.id.widget_weather_temp, "setAlpha", alpha)
        }

        private const val STALE_AFTER_MILLIS = 6L * 60 * 60 * 1000
        private const val FRESH_ALPHA = 1.0f
        private const val STALE_ALPHA = 0.5f

        private fun phraseOfTheDay(context: Context): String {
            val quotes = context.resources.getStringArray(R.array.widget_phrases)
            // Index 0 = groesste Distanz, letzter Index = Reisetag.
            val index = (quotes.size - 1 - daysUntilDeparture())
                .coerceIn(0, quotes.size - 1)
            return quotes[index]
        }

        private fun daysUntilDeparture(): Int {
            val today = midnight(Calendar.getInstance())
            val departure = midnight(Calendar.getInstance().apply {
                set(Calendar.YEAR, DEPARTURE_YEAR)
                set(Calendar.MONTH, DEPARTURE_MONTH)
                set(Calendar.DAY_OF_MONTH, DEPARTURE_DAY)
            })
            // Auf ganze Tage runden, damit Sommer-/Winterzeit nicht stoert.
            val diffDays = (departure.timeInMillis - today.timeInMillis) / 86_400_000.0
            return Math.round(diffDays).toInt()
        }

        private fun midnight(cal: Calendar): Calendar = cal.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        private fun openAppIntent(context: Context): PendingIntent {
            val intent = Intent(context, MainActivity::class.java)
            return PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun refreshIntent(context: Context): PendingIntent {
            val intent = Intent(context, RhodosCountdownLargeWidgetProvider::class.java)
                .setAction(ACTION_REFRESH)
            return PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun calculateRemainingTime(): RemainingTime {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.YEAR, DEPARTURE_YEAR)
                set(Calendar.MONTH, DEPARTURE_MONTH)
                set(Calendar.DAY_OF_MONTH, DEPARTURE_DAY)
                set(Calendar.HOUR_OF_DAY, DEPARTURE_HOUR)
                set(Calendar.MINUTE, DEPARTURE_MINUTE)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            // Abflugtag-Nachricht ab Mitternacht des Zieltages anzeigen.
            val arrivalDayStart = midnight(target.clone() as Calendar)

            val remainingMillis = (target.timeInMillis - now.timeInMillis).coerceAtLeast(0L)
            val days = TimeUnit.MILLISECONDS.toDays(remainingMillis)
            val afterDays = remainingMillis - TimeUnit.DAYS.toMillis(days)
            val hours = TimeUnit.MILLISECONDS.toHours(afterDays)
            val afterHours = afterDays - TimeUnit.HOURS.toMillis(hours)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(afterHours)

            return RemainingTime(
                days, hours, minutes,
                isReached = now.timeInMillis >= arrivalDayStart.timeInMillis
            )
        }
    }
}

data class RemainingTime(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val isReached: Boolean = false
)
