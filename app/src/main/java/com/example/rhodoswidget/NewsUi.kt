package com.example.rhodoswidget

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max

private val Sea = Color(0xFF1F8796)
private val Sun = Color(0xFFF4B942)
private val Sand = Color(0xFFFFF3D8)

@Composable
fun NewsTicker(state: NewsUiState, onOpenNews: () -> Unit) {
    val articles = (state as? NewsUiState.Content)?.articles.orEmpty().take(10)
    var index by remember(articles) { mutableIntStateOf(0) }
    LaunchedEffect(articles) {
        while (articles.size > 1) {
            delay(6_000)
            index = (index + 1) % articles.size
        }
    }

    val shape = RoundedCornerShape(16.dp)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("news-ticker")
            .border(1.dp, Color(0x665DB7BE), shape)
            .clickable(onClick = onOpenNews),
        colors = CardDefaults.cardColors(containerColor = Color(0xD9FFF8E8)),
        shape = shape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("INSEL-NEWS", color = Sea, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
            Spacer(Modifier.width(10.dp))
            when {
                articles.isNotEmpty() -> AnimatedContent(
                    targetState = articles[index.coerceAtMost(articles.lastIndex)],
                    modifier = Modifier.weight(1f),
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "newsTicker"
                ) { article ->
                    Text(
                        article.germanTitle,
                        color = Color(0xFF183B43),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                state is NewsUiState.Loading -> Text("Aktuelles von Rhodos wird geladen …", color = Sea)
                else -> Text("Aktuelles von Rhodos öffnen", color = Sea, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.width(6.dp))
            Text("›", color = Sea.copy(alpha = 0.72f), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun NewsScreen(
    state: NewsUiState,
    padding: PaddingValues,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onOpenDetail: (NewsArticle) -> Unit
) {
    var selected by remember { mutableStateOf(NewsCategory.ALL) }
    val articles = (state as? NewsUiState.Content)?.articles.orEmpty().filter {
        selected == NewsCategory.ALL || it.category == selected
    }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFFFFF8E8), Color(0xFFE8F7F7), Color.White))
        )
    ) {
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("‹", modifier = Modifier.clickable(onClick = onBack).padding(8.dp), color = Sea, fontSize = 34.sp)
                Column(Modifier.weight(1f)) {
                    Text("Aktuelles von Rhodos", color = Color(0xFF174954), fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Inselinfos für unterwegs", color = Sea, fontSize = 13.sp)
                }
                TextButton(
                    onClick = onRefresh,
                    enabled = (state as? NewsUiState.Content)?.isRefreshing != true
                ) { Text("Aktualisieren", color = Sea, fontWeight = FontWeight.Bold) }
            }
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NewsCategory.entries.forEach { category ->
                    FilterChip(
                        selected = selected == category,
                        onClick = { selected = category },
                        label = { Text(category.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White.copy(alpha = 0.45f),
                            labelColor = Sea,
                            selectedContainerColor = Sand,
                            selectedLabelColor = Color(0xFF174954)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (selected == category) Sea.copy(alpha = 0.24f)
                            else Sea.copy(alpha = 0.55f)
                        )
                    )
                }
            }
            Text(
                "Weitere Filter ›",
                color = Sea.copy(alpha = 0.72f),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.End).padding(end = 20.dp, top = 2.dp)
            )
            Spacer(Modifier.height(10.dp))
            when {
                state is NewsUiState.Loading -> NewsMessage("Die neuesten Inselmeldungen werden geladen …")
                state is NewsUiState.Error -> NewsMessage(state.message, onRefresh)
                state is NewsUiState.Empty -> NewsMessage(state.message, onRefresh)
                articles.isEmpty() -> NewsMessage("Für diesen Filter gibt es gerade keine Meldungen.")
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val content = state as NewsUiState.Content
                    content.warning?.let { item { Text(it, color = Color(0xFF8A5B00), fontSize = 13.sp) } }
                    if (content.isCached) item { Text("Offline verfügbar · zuletzt geladener Stand", color = Sea, fontSize = 12.sp) }
                    items(articles, key = NewsArticle::id) { NewsCard(it, onOpenDetail) }
                    item { Text("Automatisch aus dem Griechischen übersetzt", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp)) }
                }
            }
        }
    }
}

@Composable
private fun NewsCard(article: NewsArticle, onOpenDetail: (NewsArticle) -> Unit) {
    val context = LocalContext.current
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(18.dp)) {
            Text(article.category.label.uppercase(), color = Sea, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(7.dp))
            Text(
                article.germanTitle,
                color = Color(0xFF173D46),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Text(
                article.germanSummary,
                color = Color(0xFF496268),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(12.dp))
            val relativeAge = relativeNewsAge(article.publishedAt)
            Text(
                "${article.source} · ${formatNewsDate(article.publishedAt)}${relativeAge?.let { " · $it" } ?: ""}",
                color = Color.Gray,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(article.originalUrl))) }
                ) { Text("Auf Griechisch", color = Sea, fontWeight = FontWeight.SemiBold) }
                Spacer(Modifier.width(6.dp))
                Button(
                    onClick = { onOpenDetail(article) },
                    modifier = Modifier.testTag("news-open-detail"),
                    colors = ButtonDefaults.buttonColors(containerColor = Sun, contentColor = Color(0xFF3D2B00))
                ) { Text("Deutsch lesen", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun NewsMessage(message: String, onRetry: (() -> Unit)? = null) {
    Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("☀", fontSize = 38.sp)
        Spacer(Modifier.height(10.dp))
        Text(message, color = Color(0xFF496268))
        if (onRetry != null) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = Sea)) { Text("Erneut versuchen") }
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
