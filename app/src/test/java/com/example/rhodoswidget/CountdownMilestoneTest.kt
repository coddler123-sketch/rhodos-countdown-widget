package com.example.rhodoswidget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CountdownMilestoneTest {
    @Test
    fun `milestones exist at configured days`() {
        assertEquals("Ein Monat bis Rhodos", countdownMilestone(30)?.message)
        assertEquals("In zwei Wochen geht es los", countdownMilestone(14)?.message)
        assertEquals("Nur noch eine Woche", countdownMilestone(7)?.message)
        assertEquals("Morgen geht es nach Rhodos", countdownMilestone(1)?.message)
    }

    @Test
    fun `ordinary countdown day has no milestone`() {
        assertNull(countdownMilestone(69))
    }
}
