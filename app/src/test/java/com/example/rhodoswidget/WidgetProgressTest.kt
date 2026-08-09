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

    @Test fun `decorative phrase is hidden for large system text`() {
        assertEquals(true, RhodosCountdownLargeWidgetProvider.shouldShowWidgetPhrase(1.0f))
        assertEquals(false, RhodosCountdownLargeWidgetProvider.shouldShowWidgetPhrase(1.3f))
        assertEquals(false, RhodosCountdownLargeWidgetProvider.shouldShowWidgetPhrase(1.5f))
    }
}
