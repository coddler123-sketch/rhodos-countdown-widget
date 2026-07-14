package com.example.rhodoswidget

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun refresh_replacesCachedNewsWithFreshContent() = runTest {
        val repository = FakeNewsRepository(
            cachedNews = parsedNews("cached", fromCache = true),
            freshNews = parsedNews("fresh", fromCache = false)
        )

        val state = NewsViewModel(repository).uiState.value as NewsUiState.Content

        assertEquals("fresh", state.articles.single().id)
        assertFalse(state.isCached)
    }

    @Test
    fun refresh_keepsCachedNewsAndAddsWarningOnFailure() = runTest {
        val repository = FakeNewsRepository(
            cachedNews = parsedNews("cached", fromCache = true),
            newsFailure = IllegalStateException("offline")
        )

        val state = NewsViewModel(repository).uiState.value as NewsUiState.Content

        assertEquals("cached", state.articles.single().id)
        assertFalse(state.isRefreshing)
        assertTrue(state.warning?.contains("zuletzt geladene") == true)
    }

    @Test
    fun refresh_showsErrorWithoutCache() = runTest {
        val repository = FakeNewsRepository(newsFailure = IllegalStateException("offline"))

        val state = NewsViewModel(repository).uiState.value

        assertTrue(state is NewsUiState.Error)
    }

    @Test
    fun refresh_showsConfigurationMessageWhenEndpointIsMissing() = runTest {
        val repository = FakeNewsRepository(isConfigured = false)

        val state = NewsViewModel(repository).uiState.value

        assertTrue(state is NewsUiState.Empty)
    }

    @Test
    fun detailRefresh_keepsCachedDetailAndAddsWarningOnFailure() = runTest {
        val article = article("detail")
        val repository = FakeNewsRepository(
            cachedDetail = parsedDetail("detail"),
            detailFailure = IllegalStateException("offline")
        )

        val viewModel = NewsDetailViewModel(article, repository)
        viewModel.refresh()
        val state = viewModel.uiState.value as NewsDetailUiState.Content

        assertEquals("detail", state.detail.id)
        assertTrue(state.isCached)
        assertTrue(state.warning?.contains("gespeicherte") == true)
    }

    @Test
    fun detailRefresh_fallsBackToPreviewWithoutCache() = runTest {
        val article = article("detail")
        val repository = FakeNewsRepository(detailFailure = IllegalStateException("offline"))

        val viewModel = NewsDetailViewModel(article, repository)
        viewModel.refresh()
        val state = viewModel.uiState.value as NewsDetailUiState.Preview

        assertFalse(state.isLoading)
        assertTrue(state.warning?.contains("nicht verfügbar") == true)
    }

    private class FakeNewsRepository(
        override val isConfigured: Boolean = true,
        private val cachedNews: ParsedNews? = null,
        private val freshNews: ParsedNews = parsedNews("fresh", fromCache = false),
        private val newsFailure: Throwable? = null,
        private val cachedDetail: ParsedNewsDetail? = null,
        private val freshDetail: ParsedNewsDetail = parsedDetail("detail"),
        private val detailFailure: Throwable? = null
    ) : NewsRepository {
        override fun cachedNews(): ParsedNews? = cachedNews

        override fun cachedDetail(articleId: String): ParsedNewsDetail? = cachedDetail

        override suspend fun refreshNews(): ParsedNews {
            newsFailure?.let { throw it }
            return freshNews
        }

        override suspend fun refreshDetail(articleId: String): ParsedNewsDetail {
            detailFailure?.let { throw it }
            return freshDetail
        }
    }

    private companion object {
        fun article(id: String) = NewsArticle(
            id = id,
            originalTitle = "Original",
            germanTitle = "Titel",
            germanSummary = "Zusammenfassung",
            originalUrl = "https://example.com/$id",
            publishedAt = "2026-07-05T08:00:00Z",
            source = "Quelle",
            category = NewsCategory.RHODOS,
            imageUrl = null
        )

        fun parsedNews(id: String, fromCache: Boolean) = ParsedNews(
            articles = listOf(article(id)),
            generatedAt = "2026-07-05T08:00:00Z",
            raw = "{}",
            fromCache = fromCache
        )

        fun parsedDetail(id: String) = ParsedNewsDetail(
            value = NewsDetail(
                id = id,
                germanTitle = "Titel",
                germanDetail = "Ausführliche Zusammenfassung",
                keyPoints = listOf("Eins", "Zwei", "Drei"),
                source = "Quelle",
                publishedAt = "2026-07-05T08:00:00Z",
                originalUrl = "https://example.com/$id"
            ),
            raw = "{}"
        )
    }
}
