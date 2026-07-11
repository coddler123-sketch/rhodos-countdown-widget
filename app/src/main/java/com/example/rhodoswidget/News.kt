package com.example.rhodoswidget

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
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

class NewsController(private val context: Context) {
    var state: NewsUiState by mutableStateOf(loadCached() ?: NewsUiState.Loading)
        private set

    suspend fun refresh() {
        val previous = state as? NewsUiState.Content
        state = previous?.copy(isRefreshing = true, warning = null) ?: NewsUiState.Loading
        if (BuildConfig.NEWS_API_URL.isBlank()) {
            state = previous?.copy(
                isRefreshing = false,
                warning = "News-Backend ist noch nicht konfiguriert."
            ) ?: NewsUiState.Empty("News-Backend ist noch nicht konfiguriert.")
            return
        }

        runCatching { fetch(BuildConfig.NEWS_API_URL) }
            .onSuccess { payload ->
                saveCache(payload.raw)
                state = if (payload.articles.isEmpty()) {
                    NewsUiState.Empty("Zurzeit gibt es keine aktuellen Meldungen.")
                } else {
                    NewsUiState.Content(payload.articles, payload.generatedAt, isCached = false)
                }
            }
            .onFailure {
                Log.w("RhodosNews", "News refresh failed", it)
                state = previous?.copy(
                    isRefreshing = false,
                    warning = "Aktualisierung nicht möglich – zuletzt geladene Meldungen werden angezeigt."
                ) ?: NewsUiState.Error("Keine Verbindung. Bitte später erneut versuchen.")
            }
    }

    private fun loadCached(): NewsUiState.Content? {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(CACHE_KEY, null) ?: return null
        return runCatching { parse(raw, isCached = true) }.getOrNull()?.let {
            NewsUiState.Content(it.articles, it.generatedAt, isCached = true)
        }
    }

    private suspend fun fetch(endpoint: String): ParsedNews = withContext(Dispatchers.IO) {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8_000
            readTimeout = 8_000
            setRequestProperty("Accept", "application/json")
        }
        try {
            check(connection.responseCode in 200..299) { "HTTP ${connection.responseCode}" }
            parse(connection.inputStream.bufferedReader().use { it.readText() }, isCached = false)
        } finally {
            connection.disconnect()
        }
    }

    private fun saveCache(raw: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(CACHE_KEY, raw).apply()
    }

    companion object {
        private const val PREFS = "rhodos_news"
        private const val CACHE_KEY = "latest_payload"

        internal fun parse(raw: String, isCached: Boolean = false): ParsedNews {
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
}

data class ParsedNews(
    val articles: List<NewsArticle>,
    val generatedAt: String?,
    val raw: String,
    val fromCache: Boolean
)

class NewsDetailController(
    private val context: Context,
    private val article: NewsArticle
) {
    var state: NewsDetailUiState by mutableStateOf(loadCached() ?: NewsDetailUiState.Preview(article))
        private set

    suspend fun load() {
        val previous = state as? NewsDetailUiState.Content
        if (previous == null) state = NewsDetailUiState.Preview(article)
        runCatching { fetchDetail() }
            .onSuccess { detail ->
                saveCache(detail.raw)
                state = NewsDetailUiState.Content(detail.value, isCached = false)
            }
            .onFailure {
                Log.w("RhodosNews", "News detail failed", it)
                state = previous?.copy(
                    warning = "Aktualisierung nicht möglich – gespeicherte Zusammenfassung wird angezeigt."
                ) ?: NewsDetailUiState.Preview(
                    article = article,
                    isLoading = false,
                    warning = "Die ausführliche Zusammenfassung ist gerade nicht verfügbar."
                )
            }
    }

    private suspend fun fetchDetail(): ParsedNewsDetail = withContext(Dispatchers.IO) {
        check(BuildConfig.NEWS_API_URL.isNotBlank()) { "News-Backend ist nicht konfiguriert" }
        val endpoint = "${BuildConfig.NEWS_API_URL.trimEnd('/')}/${article.id}"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8_000
            readTimeout = 45_000
            setRequestProperty("Accept", "application/json")
        }
        try {
            check(connection.responseCode in 200..299) { "HTTP ${connection.responseCode}" }
            parse(connection.inputStream.bufferedReader().use { it.readText() })
        } finally {
            connection.disconnect()
        }
    }

    private fun loadCached(): NewsDetailUiState.Content? {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(cacheKey(article.id), null) ?: return null
        return runCatching { parse(raw) }.getOrNull()?.let {
            NewsDetailUiState.Content(it.value, isCached = true)
        }
    }

    private fun saveCache(raw: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(cacheKey(article.id), raw).apply()
    }

    companion object {
        private const val PREFS = "rhodos_news_details"

        private fun cacheKey(id: String) = "detail_$id"

        internal fun parse(raw: String): ParsedNewsDetail {
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
            val detail = NewsDetail(
                id = root.getString("id"),
                germanTitle = root.getString("germanTitle"),
                germanDetail = root.getString("germanDetail"),
                keyPoints = points.take(5),
                source = root.getString("source"),
                publishedAt = root.getString("publishedAt"),
                originalUrl = originalUrl
            )
            return ParsedNewsDetail(detail, raw)
        }
    }
}

data class ParsedNewsDetail(val value: NewsDetail, val raw: String)
