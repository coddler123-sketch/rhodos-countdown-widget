package com.example.rhodoswidget.ui.home
import com.example.rhodoswidget.*

import com.example.rhodoswidget.R
import com.example.rhodoswidget.MainActivity
import com.example.rhodoswidget.ui.compass.*
import com.example.rhodoswidget.ui.travel.*
import com.example.rhodoswidget.ui.news.*
import com.example.rhodoswidget.ui.weather.*
import com.example.rhodoswidget.ui.settings.*
import com.example.rhodoswidget.ui.theme.*
import com.example.rhodoswidget.widget.*

data class CountdownMilestone(
    val title: String,
    val message: String
)

internal fun countdownMilestone(days: Long): CountdownMilestone? = when (days) {
    30L -> CountdownMilestone("NOCH 30 TAGE", "Ein Monat bis Rhodos")
    14L -> CountdownMilestone("NOCH 14 TAGE", "In zwei Wochen geht es los")
    7L -> CountdownMilestone("NOCH 7 TAGE", "Nur noch eine Woche")
    1L -> CountdownMilestone("MORGEN", "Morgen geht es nach Rhodos")
    else -> null
}
