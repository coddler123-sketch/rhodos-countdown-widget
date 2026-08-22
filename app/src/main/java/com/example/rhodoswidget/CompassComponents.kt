package com.example.rhodoswidget

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

internal data class CompassCategory(
    val title: String,
    val description: String,
    @param:DrawableRes val imageRes: Int
)

@Composable
internal fun CompassOverviewHeader(count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.weight(1f))
        Text("$count AUSGEWÄHLTE TIPPS", color = HomeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
    Text("Rhodos Tipps", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, fontFamily = Montserrat)
    Text(
        "Wähle ein Thema für Empfehlungen und persönliche Erfahrungen.",
        color = Color(0xBFFFFFFF), fontSize = 12.sp, lineHeight = 18.sp, fontFamily = Montserrat
    )
}

@Composable
internal fun CompassHeroCard(
    tip: CompassTip,
    isSaved: Boolean,
    onToggleSaved: () -> Unit,
    onOpen: () -> Unit
) {
    val editorial = editorialFor(tip)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(224.dp)
            .clip(HomeCardShape)
            .border(1.dp, HomeCardBorder, HomeCardShape)
            .clickable(onClick = onOpen)
            .testTag("compass-featured-tip")
    ) {
        Image(
            painter = painterResource(editorial.imageRes),
            contentDescription = tip.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0x14000000), Color(0xE6000000))
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(18.dp)
        ) {
            Text(
                "FÜR EUCH EMPFOHLEN",
                color = HomeAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(Modifier.height(5.dp))
            Text(
                tip.title,
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Montserrat
            )
            Spacer(Modifier.height(5.dp))
            Text(
                "${editorial.fromHotel} · ${editorial.bestTime}",
                color = Color(0xE6FFFFFF),
                fontSize = 11.sp,
                fontFamily = Montserrat
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Details ansehen  ›", color = HomeAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = onToggleSaved) {
                    Text(
                        if (isSaved) "♥ GEMERKT" else "♡ MERKEN",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
internal fun CompassQuickFilters(
    selected: String?,
    onSelected: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(listOf("Ohne Auto", "Halber Tag", "Ruhig", "Essen")) { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelected(filter) },
                label = { Text(filter, fontSize = 10.sp, fontFamily = Montserrat) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = HomeCardColor,
                    labelColor = Color(0xCCFFFFFF),
                    selectedContainerColor = HomeAccent.copy(alpha = 0.22f),
                    selectedLabelColor = HomeAccent
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected == filter,
                    borderColor = HomeCardBorder,
                    selectedBorderColor = HomeAccent
                )
            )
        }
    }
}

@Composable
internal fun CompassCategoryFilters(
    categories: List<CompassCategory>,
    selected: String?,
    onSelected: (String?) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            CompassCategoryFilterChip(
                label = "Alle",
                selected = selected == null,
                testTag = "compass-category-filter-all",
                onClick = { onSelected(null) }
            )
        }
        items(categories, key = { it.title }) { category ->
            CompassCategoryFilterChip(
                label = category.title,
                selected = selected == category.title,
                testTag = "compass-category-filter-${category.title}",
                onClick = { onSelected(category.title) }
            )
        }
    }
}

@Composable
private fun CompassCategoryFilterChip(
    label: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 10.sp, fontFamily = Montserrat) },
        modifier = Modifier.testTag(testTag),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = HomeCardColor,
            labelColor = Color(0xCCFFFFFF),
            selectedContainerColor = HomeAccent.copy(alpha = 0.22f),
            selectedLabelColor = HomeAccent
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = HomeCardBorder,
            selectedBorderColor = HomeAccent
        )
    )
}

@Composable
internal fun CompassDayPlanCard(tips: List<CompassTip>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x2924A7A4), HomeCardShape)
            .border(1.dp, HomeAccent.copy(alpha = 0.65f), HomeCardShape)
            .padding(15.dp)
            .testTag("compass-day-plan")
    ) {
        Text("EUER KLEINER TAGESPLAN", color = HomeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
        Spacer(Modifier.height(8.dp))
        tips.forEachIndexed { index, tip ->
            val editorial = editorialFor(tip)
            Text(
                "${index + 1}. ${editorial.bestTime} · ${tip.title}",
                color = Color.White,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontFamily = Montserrat
            )
        }
        Spacer(Modifier.height(6.dp))
        Text("Aus euren gemerkten Tipps · maximal drei Stationen", color = Color(0x99FFFFFF), fontSize = 9.sp)
    }
}
@Composable
internal fun CompassSearchField(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("compass-search"),
        singleLine = true,
        label = { Text("Tipps durchsuchen") },
        placeholder = { Text("z. B. Kolymbia, Bus oder ruhig") },
        trailingIcon = {
            if (query.isNotEmpty()) {
                TextButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier
                        .width(48.dp)
                        .height(48.dp)
                        .semantics { contentDescription = "Suche leeren" },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("×", color = HomeAccent, fontSize = 22.sp)
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = HomeAccent,
            unfocusedBorderColor = HomeCardBorder,
            focusedLabelColor = HomeAccent,
            unfocusedLabelColor = Color(0xBFFFFFFF),
            focusedPlaceholderColor = Color(0x80FFFFFF),
            unfocusedPlaceholderColor = Color(0x80FFFFFF),
            cursorColor = HomeAccent
        ),
        shape = HomeCardShape
    )
}

@Composable
internal fun CompassCategoryCard(
    category: CompassCategory,
    count: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(198.dp)
            .clip(HomeCardShape)
            .border(1.dp, categoryColor(category.title).copy(alpha = 0.55f), HomeCardShape)
            .clickable(onClick = onClick)
            .testTag("compass-category-${category.title}")
    ) {
        Image(
            painter = painterResource(category.imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color(0x42000000),
                        0.42f to Color(0x8F000000),
                        1f to Color(0xF5000000)
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                "$count TIPPS",
                color = categoryColor(category.title),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                modifier = Modifier
                    .background(Color(0xB3000000), RoundedCornerShape(50))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Spacer(Modifier.height(7.dp))
            Text(category.title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, fontFamily = Montserrat)
            Spacer(Modifier.height(4.dp))
            Text(category.description, color = Color.White, fontSize = 11.sp, lineHeight = 15.sp, fontWeight = FontWeight.Medium, fontFamily = Montserrat, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(9.dp))
            Text("ANSEHEN  ›", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
@Composable
internal fun CompassCommunityCard(
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
internal fun CompassTipCard(
    tip: CompassTip,
    isSaved: Boolean,
    isVisited: Boolean,
    expanded: Boolean,
    showCloseButton: Boolean = true,
    onToggleExpanded: () -> Unit,
    onClose: () -> Unit,
    onToggleSaved: () -> Unit,
    onToggleVisited: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenSource: () -> Unit
) {
    val context = LocalContext.current
    val editorial = editorialFor(tip)
    var personalNote by remember(tip.id) {
        mutableStateOf(CompassPreferences.note(context, tip.id))
    }
    LaunchedEffect(tip.id, personalNote) {
        delay(350)
        CompassPreferences.saveNote(context, tip.id, personalNote)
    }
    val statusColor = when (tip.kind) {
        CompassTipKind.RECOMMENDATION -> HomeAccent
        CompassTipKind.NOTE -> Color(0xFF8DD7E3)
        CompassTipKind.CAUTION -> Color(0xFFFFB86B)
    }
    val accent = categoryColor(tip.category)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (tip.kind == CompassTipKind.CAUTION) Color(0xFF28231F) else HomeCardColor,
                HomeCardShape
            )
            .border(1.dp, accent.copy(alpha = 0.45f), HomeCardShape)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpanded)
                .semantics {
                    role = Role.Button
                    contentDescription = "${tip.title}, ${if (expanded) "Details schließen" else "Details öffnen"}"
                }
                .testTag("compass-tip-${tip.id}")
        ) {
            Image(
                painter = painterResource(editorial.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (expanded) 184.dp else 136.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .testTag("compass-tip-image-${tip.id}-${if (expanded) "detail" else "card"}")
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(tip.category.uppercase(), color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tip.kind.label,
                        color = statusColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(50))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    if (expanded && showCloseButton) {
                        TextButton(
                            onClick = onClose,
                            modifier = Modifier
                                .width(48.dp)
                                .height(48.dp)
                                .testTag("compass-close-${tip.id}")
                                .semantics {
                                    contentDescription = "Tipp schließen und zur Tipps-Übersicht zurückkehren"
                                },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("×", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
            Spacer(Modifier.height(7.dp))
            Text(tip.title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, fontFamily = Montserrat)
            Spacer(Modifier.height(5.dp))
            Text(
                tip.description,
                color = Color(0xE6FFFFFF),
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontFamily = Montserrat,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Text(
                listOf(tip.location, editorial.duration, editorial.bestTime).filter(String::isNotBlank).joinToString("  ·  "),
                color = Color(0xBFFFFFFF),
                fontSize = 10.sp,
                fontFamily = Montserrat
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (expanded) "WENIGER ANZEIGEN  ⌃" else "MEHR ANZEIGEN  ›",
                color = accent,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(
                onClick = onToggleSaved,
                modifier = Modifier.testTag("compass-save-${tip.id}")
            ) {
                Text(
                    if (isSaved) "♥ GEMERKT" else "♡ MERKEN",
                    color = if (isSaved) HomeAccent else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            TextButton(onClick = onToggleVisited) {
                Text(if (isVisited) "✓ ERLEDIGT" else "ALS ERLEDIGT", color = if (isVisited) Color(0xFF8DD7E3) else Color(0xBFFFFFFF), fontSize = 9.sp)
            }
        }

        if (expanded) {
            if (tip.tags.isNotEmpty()) {
                Text(tip.tags.joinToString("  ·  "), color = accent, fontSize = 9.sp, fontFamily = Montserrat)
            }
            tip.reviewSummary?.let { summary ->
                Spacer(Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(accent.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
                        .border(1.dp, accent.copy(alpha = 0.30f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        "REZENSIONEN ZUSAMMENGEFASST",
                        color = accent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.7.sp
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        summary,
                        color = Color(0xE6FFFFFF),
                        fontSize = 11.sp,
                        lineHeight = 17.sp,
                        fontFamily = Montserrat
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            CompassFactRow("DAUER", editorial.duration)
            CompassFactRow("BESTE ZEIT", editorial.bestTime)
            CompassFactRow("AB RELAX HOTEL", editorial.fromHotel)
            CompassFactRow("ANREISE", editorial.transport)

            Spacer(Modifier.height(10.dp))
            Text(
                "IM SEPTEMBER",
                color = HomeAccent,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Text(editorial.septemberNote, color = Color(0xE6FFFFFF), fontSize = 11.sp, lineHeight = 17.sp, fontFamily = Montserrat)

            Spacer(Modifier.height(12.dp))
            CompassFactRow("GUT KOMBINIERBAR", editorial.combination)
            CompassFactRow("RÜCKFAHRT", editorial.returnTip)
            CompassFactRow("VOR ORT", editorial.facilities)

            Spacer(Modifier.height(10.dp))
            Text("EHRLICH EINGESCHÄTZT", color = Color(0xFFFFB86B), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
            Text(editorial.counterRecommendation, color = Color(0xFFD9C1A8), fontSize = 11.sp, lineHeight = 17.sp, fontFamily = Montserrat)

            Spacer(Modifier.height(12.dp))
            if (tip.mapsUrl != null) {
                OutlinedButton(
                    onClick = onOpenMap,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("compass-map-${tip.id}"),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.75f))
                ) {
                    Text(
                        "IN GOOGLE MAPS ÖFFNEN  ↗",
                        color = accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
            OutlinedTextField(
                value = personalNote,
                onValueChange = { personalNote = it },
                label = { Text("Eigene Notiz") },
                placeholder = { Text("Treffpunkt, Uhrzeit oder Erinnerung") },
                minLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("compass-note-${tip.id}"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = accent,
                    unfocusedBorderColor = HomeCardBorder,
                    focusedLabelColor = accent,
                    unfocusedLabelColor = Color(0xBFFFFFFF),
                    focusedPlaceholderColor = Color(0x80FFFFFF),
                    unfocusedPlaceholderColor = Color(0x80FFFFFF),
                    cursorColor = accent
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(Modifier.height(10.dp))
            Text(
                if (tip.source == CompassTipSource.COMMUNITY) "PERSÖNLICHE ERFAHRUNG" else "RECHERCHIERTER HINWEIS · STAND ${editorial.checkedAt}",
                color = Color(0x80FFFFFF),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
            editorial.validUntil?.let {
                Text(
                    "GÜLTIGKEIT · $it",
                    color = Color(0xFFFFB86B),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (tip.sourceUrl != null) {
                Text(
                    "${tip.source.label} · QUELLE ↗",
                    color = Color(0x99FFFFFF),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable(onClick = onOpenSource)
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
internal fun CompassFactRow(label: String, value: String) {
    if (value.isBlank()) return
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, color = Color(0x80FFFFFF), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(116.dp))
        Text(value, color = Color(0xE6FFFFFF), fontSize = 11.sp, lineHeight = 16.sp, fontFamily = Montserrat, modifier = Modifier.weight(1f))
    }
}

internal fun categoryColor(category: String): Color = when (category) {
    "Kolymbia" -> Color(0xFF64D8CB)
    "Essen" -> Color(0xFFE8B96A)
    "Strände" -> Color(0xFF70B8E8)
    "Ausflüge" -> Color(0xFFE58B6B)
    "Mobilität" -> Color(0xFF8DD7E3)
    "Unterkünfte" -> Color(0xFF93C98B)
    "Supermärkte" -> Color(0xFF79C98D)
    "Mode & Accessoires" -> Color(0xFFE0A36C)
    "Souvenirs" -> Color(0xFFD6A4E8)
    "Regionale Produkte" -> Color(0xFFD9A86C)
    else -> HomeAccent
}
