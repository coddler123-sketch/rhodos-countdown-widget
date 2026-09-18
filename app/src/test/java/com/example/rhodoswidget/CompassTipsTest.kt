package com.example.rhodoswidget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompassTipsTest {
    @Test
    fun `tip collection balances community and researched guidance`() {
        assertEquals(52, compassTips.size)
        assertEquals(compassTips.size, compassTips.map { it.id }.distinct().size)
        assertTrue(compassTips.all { it.id.isNotBlank() && it.id != it.title })
        assertTrue(compassTips.count { it.source == CompassTipSource.COMMUNITY } >= 10)
        assertTrue(compassTips.count { it.source == CompassTipSource.RESEARCHED } >= 10)
        assertTrue(compassTips.count { it.category == "Ausflüge" } >= 5)
        assertTrue(compassTips.any { it.category == "Kolymbia" })
    }

    @Test
    fun `search matches title location journey and tags`() {
        assertTrue(filterCompassTips(compassTips, "Melekouni").any { it.title == "Melekouni probieren" })
        assertTrue(filterCompassTips(compassTips, "Kolymbia").size >= 4)
        assertTrue(filterCompassTips(compassTips, "ohne Auto").size >= 2)
        assertEquals(
            listOf("Linienbusse & Fahrpläne ab Kolymbia"),
            filterCompassTips(compassTips, "Fahrplan", "Mobilität").map { it.title }
        )
    }

    @Test
    fun `researched tips link to secure sources`() {
        val researchedTips = compassTips.filter { it.source == CompassTipSource.RESEARCHED }

        assertTrue(researchedTips.all { it.sourceUrl?.startsWith("https://") == true })
    }

    @Test
    fun `CSV covers every tip and applies the configured Google Maps links`() {
        val mappedTips = compassTips.filter { it.mapsUrl != null }

        assertEquals(compassTipIds.toSet(), generatedCompassTipIds)
        assertEquals(generatedCompassTipMapsUrls.size, mappedTips.size)
        assertTrue(mappedTips.all { tip -> generatedCompassTipMapsUrls[tip.id] == tip.mapsUrl })
    }

    @Test
    fun `Kolymbia food recommendations are sourced and mapped`() {
        val localFoodTips = compassTips.filter { it.id.startsWith("food-kolymbia-") }

        assertEquals(11, localFoodTips.size)
        assertTrue(localFoodTips.all { it.category == "Essen" })
        assertTrue(localFoodTips.all { "Kolymbia" in it.location })
        assertTrue(localFoodTips.all { it.sourceUrl?.startsWith("https://") == true })
    }

    @Test
    fun `new food and shopping tips include balanced review summaries`() {
        val reviewedTips = compassTips.filter { it.reviewSummary != null }

        assertEquals(22, reviewedTips.size)
        assertTrue(reviewedTips.all { it.reviewSummary.orEmpty().length >= 120 })
        assertTrue(reviewedTips.all { it.source == CompassTipSource.RESEARCHED })
        assertEquals(4, compassTips.count { it.category == "Supermärkte" })
        assertEquals(3, compassTips.count { it.category == "Souvenirs" })
        assertEquals(1, compassTips.count { it.category == "Mode & Accessoires" })
        assertEquals(3, compassTips.count { it.category == "Regionale Produkte" })
    }

    @Test
    fun `Relax Hotel tip contains practical sourced details and map`() {
        val hotel = compassTips.first { it.id == "hotel-relax-kolymbia" }

        assertEquals("Unterkünfte", hotel.category)
        assertEquals(8, hotel.hotelDetails.size)
        assertTrue(hotel.hotelDetails.any {
            it.title == "EUER ZIMMER · SHARED POOL" &&
                "kein Privatpool" in it.text &&
                "renovierten Teil" in it.text &&
                "Sommer 2023" in it.text &&
                "RHO071" in it.text
        })
        assertTrue(hotel.hotelDetails.any { it.title == "ADRESSE & KONTAKT" && "+30 22410 56220" in it.text })
        assertTrue(hotel.hotelDetails.any { it.title == "AN- & ABREISE" && "Voucher" in it.text })
        assertTrue(hotel.sourceUrl == "https://www.relaxhotel.gr/")
        assertTrue(hotel.mapsUrl?.startsWith("https://www.google.com/maps/") == true)
    }

    @Test
    fun `featured tip rotates predictably`() {
        assertEquals(compassTips.first(), featuredCompassTip(compassTips, 1))
        assertEquals(compassTips.first(), featuredCompassTip(compassTips, compassTips.size + 1))
    }

    @Test
    fun `each tip uses a distinct editorial image`() {
        val images = compassTips.map { editorialFor(it).imageRes }

        assertEquals(images.size, images.distinct().size)
    }

    @Test
    fun `day plan contains at most three saved tips`() {
        val saved = compassTips.take(5).mapTo(mutableSetOf()) { it.id }

        val plan = buildCompassDayPlan(compassTips, saved)

        assertEquals(3, plan.size)
        assertTrue(plan.all { it.id in saved })
    }

    @Test
    fun `quick filters use editorial travel details`() {
        val lindos = compassTips.first { it.title == "Lindos gleich morgens" }
        val food = compassTips.first { it.title == "Melekouni probieren" }

        assertTrue(matchesCompassQuickFilter(lindos, "Halber Tag"))
        assertTrue(matchesCompassQuickFilter(food, "Essen"))
    }

    @Test
    fun `legacy titles migrate to stable ids without changing unknown values`() {
        val titleToId = compassTips.associate { it.title to it.id }
        val migrated = migrateCompassSelection(
            setOf("Melekouni probieren", "future-stable-id"),
            titleToId
        )

        assertEquals(setOf("food-melekouni", "future-stable-id"), migrated)
    }
}
