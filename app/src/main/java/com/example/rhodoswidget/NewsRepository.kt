package com.example.rhodoswidget

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

internal interface NewsRepository {
    val isConfigured: Boolean

    fun cachedNews(): ParsedNews?
    fun cachedDetail(articleId: String): ParsedNewsDetail?
    suspend fun refreshNews(): ParsedNews
    suspend fun refreshDetail(articleId: String): ParsedNewsDetail
}

internal class DefaultNewsRepository(
    context: Context,
    private val endpoint: String
) : NewsRepository {
    private val preferences = context.getSharedPreferences(NEWS_PREFS, Context.MODE_PRIVATE)
    private val detailPreferences = context.getSharedPreferences(DETAIL_PREFS, Context.MODE_PRIVATE)

    override val isConfigured: Boolean
        get() = endpoint.isNotBlank()

    override fun cachedNews(): ParsedNews? {
        val raw = preferences.getString(NEWS_CACHE_KEY, null) ?: return null
        return runCatching { NewsParser.parse(raw, isCached = true) }.getOrNull()
    }

    override fun cachedDetail(articleId: String): ParsedNewsDetail? {
        val raw = detailPreferences.getString(detailCacheKey(articleId), null) ?: return null
        return runCatching { NewsDetailParser.parse(raw) }.getOrNull()
    }

    override suspend fun refreshNews(): ParsedNews = withContext(Dispatchers.IO) {
        check(isConfigured) { "News-Backend ist nicht konfiguriert" }
        val raw = fetch(endpoint, readTimeoutMillis = 8_000)
        NewsParser.parse(raw).also {
            preferences.edit().putString(NEWS_CACHE_KEY, raw).apply()
        }
    }

    override suspend fun refreshDetail(articleId: String): ParsedNewsDetail = withContext(Dispatchers.IO) {
        check(isConfigured) { "News-Backend ist nicht konfiguriert" }
        val detailEndpoint = "${endpoint.trimEnd('/')}/$articleId"
        val raw = fetch(detailEndpoint, readTimeoutMillis = 45_000)
        NewsDetailParser.parse(raw).also {
            detailPreferences.edit().putString(detailCacheKey(articleId), raw).apply()
        }
    }

    private fun fetch(url: String, readTimeoutMillis: Int): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8_000
            readTimeout = readTimeoutMillis
            setRequestProperty("Accept", "application/json")
        }
        return try {
            check(connection.responseCode in 200..299) { "HTTP ${connection.responseCode}" }
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun detailCacheKey(articleId: String) = "detail_$articleId"

    private companion object {
        const val NEWS_PREFS = "rhodos_news"
        const val NEWS_CACHE_KEY = "latest_payload"
        const val DETAIL_PREFS = "rhodos_news_details"
    }
}
