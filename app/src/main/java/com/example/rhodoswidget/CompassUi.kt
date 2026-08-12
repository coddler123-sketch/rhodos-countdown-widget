package com.example.rhodoswidget

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CompassTip(
    val category: String,
    val title: String,
    val description: String,
    val note: String,
    val kind: CompassTipKind = CompassTipKind.RECOMMENDATION
)

enum class CompassTipKind(val label: String) {
    RECOMMENDATION("EMPFOHLEN"),
    NOTE("HINWEIS"),
    CAUTION("BEACHTEN")
}

internal val compassTips = listOf(
    CompassTip("Essen", "Taverne Akti", "Leckeres Essen, faire Preise und freundlicher Service.", "Besonders häufig empfohlen"),
    CompassTip("Essen", "Stama", "Kleine Speisekarte und sehr herzlicher Service.", "Persönliche Empfehlung aus der Gruppe"),
    CompassTip("Strände", "Tsambika / Tsampika", "Weitläufiger Strand mit Restaurant und Liegen auf der linken Seite.", "Kann tagsüber sehr voll werden", CompassTipKind.CAUTION),
    CompassTip("Strände", "Stegna", "Schöner Strand mit guten Möglichkeiten zum Essen.", "In der Hauptzeit mehr Andrang", CompassTipKind.NOTE),
    CompassTip("Strände", "Pefkoi Plakia Beach", "Ruhige Pause am Meer bei der Blue Waves Cantine.", "Tipp für einen entspannten Strandtag"),
    CompassTip("Strände", "Elli Beach", "Zentraler Stadtstrand mit derzeit gemischten Erfahrungen.", "Sauberkeit vor Ort prüfen", CompassTipKind.CAUTION),
    CompassTip("Ausflüge", "Mandraki vor Sonnenaufgang", "Ruhiges Licht, Altstadtkulisse und einlaufende Schiffe.", "Am besten sehr früh besuchen"),
    CompassTip("Mobilität", "Mietwagenbedingungen prüfen", "Selbstbeteiligung, Kaution, Shuttle und Versicherung schriftlich bestätigen lassen.", "Vor der Buchung klären", CompassTipKind.CAUTION),
    CompassTip("Unterkünfte", "Elysium bei Faliraki", "Strand, Service und Lage wurden positiv hervorgehoben.", "Einzelne sehr positive Erfahrung", CompassTipKind.NOTE),
    CompassTip("Unterkünfte", "Kresten Palace", "Mehrere positive Rückmeldungen zu Hotel und Aufenthalt.", "Steiler Weg zum Strand", CompassTipKind.CAUTION),
    CompassTip("Unterkünfte", "Lydia Maris Resort", "Von einem wiederkehrenden Gast erneut positiv bewertet.", "Erfahrung eines Stammgasts", CompassTipKind.NOTE),
    CompassTip("Unterkünfte", "Esperides Beach Family", "Familienhotel mit Pool, direkter Meerlage und gutem Essen.", "Gut für Familien"),
    CompassTip("Unterkünfte", "Blue Sea Beach", "Positiver erster Eindruck in direkter Strandlage.", "Für Abendbummel außerhalb von Faliraki", CompassTipKind.CAUTION)
)

@Composable
fun CompassScreen(
    padding: PaddingValues,
    scrollToTopRequest: Int = 0,
    onOpenCommunity: () -> Unit,
    onDetailVisibilityChanged: (Boolean) -> Unit = {}
) {
    val listState = rememberLazyListState()
    LaunchedEffect(scrollToTopRequest) {
        if (scrollToTopRequest > 0) listState.animateScrollToItem(0)
    }
    val categories = remember {
        listOf(
            CompassCategory("Essen", "Tavernen und persönliche Empfehlungen"),
            CompassCategory("Strände", "Strände für lebhafte und ruhige Tage"),
            CompassCategory("Ausflüge", "Besondere Orte und passende Besuchszeiten"),
            CompassCategory("Mobilität", "Wichtige Hinweise für Mietwagen und Wege"),
            CompassCategory("Unterkünfte", "Erfahrungen mit Hotels auf Rhodos")
        )
    }
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }
    val visible = remember(selectedCategory) {
        compassTips.filter { it.category == selectedCategory }
    }
    LaunchedEffect(selectedCategory) {
        onDetailVisibilityChanged(selectedCategory != null)
    }
    DisposableEffect(Unit) {
        onDispose { onDetailVisibilityChanged(false) }
    }
    BackHandler(enabled = selectedCategory != null) { selectedCategory = null }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF142E34), Color(0xFF0D1113))))
            .padding(padding)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .testTag(if (selectedCategory == null) "compass-screen" else "compass-category-screen"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (selectedCategory == null) {
                item { CompassOverviewHeader() }
                items(categories.chunked(2), key = { row -> row.first().title }) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { category ->
                            CompassCategoryCard(
                                category = category,
                                count = compassTips.count { it.category == category.title },
                                modifier = Modifier.weight(1f),
                                onClick = { selectedCategory = category.title }
                            )
                        }
                        if (row.size == 1) {
                            CompassCommunityCard(
                                modifier = Modifier.weight(1f),
                                onClick = onOpenCommunity
                            )
                        }
                    }
                }
            } else {
                item {
                    CompassCategoryHeader(
                        title = selectedCategory.orEmpty(),
                        count = visible.size,
                        onBack = { selectedCategory = null }
                    )
                }
                items(visible, key = { it.title }) { tip -> CompassTipCard(tip) }
                item {
                    Text(
                        "Persönliche Erfahrungen können sich ändern. Preise, Öffnungszeiten und Bedingungen bitte vor Ort prüfen.",
                        color = Color(0x80FFFFFF), fontSize = 10.sp, lineHeight = 15.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

private data class CompassCategory(
    val title: String,
    val description: String
)

@Composable
private fun CompassOverviewHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.weight(1f))
        Text("13 AUSGEWÄHLTE TIPPS", color = HomeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
    Text("Rhodos Tipps", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, fontFamily = Montserrat)
    Text(
        "Wähle ein Thema für Empfehlungen und persönliche Erfahrungen.",
        color = Color(0xBFFFFFFF), fontSize = 12.sp, lineHeight = 18.sp, fontFamily = Montserrat
    )
}

@Composable
private fun CompassCategoryCard(
    category: CompassCategory,
    count: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .heightIn(min = 132.dp)
            .background(HomeCardColor, HomeCardShape)
            .border(1.dp, HomeCardBorder, HomeCardShape)
            .clickable(onClick = onClick)
            .testTag("compass-category-${category.title}")
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(category.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, fontFamily = Montserrat)
            Spacer(Modifier.height(6.dp))
            Text(category.description, color = Color(0xBFFFFFFF), fontSize = 11.sp, lineHeight = 16.sp, fontFamily = Montserrat)
        }
        Text("$count TIPPS  ›", color = HomeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CompassCommunityCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .heightIn(min = 132.dp)
            .background(HomeCardColor, HomeCardShape)
            .border(1.dp, HomeCardBorder, HomeCardShape)
            .clickable(onClick = onClick)
            .testTag("community-link")
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Facebook", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, fontFamily = Montserrat)
            Spacer(Modifier.height(6.dp))
            Text("Weitere Erfahrungen aus der Rhodos-Community", color = Color(0xBFFFFFFF), fontSize = 11.sp, lineHeight = 16.sp, fontFamily = Montserrat)
        }
        Text("COMMUNITY  ↗", color = HomeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CompassCategoryHeader(
    title: String,
    count: Int,
    onBack: () -> Unit
) {
    Text(
        "‹ Alle Tipps",
        color = HomeAccent,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clickable(onClick = onBack)
            .testTag("compass-category-back")
            .padding(vertical = 12.dp)
    )
    Text(title, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, fontFamily = Montserrat)
    Text("$count persönliche Tipps", color = Color(0xBFFFFFFF), fontSize = 12.sp, fontFamily = Montserrat)
}

@Composable
private fun CompassTipCard(tip: CompassTip) {
    val statusColor = when (tip.kind) {
        CompassTipKind.RECOMMENDATION -> HomeAccent
        CompassTipKind.NOTE -> Color(0xFF8DD7E3)
        CompassTipKind.CAUTION -> Color(0xFFFFB86B)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HomeCardColor, HomeCardShape)
            .border(1.dp, HomeCardBorder, HomeCardShape)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(tip.category.uppercase(), color = HomeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
            Text(
                text = tip.kind.label,
                color = statusColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(50))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Spacer(Modifier.height(5.dp))
        Text(tip.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, fontFamily = Montserrat)
        Spacer(Modifier.height(5.dp))
        Text(tip.description, color = Color(0xE6FFFFFF), fontSize = 12.sp, lineHeight = 18.sp, fontFamily = Montserrat)
        Spacer(Modifier.height(8.dp))
        Text(tip.note, color = statusColor, fontSize = 10.sp, fontFamily = Montserrat)
    }
}
