package com.example.rhodoswidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Haelt das 4x2-Widget aktuell:
 * - rendert die Widgets bei jedem Lauf neu (alle 15 Minuten),
 * - holt das Rhodos-Wetter nur zwischen 8 und 22 Uhr und hoechstens stuendlich,
 * - bei [KEY_FORCE] (manueller Refresh) wird sofort und unabhaengig vom
 *   Zeitfenster abgerufen.
 */
class RhodosWidgetWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (shouldFetchWeather() && isNetworkAvailable(applicationContext)) {
            WeatherRepository.fetch()?.let { WeatherRepository.save(applicationContext, it) }
        }
        RhodosCountdownLargeWidgetProvider.updateAllLargeWidgets(applicationContext)
        // Der Alarm ist ein One-Shot; nach Reboot oder Force-Stop ist er weg. Der Worker
        // bewaffnet ihn deshalb bei jedem Lauf neu.
        RhodosWidgetAlarm.schedule(applicationContext)
        Result.success()
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork ?: return false) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun shouldFetchWeather(): Boolean {
        if (inputData.getBoolean(KEY_FORCE, false)) return true
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hour < WEATHER_WINDOW_START || hour > WEATHER_WINDOW_END) return false
        val sinceLastFetch =
            System.currentTimeMillis() - WeatherRepository.lastFetchMillis(applicationContext)
        return sinceLastFetch >= MIN_FETCH_INTERVAL_MILLIS
    }

    companion object {
        private const val KEY_FORCE = "force"
        private const val WEATHER_WINDOW_START = 8
        private const val WEATHER_WINDOW_END = 22

        // 58 statt 60 Minuten, damit ein 15-Minuten-Tick zuverlaessig
        // einmal pro Stunde innerhalb des Fensters greift.
        private val MIN_FETCH_INTERVAL_MILLIS = TimeUnit.MINUTES.toMillis(58)

        internal const val PERIODIC_WORK = "rhodos_widget_periodic"
        internal const val ONE_TIME_WORK = "rhodos_widget_now"
        internal const val ALARM_WORK = "rhodos_widget_alarm"

        /** Regelmaessiger 15-Minuten-Refresh (Anzeige + ggf. Wetter). */
        fun schedulePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<RhodosWidgetWorker>(
                15, TimeUnit.MINUTES
            ).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        /** Repariert die Widget-Zeitplanung nach App-Update, Neustart oder App-Start. */
        fun ensureScheduled(context: Context, requestRefresh: Boolean = false) {
            val widgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(
                ComponentName(context, RhodosCountdownLargeWidgetProvider::class.java)
            )
            ensureScheduled(context, widgetIds.isNotEmpty(), requestRefresh)
        }

        internal fun ensureScheduled(
            context: Context,
            hasWidgets: Boolean,
            requestRefresh: Boolean = false
        ) {
            if (!hasWidgets) {
                RhodosWidgetAlarm.cancel(context)
                cancelAll(context)
                return
            }
            schedulePeriodic(context)
            RhodosWidgetAlarm.schedule(context)
            if (requestRefresh) refreshNow(context)
        }

        /** Einmaliger Lauf ohne Zwang: rendert neu, holt Wetter nur nach den ueblichen Regeln. */
        fun runOnce(context: Context) {
            val request = OneTimeWorkRequestBuilder<RhodosWidgetWorker>().build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(ALARM_WORK, ExistingWorkPolicy.REPLACE, request)
        }

        /** Sofortiger, erzwungener Abruf (manueller Refresh / Widget hinzugefuegt). */
        fun refreshNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<RhodosWidgetWorker>()
                .setInputData(workDataOf(KEY_FORCE to true))
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(ONE_TIME_WORK, ExistingWorkPolicy.REPLACE, request)
        }

        /** Stoppt alle Widget-Arbeit, nachdem das letzte Widget entfernt wurde. */
        fun cancelAll(context: Context) {
            WorkManager.getInstance(context).apply {
                cancelUniqueWork(PERIODIC_WORK)
                cancelUniqueWork(ONE_TIME_WORK)
                cancelUniqueWork(ALARM_WORK)
            }
        }
    }
}
