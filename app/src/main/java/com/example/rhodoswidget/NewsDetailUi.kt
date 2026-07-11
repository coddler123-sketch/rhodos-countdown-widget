package com.example.rhodoswidget

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val DetailSea = Color(0xFF1F8796)
private val DetailSun = Color(0xFFF4B942)

@Composable
fun NewsDetailScreen(article: NewsArticle, padding: PaddingValues, onBack: () -> Unit) {
    val context = LocalContext.current
    val controller = remember(article.id) { NewsDetailController(context.applicationContext, article) }
    val scope = rememberCoroutineScope()
    val listState = remember(article.id) { LazyListState() }
    LaunchedEffect(controller) { controller.load() }
    LaunchedEffect(controller.state) {
        if (controller.state is NewsDetailUiState.Content) {
            withFrameNanos { }
            listState.scrollToItem(0, 0)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFFFFF8E8), Color(0xFFE8F7F7), Color.White))
        )
    ) {
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "‹",
                    modifier = Modifier.clickable(onClick = onBack).padding(8.dp),
                    color = DetailSea,
                    fontSize = 34.sp
                )
                Column(Modifier.weight(1f)) {
                    Text("Auf Deutsch", color = Color(0xFF174954), fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Rhodos-News kurz erklärt", color = DetailSea, fontSize = 13.sp)
                }
            }

            when (val state = controller.state) {
                is NewsDetailUiState.Preview -> DetailPreview(
                    article = state.article,
                    isLoading = state.isLoading,
                    warning = state.warning,
                    onRetry = { scope.launch { controller.load() } },
                    onOriginal = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(article.originalUrl))) }
                )
                is NewsDetailUiState.Error -> DetailError(
                    message = state.message,
                    onRetry = { scope.launch { controller.load() } },
                    onOriginal = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(article.originalUrl))) }
                )
                is NewsDetailUiState.Content -> LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(Modifier.padding(20.dp)) {
                                Text(state.detail.germanTitle, color = Color(0xFF173D46), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "${state.detail.source} · ${formatNewsDate(state.detail.publishedAt)}",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                                Spacer(Modifier.height(18.dp))
                                Text(state.detail.germanDetail, color = Color(0xFF496268), fontSize = 16.sp, lineHeight = 24.sp)
                            }
                        }
                    }
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3D8)),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(Modifier.padding(18.dp)) {
                                Text("Das Wichtigste", color = Color(0xFF174954), fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                state.detail.keyPoints.forEach { point ->
                                    Text("• $point", color = Color(0xFF496268), modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    }
                    state.warning?.let { warning -> item { Text(warning, color = Color(0xFF8A5B00), fontSize = 13.sp) } }
                    if (state.isCached) item { Text("Offline verfügbar · gespeicherte Zusammenfassung", color = DetailSea, fontSize = 12.sp) }
                    item {
                        Text(
                            "KI-gestützt zusammengefasst · Maßgeblich ist der Originalartikel.",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                    item {
                        Button(
                            onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(state.detail.originalUrl))) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = DetailSun, contentColor = Color(0xFF3D2B00))
                        ) { Text("Originalartikel öffnen", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailPreview(
    article: NewsArticle,
    isLoading: Boolean,
    warning: String?,
    onRetry: () -> Unit,
    onOriginal: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(article.germanTitle, color = Color(0xFF173D46), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Text("${article.source} · ${formatNewsDate(article.publishedAt)}", color = Color.Gray, fontSize = 12.sp)
                    Spacer(Modifier.height(18.dp))
                    Text("Kurzüberblick", color = DetailSea, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(article.germanSummary, color = Color(0xFF496268), fontSize = 16.sp, lineHeight = 24.sp)
                }
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3D8)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(22.dp).width(22.dp),
                            color = DetailSea,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Ausführliche deutsche Zusammenfassung wird geladen …", color = Color(0xFF496268), fontSize = 14.sp)
                    } else {
                        Column {
                            Text(warning ?: "Zusammenfassung nicht verfügbar.", color = Color(0xFF8A5B00), fontSize = 14.sp)
                            Spacer(Modifier.height(10.dp))
                            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = DetailSea)) {
                                Text("Erneut versuchen")
                            }
                        }
                    }
                }
            }
        }
        item {
            Button(
                onClick = onOriginal,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = DetailSun, contentColor = Color(0xFF3D2B00))
            ) { Text("Originalartikel öffnen", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun DetailError(message: String, onRetry: () -> Unit, onOriginal: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, color = Color(0xFF496268))
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = DetailSea)) {
            Text("Erneut versuchen")
        }
        Button(onClick = onOriginal, colors = ButtonDefaults.buttonColors(containerColor = DetailSun, contentColor = Color(0xFF3D2B00))) {
            Text("Original öffnen")
        }
    }
}
