package com.example.rhodoswidget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NewsParserTest {
    @Test
    fun parse_mapsAndSortsArticles() {
        val result = NewsParser.parse(
            """{"generatedAt":"2026-07-05T08:00:00Z","items":[
              {"id":"older","originalTitle":"Παλαιότερο","germanTitle":"Älter","germanSummary":"Kurz.","originalUrl":"https://www.rodiaki.gr/a","publishedAt":"2026-07-04T08:00:00Z","source":"Rodiaki","category":"RHODOS"},
              {"id":"newer","originalTitle":"Νεότερο","germanTitle":"Neuer","germanSummary":"Kurz.","originalUrl":"https://www.dimokratiki.gr/b","publishedAt":"2026-07-05T08:00:00.000Z","source":"Dimokratiki","category":"TRAVEL","imageUrl":"http://unsafe.example/image.jpg"}
            ]}"""
        )

        assertEquals(listOf("newer", "older"), result.articles.map { it.id })
        assertEquals(NewsCategory.TRAVEL, result.articles.first().category)
        assertNull(result.articles.first().imageUrl)
    }

    @Test(expected = IllegalArgumentException::class)
    fun parse_rejectsNonHttpsOriginalLinks() {
        NewsParser.parse(
            """{"items":[{"id":"1","originalTitle":"x","germanTitle":"x","germanSummary":"x","originalUrl":"http://example.com","publishedAt":"2026-07-05T08:00:00Z","source":"x","category":"RHODOS"}]}"""
        )
    }

    @Test
    fun parseDetail_mapsGermanReaderPayload() {
        val result = NewsDetailParser.parse(
            """{"id":"rodiaki-1","germanTitle":"Titel","germanDetail":"Ausführliche Zusammenfassung.","keyPoints":["Punkt 1","Punkt 2","Punkt 3"],"source":"Rodiaki","publishedAt":"2026-07-05T08:00:00Z","originalUrl":"https://www.rodiaki.gr/article/1"}"""
        )

        assertEquals("Titel", result.value.germanTitle)
        assertEquals(3, result.value.keyPoints.size)
    }

    @Test
    fun relativeNewsAge_describesRecentArticles() {
        assertEquals(
            "vor 2 Std.",
            relativeNewsAge("2026-07-11T08:00:00Z", nowMillis = 1_783_766_400_000)
        )
    }
}
