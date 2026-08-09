package com.example.rhodoswidget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class NewsViewModel(
    private val repository: NewsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        repository.cachedNews()?.toContentState() ?: NewsUiState.Loading
    )
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val previous = _uiState.value as? NewsUiState.Content
            _uiState.value = previous?.copy(isRefreshing = true, warning = null) ?: NewsUiState.Loading
            if (!repository.isConfigured) {
                _uiState.value = previous?.copy(
                    isRefreshing = false,
                    warning = "Der Nachrichtendienst ist noch nicht eingerichtet."
                ) ?: NewsUiState.Empty("Der Nachrichtendienst ist noch nicht eingerichtet.")
                return@launch
            }

            runCatching { repository.refreshNews() }
                .onSuccess { payload ->
                    _uiState.value = if (payload.articles.isEmpty()) {
                        NewsUiState.Empty("Zurzeit gibt es keine aktuellen Meldungen.")
                    } else {
                        payload.toContentState()
                    }
                }
                .onFailure {
                    _uiState.value = previous?.copy(
                        isRefreshing = false,
                        warning = "Aktualisierung nicht möglich – zuletzt geladene Meldungen werden angezeigt."
                    ) ?: NewsUiState.Error("Keine Verbindung. Bitte später erneut versuchen.")
                }
        }
    }

    companion object {
        fun factory(repository: NewsRepository): ViewModelProvider.Factory = simpleViewModelFactory {
            NewsViewModel(repository)
        }
    }
}

internal class NewsDetailViewModel(
    private val article: NewsArticle,
    private val repository: NewsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        repository.cachedDetail(article.id)?.let {
            NewsDetailUiState.Content(it.value, isCached = true)
        } ?: NewsDetailUiState.Preview(article)
    )
    val uiState: StateFlow<NewsDetailUiState> = _uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            val previous = _uiState.value as? NewsDetailUiState.Content
            if (previous == null) _uiState.value = NewsDetailUiState.Preview(article)
            runCatching { repository.refreshDetail(article.id) }
                .onSuccess { detail ->
                    _uiState.value = NewsDetailUiState.Content(detail.value, isCached = false)
                }
                .onFailure {
                    _uiState.value = previous?.copy(
                        warning = "Aktualisierung nicht möglich – gespeicherte Zusammenfassung wird angezeigt."
                    ) ?: NewsDetailUiState.Preview(
                        article = article,
                        isLoading = false,
                        warning = "Die ausführliche Zusammenfassung ist gerade nicht verfügbar."
                    )
                }
        }
    }

    companion object {
        fun factory(
            article: NewsArticle,
            repository: NewsRepository
        ): ViewModelProvider.Factory = simpleViewModelFactory {
            NewsDetailViewModel(article, repository)
        }
    }
}

private fun ParsedNews.toContentState() = NewsUiState.Content(
    articles = articles,
    updatedAt = generatedAt,
    isCached = fromCache
)

private fun <T : ViewModel> simpleViewModelFactory(create: () -> T): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <R : ViewModel> create(modelClass: Class<R>): R = create() as R
    }
