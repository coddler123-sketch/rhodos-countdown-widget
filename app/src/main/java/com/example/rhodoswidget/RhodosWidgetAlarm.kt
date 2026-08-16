package com.example.rhodoswidget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Doze-toleranter Ausloeser fuer den Widget-Refresh.
 *
 * WorkManagers periodische Arbeit ist nur ein "irgendwann"-Versprechen: sobald das Geraet im Doze
 * ist (oder MIUI die App einschlaefert), koennen Stunden ohne Lauf vergehen — dann steht der
 * Countdown sichtbar falsch. setAndAllowWhileIdle feuert auch in den Doze-Wartungsfenstern und ist
 * damit das Naechste an einer Garantie, das eine normale App bekommt. Der Alarm bewaffnet sich bei
 * jedem Feuern selbst neu (setAndAllowWhileIdle kennt nur One-Shots), und der 15-Minuten-Worker
 * bewaffnet ihn ebenfalls neu — so kommt die Kette auch nach einem Neustart wieder in Gang.
 */
object RhodosWidgetAlarm {

    private const val INTERVAL_MS = 15 * 60 * 1000L

    fun schedule(context: Context) {
        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        manager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + INTERVAL_MS,
            pendingIntent(context),
        )
    }

    fun cancel(context: Context) {
        context.getSystemService(AlarmManager::class.java)?.cancel(pendingIntent(context))
    }

    private fun pendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, RhodosWidgetAlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}

class RhodosWidgetAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        RhodosCountdownLargeWidgetProvider.updateAllLargeWidgets(context)
        RhodosWidgetWorker.runOnce(context)
        RhodosWidgetAlarm.schedule(context)
    }
}
