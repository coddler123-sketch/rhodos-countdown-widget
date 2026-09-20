package com.example.rhodoswidget

import com.example.rhodoswidget.ui.travel.photoSpots
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhotoSpotsTest {

    @Test
    fun photoSpotsContainsTenSpots() {
        assertEquals(10, photoSpots.size)
    }

    @Test
    fun photoSpotsHaveValidData() {
        val uniqueIds = mutableSetOf<String>()
        photoSpots.forEach { spot ->
            assertTrue("Duplicate photo spot ID: ${spot.id}", uniqueIds.add(spot.id))
            assertTrue("Photo spot title empty", spot.title.isNotBlank())
            assertTrue("Photo spot location empty", spot.location.isNotBlank())
            assertTrue("Photo spot best time empty", spot.bestTime.isNotBlank())
            assertTrue("Photo spot time category empty", spot.timeCategory.isNotBlank())
            assertTrue("Photo spot tip empty", spot.tip.isNotBlank())
            assertTrue("Photo spot map query empty", spot.mapQuery.isNotBlank())
            assertTrue("Photo spot emoji empty", spot.iconEmoji.isNotBlank())
        }
    }
}
