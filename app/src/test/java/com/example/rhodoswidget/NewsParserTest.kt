package com.example.rhodoswidget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NewsParserTest {
    @Test
    fun parse_mapsAndSortsArticles() {
        val result = NewsController.parse(
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
        NewsController.parse(
            """{"items":[{"id":"1","originalTitle":"x","germanTitle":"x","germanSummary":"x","originalUrl":"http://example.com","publishedAt":"2026-07-05T08:00:00Z","source":"x","category":"RHODOS"}]}"""
        )
    }
}
