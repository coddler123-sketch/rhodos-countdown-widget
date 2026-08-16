package com.example.rhodoswidget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Stellt die Widget-Zeitplanung nach Neustart und App-Update wieder her. */
class RhodosWidgetScheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            RhodosWidgetWorker.ensureScheduled(context, requestRefresh = true)
        }
    }
}
