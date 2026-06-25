package com.example.rhodoswidget

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
}
