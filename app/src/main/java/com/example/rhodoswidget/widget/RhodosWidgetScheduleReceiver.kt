package com.example.rhodoswidget.widget
import com.example.rhodoswidget.*

import com.example.rhodoswidget.R
import com.example.rhodoswidget.MainActivity
import com.example.rhodoswidget.ui.home.*
import com.example.rhodoswidget.ui.compass.*
import com.example.rhodoswidget.ui.travel.*
import com.example.rhodoswidget.ui.news.*
import com.example.rhodoswidget.ui.weather.*
import com.example.rhodoswidget.ui.settings.*
import com.example.rhodoswidget.ui.theme.*

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
