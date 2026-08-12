package com.example.rhodoswidget

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max

private val Sun = Color(0xFFF4B942)

@Composable
fun NewsTicker(state: NewsUiState, onOpenNews: () -> Unit) {
    val articles = (state as? NewsUiState.Content)?.articles.orEmpty().take(10)
    var index by remember(articles) { mutableIntStateOf(0) }
    var isPaused by rememberSaveable { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(articles, isPaused) {
        while (articles.size > 1 && !isPaused) {
            delay(6_000)
            index = (index + 1) % articles.size
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("news-ticker")
            .border(1.dp, HomeAccent.copy(alpha = 0.5f), HomeCardShape)
            .clickable(onClick = onOpenNews),
        colors = CardDefaults.cardColors(containerColor = HomeCardColor),
        shape = HomeCardShape
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "AKTUELLES",
                    color = HomeAccent,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp
                )
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("news-ticker-pause")
                        .clickable {
                            isPaused = !isPaused
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        .clearAndSetSemantics {
                            contentDescription = if (isPaused) "News-Rotation fortsetzen" else "News-Rotation pausieren"
                            onClick { isPaused = !isPaused; true }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (isPaused) "▶" else "Ⅱ", color = HomeAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            Box(modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp), contentAlignment = Alignment.CenterStart) {
                when {
                    articles.isNotEmpty() -> AnimatedContent(
                        targetState = articles[index.coerceAtMost(articles.lastIndex)],
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "newsTicker"
                    ) { article ->
                        Column {
                            Text(
                                article.germanTitle,
                                color = Color.White,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(4.dp))
                            val age = relativeNewsAge(article.publishedAt)
                            Text(
                                listOfNotNull(article.source.takeIf { it.isNotBlank() }, age).joinToString(" · "),
                                color = Color(0x99FFFFFF),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    state is NewsUiState.Loading -> Text("Aktuelles von Rhodos wird geladen …", color = Color(0xCCFFFFFF), fontSize = 12.sp)
                    else -> Text("Aktuelles von Rhodos öffnen", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun NewsScreen(
    state: NewsUiState,
    padding: PaddingValues,
    scrollToTopRequest: Int = 0,
    onRefresh: () -> Unit,
    onOpenDetail: (NewsArticle) -> Unit
) {
    var selected by rememberSaveable { mutableStateOf(NewsCategory.ALL) }
    var showMoreFilters by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    LaunchedEffect(scrollToTopRequest) {
        if (scrollToTopRequest > 0) listState.animateScrollToItem(0)
    }
    val articles = (state as? NewsUiState.Content)?.articles.orEmpty().filter {
        selected == NewsCategory.ALL || it.category == selected
    }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF142E34), Color(0xFF0D1113)))
        )
    ) {
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Aktuelles von Rhodos",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Montserrat
                    )
                    Text("Inselinfos für unterwegs", color = Color(0xBFFFFFFF), fontSize = 12.sp)
                }
                TextButton(
                    onClick = onRefresh,
                    enabled = (state as? NewsUiState.Content)?.isRefreshing != true
                ) { Text("Aktualisieren", color = HomeAccent, fontWeight = FontWeight.Bold) }
            }
            val primaryCategories = listOf(NewsCategory.ALL, NewsCategory.RHODOS, NewsCategory.TRAVEL)
            val secondaryCategories = NewsCategory.entries.filterNot(primaryCategories::contains)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                primaryCategories.forEach { category -> NewsFilterChip(category, selected) { selected = category } }
            }
            Text(
                if (showMoreFilters) "Weniger Filter ︿" else "Weitere Filter ›",
                color = HomeAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.End)
                    .testTag("news-more-filters")
                    .clickable { showMoreFilters = !showMoreFilters }
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            )
            AnimatedVisibility(showMoreFilters || selected in secondaryCategories) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    secondaryCategories.forEach { category -> NewsFilterChip(category, selected) { selected = category } }
                }
            }
            Spacer(Modifier.height(6.dp))
            when {
                state is NewsUiState.Loading -> NewsMessage("Die neuesten Inselmeldungen werden geladen …")
                state is NewsUiState.Error -> NewsMessage(state.message, onRefresh)
                state is NewsUiState.Empty -> NewsMessage(state.message, onRefresh)
                articles.isEmpty() -> NewsMessage("Für diesen Filter gibt es gerade keine Meldungen.")
                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.testTag("news-list"),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val content = state as NewsUiState.Content
                    content.warning?.let { item { Text(it, color = Sun, fontSize = 13.sp) } }
                    if (content.isCached) item { Text("Ohne Internet verfügbar · zuletzt geladener Stand", color = HomeAccent, fontSize = 12.sp) }
                    item(key = "featured-${articles.first().id}") {
                        FeaturedNewsCard(articles.first(), onOpenDetail)
                    }
                    items(articles.drop(1), key = NewsArticle::id) { CompactNewsCard(it, onOpenDetail) }
                    item { Text("Automatisch aus dem Griechischen übersetzt", color = Color(0x80FFFFFF), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp)) }
                }
            }
        }
    }
}

@Composable
private fun NewsFilterChip(
    category: NewsCategory,
    selected: NewsCategory,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected == category,
        onClick = onClick,
        label = { Text(category.label) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.Transparent,
            labelColor = Color.White,
            selectedContainerColor = HomeAccent,
            selectedLabelColor = Color(0xFF102126)
        ),
        border = BorderStroke(1.dp, if (selected == category) HomeAccent else HomeCardBorder)
    )
}

@Composable
private fun FeaturedNewsCard(article: NewsArticle, onOpenDetail: (NewsArticle) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, HomeAccent.copy(alpha = 0.7f), HomeCardShape)
            .clickable { onOpenDetail(article) }
            .testTag("news-open-detail"),
        colors = CardDefaults.cardColors(containerColor = HomeAccent.copy(alpha = 0.16f)),
        shape = HomeCardShape
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("NEUESTE MELDUNG", color = HomeAccent, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.8.sp)
                Spacer(Modifier.weight(1f))
                Text(article.category.label.uppercase(), color = Color(0xBFFFFFFF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(7.dp))
            Text(
                article.germanTitle,
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Text(
                article.germanSummary,
                color = Color(0xD9FFFFFF),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(12.dp))
            val relativeAge = relativeNewsAge(article.publishedAt)
            Text(
                newsMeta(article, relativeAge),
                color = Color(0x99FFFFFF),
                fontSize = 12.sp
            )
            Spacer(Modifier.height(10.dp))
            Text("Deutsch lesen  ›", color = HomeAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CompactNewsCard(article: NewsArticle, onOpenDetail: (NewsArticle) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, HomeCardBorder, HomeCardShape)
            .clickable { onOpenDetail(article) }
            .testTag("news-open-detail"),
        colors = CardDefaults.cardColors(containerColor = HomeCardColor),
        shape = HomeCardShape
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(article.category.label.uppercase(), color = HomeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(5.dp))
            Text(
                article.germanTitle,
                color = Color.White,
                fontSize = 16.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(7.dp))
            Text(
                newsMeta(article, relativeNewsAge(article.publishedAt)),
                color = Color(0x99FFFFFF),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun newsMeta(article: NewsArticle, relativeAge: String?): String =
    "${article.source} · ${formatNewsDate(article.publishedAt)}${relativeAge?.let { " · $it" } ?: ""}"

@Composable
private fun NewsMessage(message: String, onRetry: (() -> Unit)? = null) {
    Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("☀", fontSize = 38.sp)
        Spacer(Modifier.height(10.dp))
        Text(message, color = Color(0xD9FFFFFF))
        if (onRetry != null) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = HomeAccent, contentColor = Color(0xFF102126))
            ) { Text("Erneut versuchen") }
        }
    }
}

internal fun formatNewsDate(value: String): String = runCatching {
    val inputPattern = if (value.contains('.')) "yyyy-MM-dd'T'HH:mm:ss.SSSX" else "yyyy-MM-dd'T'HH:mm:ssX"
    val input = SimpleDateFormat(inputPattern, Locale.ROOT)
    val output = SimpleDateFormat("dd.MM., HH:mm 'Uhr'", Locale.GERMANY)
    output.format(requireNotNull(input.parse(value)))
}.getOrDefault(value)

internal fun relativeNewsAge(value: String, nowMillis: Long = System.currentTimeMillis()): String? = runCatching {
    val inputPattern = if (value.contains('.')) "yyyy-MM-dd'T'HH:mm:ss.SSSX" else "yyyy-MM-dd'T'HH:mm:ssX"
    val publishedAt = requireNotNull(SimpleDateFormat(inputPattern, Locale.ROOT).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.parse(value)).time
    val minutes = max(0, (nowMillis - publishedAt) / 60_000)
    when {
        minutes < 1 -> "gerade eben"
        minutes < 60 -> "vor $minutes Min."
        minutes < 24 * 60 -> "vor ${minutes / 60} Std."
        minutes < 7 * 24 * 60 -> "vor ${minutes / (24 * 60)} Tagen"
        else -> null
    }
}.getOrNull()
