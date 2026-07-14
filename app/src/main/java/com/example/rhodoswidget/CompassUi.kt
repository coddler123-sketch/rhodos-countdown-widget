package com.example.rhodoswidget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
    val signal: String
)

internal val compassTips = listOf(
    CompassTip("Essen", "Taverne Akti", "Als sehr lecker, günstig und besonders freundlich empfohlen.", "35 Reaktionen · aktueller Beitrag"),
    CompassTip("Essen", "Stama", "Kleine Speisekarte und besonders herzlicher Service wurden positiv hervorgehoben.", "Ergänzende Community-Empfehlung"),
    CompassTip("Strände", "Tsambika / Tsampika", "Mehrfach positiv genannt, aktuell aber gut besucht. Links am Strand wurden Restaurant und Liegen erwähnt.", "Besucherandrang beachten"),
    CompassTip("Strände", "Stegna", "Strand und Essen vor Ort wurden positiv bewertet; auch hier kann es voller werden.", "Aktueller Erfahrungsbericht"),
    CompassTip("Strände", "Pefkoi Plakia Beach", "Die Blue Waves Cantine wurde als versteckter Hotspot für eine Pause am Meer genannt.", "Konkreter Fund aus dem Feed"),
    CompassTip("Unterkünfte", "Elysium bei Faliraki", "Hotel, Strand, Service und Lage wurden in einem aktuellen Gastbericht sehr positiv bewertet.", "Einzelne positive Erfahrung"),
    CompassTip("Strände", "Elli Beach", "Ein aktueller Beitrag berichtet von mehr Müll als in den Vorjahren.", "Einzelne aktuelle Beobachtung"),
    CompassTip("Stadt", "Mandraki vor Sonnenaufgang", "Früh am Morgen sorgen ruhiges Licht, Altstadtkulisse und einlaufende Schiffe für eine besondere Stimmung.", "Konkreter Zeitpunkt"),
    CompassTip("Unterkünfte", "Kresten Palace", "Mehrere positive Rückmeldungen; zum Strand führt ein steiler Weg bergab und wieder hinauf.", "Mehrere Erfahrungen"),
    CompassTip("Unterkünfte", "Lydia Maris Resort", "Ein wiederkehrender Gast beschreibt den Aufenthalt erneut als sehr angenehm.", "Bericht eines Stammgasts"),
    CompassTip("Unterkünfte", "Esperides Beach Family", "Mehrfach als Familienhotel mit Pool, direkter Meerlage und gutem Essen unterstützt.", "Community-Tipp für Familien"),
    CompassTip("Unterkünfte", "Blue Sea Beach", "Positiver erster Eindruck, aber für einen Abendbummel deutlich außerhalb von Faliraki.", "Lage und Transfer prüfen"),
    CompassTip("Mobilität", "Mietwagenbedingungen prüfen", "Selbstbeteiligung, Kaution, Shuttle und Versicherung vor der Buchung schriftlich bestätigen lassen.", "Praktischer Warnhinweis")
)

@Composable
fun CompassScreen(padding: PaddingValues, onBack: () -> Unit) {
    val categories = remember { listOf("Alle") + compassTips.map { it.category }.distinct() }
    var selected by rememberSaveable { mutableStateOf("Alle") }
    val visible = remember(selected) {
        if (selected == "Alle") compassTips else compassTips.filter { it.category == selected }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF142E34), Color(0xFF0D1113))))
            .padding(padding)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("compass-screen"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onBack) { Text("‹ Zurück", color = HomeAccent) }
                    Spacer(Modifier.weight(1f))
                    Text("13 COMMUNITY-TIPPS", color = HomeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Text("Rhodos Kompass", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, fontFamily = Montserrat)
                Text(
                    "Fester Stand aus sichtbaren Community-Beiträgen. Angaben bitte vor Ort prüfen.",
                    color = Color(0xBFFFFFFF), fontSize = 12.sp, lineHeight = 18.sp, fontFamily = Montserrat
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = selected == category,
                            onClick = { selected = category },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HomeAccent,
                                selectedLabelColor = Color(0xFF102126),
                                labelColor = Color.White
                            )
                        )
                    }
                }
            }
            items(visible, key = { it.title }) { tip -> CompassTipCard(tip) }
            item {
                Text(
                    "Die Hinweise sind persönliche Erfahrungen und werden nicht automatisch aktualisiert.",
                    color = Color(0x80FFFFFF), fontSize = 10.sp, lineHeight = 15.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun CompassTipCard(tip: CompassTip) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HomeCardColor, HomeCardShape)
            .border(1.dp, HomeCardBorder, HomeCardShape)
            .padding(16.dp)
    ) {
        Text(tip.category.uppercase(), color = HomeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
        Spacer(Modifier.height(5.dp))
        Text(tip.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, fontFamily = Montserrat)
        Spacer(Modifier.height(5.dp))
        Text(tip.description, color = Color(0xE6FFFFFF), fontSize = 12.sp, lineHeight = 18.sp, fontFamily = Montserrat)
        Spacer(Modifier.height(8.dp))
        Text(tip.signal, color = Color(0xA6FFFFFF), fontSize = 10.sp, fontFamily = Montserrat)
    }
}
