package com.example.rhodoswidget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TravelContentTest {
    @Test
    fun `all travel links use trusted https sources`() {
        val urls = travelSources.map { it.url } +
            ferryAndEventSources.map { it.url } +
            excursionIdeas.map { it.url }

        assertTrue(urls.all(::isTrustedTravelUrl))
    }

    @Test
    fun `bus section includes both Rhodes operators`() {
        assertEquals(
            setOf("www.ktelrodou.gr", "www.rhodes.gr"),
            travelSources.map { java.net.URI(it.url).host }.toSet()
        )
    }

    @Test
    fun `untrusted and insecure links are rejected`() {
        assertFalse(isTrustedTravelUrl("http://www.ktelrodou.gr/schedule/"))
        assertFalse(isTrustedTravelUrl("https://example.com/"))
        assertFalse(isTrustedTravelUrl("not-a-url"))
    }

    @Test
    fun `selection toggles without mutating original set`() {
        val original = setOf("lindos")

        assertEquals(emptySet<String>(), toggledSelection(original, "lindos"))
        assertEquals(setOf("lindos", "tsambika"), toggledSelection(original, "tsambika"))
        assertEquals(setOf("lindos"), original)
    }
}
