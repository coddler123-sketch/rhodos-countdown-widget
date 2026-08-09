package com.example.rhodoswidget

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

data class TravelAlert(val key: String, val title: String, val message: String)

internal fun travelAlerts(
    weather: RhodosWeather?,
    marine: MarineWeather?,
    events: List<RhodesEvent>,
    nowMillis: Long
): List<TravelAlert> = buildList {
    val day = SimpleDateFormat("yyyyMMdd", Locale.US).format(nowMillis)
    if ((weather?.uvIndex ?: 0.0) >= 7.0) {
        add(TravelAlert("uv_$day", "Hoher UV-Wert auf Rhodos", "Sonnenschutz und eine Pause im Schatten einplanen."))
    }
    if (isMarineCaution(weather, marine)) {
        add(TravelAlert("marine_$day", "Wind und Wellen beachten", "Vor dem Baden oder einer Bootsfahrt die Bedingungen prüfen."))
    }
    val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    events.firstOrNull { event ->
        val start = runCatching { parser.parse(event.startDateTime)?.time }.getOrNull() ?: return@firstOrNull false
        start in nowMillis..(nowMillis + TimeUnit.HOURS.toMillis(24))
    }?.let { event ->
        val name = if (TravelTranslationRepository.containsGreek(event.title)) {
            "Eine Veranstaltung der Gemeinde beginnt innerhalb der nächsten 24 Stunden."
        } else {
            "${event.title} beginnt innerhalb der nächsten 24 Stunden."
        }
        add(TravelAlert("event_${event.id}", "Veranstaltung auf Rhodos", name))
    }
}

object TravelAlertSettings {
    private const val PREFS = "rhodos_travel_alerts"
    private const val KEY_ENABLED = "enabled"

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_ENABLED, enabled).apply()
        if (enabled) TravelAlertWorker.schedule(context) else TravelAlertWorker.cancel(context)
    }

    internal fun wasSent(context: Context, key: String): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("sent_$key", false)

    internal fun markSent(context: Context, key: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("sent_$key", true).apply()
    }
}

class TravelAlertWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (!TravelAlertSettings.isEnabled(applicationContext) || !canNotify(applicationContext)) {
            return@withContext Result.success()
        }
        val weather = WeatherRepository.fetch()?.also { WeatherRepository.save(applicationContext, it) }
            ?: WeatherRepository.cached(applicationContext)
        val marine = MarineWeatherRepository.fetch()?.also { MarineWeatherRepository.save(applicationContext, it) }
            ?: MarineWeatherRepository.cached(applicationContext)
        val events = LiveTravelRepository.fetchEvents()?.also {
            LiveTravelRepository.saveEvents(applicationContext, it)
        } ?: LiveTravelRepository.cachedEvents(applicationContext)

        createChannel(applicationContext)
        travelAlerts(weather, marine, events, System.currentTimeMillis()).forEach { alert ->
            if (!TravelAlertSettings.wasSent(applicationContext, alert.key)) {
                showNotification(applicationContext, alert)
                TravelAlertSettings.markSent(applicationContext, alert.key)
            }
        }
        Result.success()
    }

    companion object {
        private const val WORK_NAME = "rhodos_travel_alerts"
        private const val CHANNEL_ID = "travel_alerts"

        fun schedule(context: Context) {
            createChannel(context)
            val request = PeriodicWorkRequestBuilder<TravelAlertWorker>(6, TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        private fun showNotification(context: Context, alert: TravelAlert) {
            if (!canNotify(context)) return
            val intent = Intent(context, MainActivity::class.java).putExtra(MainActivity.EXTRA_OPEN_TRAVEL, true)
            val pendingIntent = PendingIntent.getActivity(
                context,
                alert.key.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_sun)
                .setContentTitle(alert.title)
                .setContentText(alert.message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(alert.message))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            try {
                NotificationManagerCompat.from(context).notify(alert.key.hashCode(), notification)
            } catch (_: SecurityException) {
                return
            }
        }

        private fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(NotificationManager::class.java)
                manager.createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, "Reisehinweise", NotificationManager.IMPORTANCE_DEFAULT).apply {
                        description = "Hinweise zu Wetter, Meer und Veranstaltungen auf Rhodos"
                    }
                )
            }
        }

        private fun canNotify(context: Context): Boolean =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
    }
}
