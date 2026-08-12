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
import java.util.concurrent.TimeUnit

class BusTimetableCheckWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val document = LiveTravelRepository.fetchKolymbiaDocument() ?: return@withContext Result.retry()
        val cached = LiveTravelRepository.cachedTransit(applicationContext)
        LiveTravelRepository.saveTransit(
            applicationContext,
            (cached.filterNot { it.id == document.id } + document).sortedBy(TransitDocument::id)
        )
        if (KolymbiaTimetable.hasUnreviewedUpdate(document.pdfUrl)) {
            notifyOnce(applicationContext, document.pdfUrl)
        }
        Result.success()
    }

    companion object {
        private const val WORK_NAME = "kolymbia_timetable_check"
        private const val CHANNEL_ID = "bus_timetable_updates"
        private const val PREFS = "kolymbia_timetable_check"
        private const val KEY_NOTIFIED_URL = "notified_url"

        fun schedule(context: Context) {
            createChannel(context)
            val request = PeriodicWorkRequestBuilder<BusTimetableCheckWorker>(24, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        private fun notifyOnce(context: Context, pdfUrl: String) {
            val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            if (preferences.getString(KEY_NOTIFIED_URL, null) == pdfUrl || !canNotify(context)) return
            val pendingIntent = PendingIntent.getActivity(
                context,
                2212,
                Intent(context, MainActivity::class.java)
                    .putExtra(MainActivity.EXTRA_OPEN_TRAVEL, true),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val message =
                "Der offizielle Kolymbia-Fahrplan hat sich geändert. Die deutschen Zeiten werden erst nach Prüfung übernommen."
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_sun)
                .setContentTitle("Neuer Busfahrplan erkannt")
                .setContentText("Der offizielle Kolymbia-Fahrplan sollte geprüft werden.")
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            try {
                NotificationManagerCompat.from(context).notify(2212, notification)
                preferences.edit().putString(KEY_NOTIFIED_URL, pdfUrl).apply()
            } catch (_: SecurityException) {
                return
            }
        }

        private fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.getSystemService(NotificationManager::class.java).createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        "Busfahrplan-Aktualisierungen",
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        description = "Meldet neue offizielle Busfahrpläne für Kolymbia"
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
