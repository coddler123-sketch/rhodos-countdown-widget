package com.example.rhodoswidget

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

enum class NewsCategory(val label: String) {
    ALL("Alle"), RHODOS("Rhodos"), DODECANESE("Dodekanes"),
    TRAVEL("Reise & Verkehr"), WEATHER("Wetter/Unwetter"), EVENTS("Events");

    companion object {
        fun fromApi(value: String): NewsCategory = entries.firstOrNull {
            it.name.equals(value, ignoreCase = true)
        } ?: RHODOS
    }
}

data class NewsArticle(
    val id: String,
    val originalTitle: String,
    val germanTitle: String,
    val germanSummary: String,
    val originalUrl: String,
    val publishedAt: String,
    val source: String,
    val category: NewsCategory,
    val imageUrl: String?
)

data class NewsDetail(
    val id: String,
    val germanTitle: String,
    val germanDetail: String,
    val keyPoints: List<String>,
    val source: String,
    val publishedAt: String,
    val originalUrl: String
)

sealed interface NewsDetailUiState {
    data class Preview(
        val article: NewsArticle,
        val isLoading: Boolean = true,
        val warning: String? = null
    ) : NewsDetailUiState

    data class Content(
        val detail: NewsDetail,
        val isCached: Boolean,
        val warning: String? = null
    ) : NewsDetailUiState

    data class Error(val message: String) : NewsDetailUiState
}

sealed interface NewsUiState {
    data object Loading : NewsUiState

    data class Content(
        val articles: List<NewsArticle>,
        val updatedAt: String?,
        val isCached: Boolean,
        val isRefreshing: Boolean = false,
        val warning: String? = null
    ) : NewsUiState

    data class Empty(val message: String) : NewsUiState
    data class Error(val message: String) : NewsUiState
}

data class ParsedNews(
    val articles: List<NewsArticle>,
    val generatedAt: String?,
    val raw: String,
    val fromCache: Boolean
)

data class ParsedNewsDetail(val value: NewsDetail, val raw: String)

internal object NewsParser {
    fun parse(raw: String, isCached: Boolean = false): ParsedNews {
        val root = JSONObject(raw)
        val items = root.optJSONArray("items") ?: throw IllegalArgumentException("items fehlt")
        val articles = buildList {
            for (index in 0 until items.length()) {
                val item = items.getJSONObject(index)
                val url = item.getString("originalUrl")
                require(url.startsWith("https://")) { "Nur HTTPS-Links sind erlaubt" }
                add(
                    NewsArticle(
                        id = item.getString("id"),
                        originalTitle = item.getString("originalTitle"),
                        germanTitle = item.getString("germanTitle"),
                        germanSummary = item.getString("germanSummary"),
                        originalUrl = url,
                        publishedAt = item.getString("publishedAt").also(::requireIsoDate),
                        source = item.getString("source"),
                        category = NewsCategory.fromApi(item.optString("category")),
                        imageUrl = item.optString("imageUrl").takeIf { it.startsWith("https://") }
                    )
                )
            }
        }
        return ParsedNews(
            articles = articles.sortedByDescending { it.publishedAt }.take(50),
            generatedAt = root.optString("generatedAt").takeIf(String::isNotBlank),
            raw = raw,
            fromCache = isCached
        )
    }

    private fun requireIsoDate(value: String) {
        val valid = listOf("yyyy-MM-dd'T'HH:mm:ss.SSSX", "yyyy-MM-dd'T'HH:mm:ssX").any { pattern ->
            runCatching {
                SimpleDateFormat(pattern, Locale.ROOT).apply {
                    isLenient = false
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(value)
            }.getOrNull() != null
        }
        require(valid) { "Ungültiges Veröffentlichungsdatum" }
    }
}

internal object NewsDetailParser {
    fun parse(raw: String): ParsedNewsDetail {
        val root = JSONObject(raw)
        val originalUrl = root.getString("originalUrl")
        require(originalUrl.startsWith("https://")) { "Nur HTTPS-Links sind erlaubt" }
        val pointsJson = root.getJSONArray("keyPoints")
        val points = buildList {
            for (index in 0 until pointsJson.length()) {
                pointsJson.optString(index).takeIf(String::isNotBlank)?.let(::add)
            }
        }
        require(points.size >= 3) { "Zu wenige Stichpunkte" }
        return ParsedNewsDetail(
            value = NewsDetail(
                id = root.getString("id"),
                germanTitle = root.getString("germanTitle"),
                germanDetail = root.getString("germanDetail"),
                keyPoints = points.take(5),
                source = root.getString("source"),
                publishedAt = root.getString("publishedAt"),
                originalUrl = originalUrl
            ),
            raw = raw
        )
    }
}
