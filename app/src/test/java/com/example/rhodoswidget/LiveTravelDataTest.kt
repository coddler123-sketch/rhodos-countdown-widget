package com.example.rhodoswidget

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class LiveTravelDataTest {
    @Test
    fun `ktel parser extracts official uploaded pdf`() {
        val pdf = "https://www.ktelrodou.gr/wp-content/uploads/2026/08/LINDOS.pdf"
        val json = JSONObject().put(
            "content",
            JSONObject().put("rendered", "viewer?file=${URLEncoder.encode(pdf, StandardCharsets.UTF_8.name())} $pdf=")
        ).toString()

        val document = LiveTravelParser.ktelDocument(json, "lindos", "Lindos", "https://source", 42L)

        assertEquals(pdf, document?.pdfUrl)
        assertEquals(42L, document?.fetchedAtMillis)
    }

    @Test
    fun `roda parser keeps latest document per service day`() {
        val old = encoded("https://www.rhodes.gr/wp-content/uploads/2026/06/ΔΥΤΙΚΗΣ-ΔΕΥΤΕΡΑ-01.06.2026.pdf")
        val current = encoded("https://www.rhodes.gr/wp-content/uploads/2026/08/ΔΥΤΙΚΗΣ-ΔΕΥΤΕΡΑ-03.08.2026.pdf")
        val saturday = encoded("https://www.rhodes.gr/wp-content/uploads/2026/08/ΔΥΤΙΚΗΣ-ΣΑΒΒΑΤΟ-01.08.2026.pdf")
        val json = JSONObject().put(
            "content",
            JSONObject().put("rendered", "url:$old|target url:$current|target url:$saturday|target")
        ).toString()

        val documents = LiveTravelParser.rodaDocuments(json, 7L)

        assertEquals(2, documents.size)
        assertTrue(documents.first { it.id == "roda_weekday" }.pdfUrl.contains("03.08.2026"))
        assertTrue(documents.all { it.operator == "RODA" })
    }

    @Test
    fun `event parser removes expired events and decodes titles`() {
        val events = JSONArray()
            .put(event(1, "Alt", "2020-01-01 10:00:00", "2020-01-01 12:00:00"))
            .put(event(2, "Musik &#038; Meer", "2099-01-01 10:00:00", "2099-01-01 12:00:00"))
        val json = JSONObject().put("events", events).toString()

        val parsed = LiveTravelParser.events(json, 1_800_000_000_000L)

        assertEquals(listOf("Musik & Meer"), parsed.map { it.title })
        assertEquals("Mandraki", parsed.single().venue)
    }

    @Test
    fun `only official pdf hosts are accepted`() {
        assertTrue(LiveTravelParser.isTrustedPdf("https://www.ktelrodou.gr/wp-content/uploads/a.pdf"))
        assertTrue(LiveTravelParser.isTrustedPdf("https://www.rhodes.gr/wp-content/uploads/a.PDF"))
        assertFalse(LiveTravelParser.isTrustedPdf("http://www.rhodes.gr/a.pdf"))
        assertFalse(LiveTravelParser.isTrustedPdf("https://example.com/a.pdf"))
        assertFalse(LiveTravelParser.isTrustedPdf("https://www.rhodes.gr/not-a-pdf"))
    }

    @Test
    fun `event endpoint encodes date parameters safely`() {
        val endpoint = LiveTravelRepository.eventEndpoint(1_800_000_000_000L)

        assertTrue(endpoint.contains("start_date="))
        assertTrue(endpoint.contains("%20"))
        assertTrue(endpoint.contains("%3A"))
    }

    @Test
    fun `greek text detection leaves German content untouched`() {
        assertTrue(TravelTranslationRepository.containsGreek("Μουσική στη Ρόδο"))
        assertFalse(TravelTranslationRepository.containsGreek("Musik auf Rhodos"))
    }

    private fun event(id: Int, title: String, start: String, end: String) = JSONObject()
        .put("id", id)
        .put("title", title)
        .put("start_date", start)
        .put("end_date", end)
        .put("venue", JSONObject().put("venue", "Mandraki"))
        .put("url", "https://www.rhodes.gr/event/$id/")

    private fun encoded(url: String): String =
        URLEncoder.encode(url, StandardCharsets.UTF_8.name()).replace("+", "%20")
}
