package com.example.rhodoswidget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompassTipsTest {
    @Test
    fun `tips use the reduced navigation categories`() {
        assertEquals(
            setOf("Essen", "Strände", "Ausflüge", "Mobilität", "Unterkünfte"),
            compassTips.map(CompassTip::category).toSet()
        )
        assertFalse(compassTips.any { it.category == "Stadt" })
    }

    @Test
    fun `every tip has a concise description and a useful status`() {
        assertEquals(13, compassTips.size)
        assertTrue(compassTips.all { it.description.length <= 100 })
        assertTrue(compassTips.all { it.note.isNotBlank() })
        assertTrue(compassTips.any { it.kind == CompassTipKind.RECOMMENDATION })
        assertTrue(compassTips.any { it.kind == CompassTipKind.NOTE })
        assertTrue(compassTips.any { it.kind == CompassTipKind.CAUTION })
    }
}
