package com.example.rhodoswidget

import com.example.rhodoswidget.R
import com.example.rhodoswidget.MainActivity
import com.example.rhodoswidget.ui.home.*
import com.example.rhodoswidget.ui.compass.*
import com.example.rhodoswidget.ui.travel.*
import com.example.rhodoswidget.ui.news.*
import com.example.rhodoswidget.ui.weather.*
import com.example.rhodoswidget.ui.settings.*
import com.example.rhodoswidget.ui.theme.*
import com.example.rhodoswidget.widget.*

import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetProgressTest {

    @Test fun `widget progress maps fraction to progress bar range`() {
        assertEquals(0, RhodosCountdownLargeWidgetProvider.widgetProgressValue(0f))
        assertEquals(500, RhodosCountdownLargeWidgetProvider.widgetProgressValue(0.5f))
        assertEquals(1000, RhodosCountdownLargeWidgetProvider.widgetProgressValue(1f))
    }

    @Test fun `widget progress clamps out of range fractions`() {
        assertEquals(0, RhodosCountdownLargeWidgetProvider.widgetProgressValue(-0.25f))
        assertEquals(1000, RhodosCountdownLargeWidgetProvider.widgetProgressValue(1.25f))
    }

    @Test fun `decorative phrase is hidden for large system text`() {
        assertEquals(true, RhodosCountdownLargeWidgetProvider.shouldShowWidgetPhrase(1.0f))
        assertEquals(false, RhodosCountdownLargeWidgetProvider.shouldShowWidgetPhrase(1.3f))
        assertEquals(false, RhodosCountdownLargeWidgetProvider.shouldShowWidgetPhrase(1.5f))
    }
}
