package com.example.rhodoswidget

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar


@Composable
fun CompassScreen(
    padding: PaddingValues,
    scrollToTopRequest: Int = 0,
    onOpenCommunity: () -> Unit,
    onDetailVisibilityChanged: (Boolean) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    LaunchedEffect(scrollToTopRequest) {
        if (scrollToTopRequest > 0) listState.animateScrollToItem(0)
    }
    val categories = remember {
        listOf(
            CompassCategory("Kolymbia", "Tipps direkt rund um euren Urlaubsort", R.drawable.category_kolymbia_harbour),
            CompassCategory("Essen", "Tavernen und regionale Spezialitäten", R.drawable.category_greek_food),
            CompassCategory("Strände", "Lebhafte Buchten und ruhige Badetage", R.drawable.anthony_quinn_bay_rhodes_001),
            CompassCategory("Ausflüge", "Orte, Besuchszeiten und Kombinationen", R.drawable.excursion_lindos),
            CompassCategory("Mobilität", "Bus, Mietwagen und sichere Rückfahrten", R.drawable.category_rhodes_bus),
            CompassCategory("Unterkünfte", "Persönliche Hotelerfahrungen", R.drawable.relax_hotel_kolymbia),
            CompassCategory("Supermärkte", "Lebensmittel, Getränke und Reisebedarf", R.drawable.rhodes_old_town_009),
            CompassCategory("Mode & Accessoires", "Sommermode, Sandalen und Taschen", R.drawable.tip_shop_hashtag),
            CompassCategory("Souvenirs", "Handgemachte und besondere Erinnerungen", R.drawable.lindos_white_houses_bougainvillea_002),
            CompassCategory("Regionale Produkte", "Öl, Honig, Keramik und Spezialitäten", R.drawable.rhodos_1906335)
        )
    }
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTipId by rememberSaveable { mutableStateOf<String?>(null) }
    var categorySelectionRequest by rememberSaveable { mutableStateOf(0) }
    val persistedTips = remember(context) {
        CompassPreferences.migrateLegacyTitles(context, compassTips)
        CompassPreferences.saved(context) to CompassPreferences.visited(context)
    }
    var savedTips by remember(context) { mutableStateOf(persistedTips.first) }
    var visitedTips by remember(context) { mutableStateOf(persistedTips.second) }
    val visible = remember(selectedCategory, searchQuery, selectedFilter) {
        filterCompassTips(compassTips, searchQuery, selectedCategory).filter { tip ->
            selectedFilter == null || matchesCompassQuickFilter(tip, selectedFilter.orEmpty())
        }
    }
    val featuredTip = remember { featuredCompassTip(compassTips, Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) }
    val dayPlan = remember(savedTips) { buildCompassDayPlan(compassTips, savedTips) }
    val selectedTip = remember(selectedTipId) { compassTips.firstOrNull { it.id == selectedTipId } }
    val isOverview = selectedCategory == null && searchQuery.isBlank() && selectedFilter == null

    fun openTip(id: String) {
        onDetailVisibilityChanged(true)
        selectedTipId = id
    }

    fun closeTip() {
        selectedTipId = null
        onDetailVisibilityChanged(false)
    }

    LaunchedEffect(selectedTipId) {
        onDetailVisibilityChanged(selectedTipId != null)
    }
    LaunchedEffect(categorySelectionRequest) {
        if (categorySelectionRequest > 0) listState.animateScrollToItem(0)
    }
    DisposableEffect(Unit) {
        onDispose { onDetailVisibilityChanged(false) }
    }
    BackHandler(
        enabled = selectedTipId != null || selectedCategory != null ||
            searchQuery.isNotBlank() || selectedFilter != null
    ) {
        if (selectedTipId != null) {
            closeTip()
        } else {
            selectedCategory = null
            searchQuery = ""
            selectedFilter = null
            categorySelectionRequest += 1
        }
    }
    fun toggleSaved(id: String) {
        savedTips = CompassPreferences.toggleSaved(context, id)
    }

    fun toggleVisited(id: String) {
        visitedTips = CompassPreferences.toggleVisited(context, id)
    }

    fun selectCategory(category: String) {
        selectedCategory = category
        categorySelectionRequest += 1
    }

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
                .testTag("compass-screen"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { CompassOverviewHeader(compassTips.size) }
            if (isOverview) {
                item {
                    CompassHeroCard(
                        tip = featuredTip,
                        isSaved = featuredTip.id in savedTips,
                        onToggleSaved = { toggleSaved(featuredTip.id) },
                        onOpen = { openTip(featuredTip.id) }
                    )
                }
            }
            item {
                CompassSearchField(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )
            }
            item {
                CompassCategoryFilters(
                    categories = categories,
                    selected = selectedCategory,
                    onSelected = { selectedCategory = it }
                )
            }
            item {
                CompassQuickFilters(
                    selected = selectedFilter,
                    onSelected = { filter ->
                        selectedFilter = if (selectedFilter == filter) null else filter
                    }
                )
            }
            if (isOverview && dayPlan.isNotEmpty()) {
                item { CompassDayPlanCard(dayPlan) }
            }
            if (isOverview) {
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
                                onClick = { selectCategory(category.title) }
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
                item(key = "community") {
                    CompassCommunityCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onOpenCommunity
                    )
                }
            } else {
                item {
                    Text(
                        "${visible.size} Treffer",
                        color = Color(0xBFFFFFFF),
                        fontSize = 12.sp,
                        fontFamily = Montserrat
                    )
                }
                items(visible, key = { it.id }) { tip ->
                    CompassTipCard(
                        tip = tip,
                        isSaved = tip.id in savedTips,
                        isVisited = tip.id in visitedTips,
                        expanded = false,
                        onToggleExpanded = { openTip(tip.id) },
                        onClose = ::closeTip,
                        onToggleSaved = { toggleSaved(tip.id) },
                        onToggleVisited = { toggleVisited(tip.id) },
                        onOpenMap = {
                            tip.mapsUrl?.let(uriHandler::openUri)
                        },
                        onOpenSource = { tip.sourceUrl?.let(uriHandler::openUri) }
                    )
                }
                item {
                    Text(
                        "Persönliche Erfahrungen können sich ändern. Preise, Öffnungszeiten und Bedingungen bitte vor Ort prüfen.",
                        color = Color(0x80FFFFFF), fontSize = 10.sp, lineHeight = 15.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        selectedTip?.let { tip ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0xFF142E34), Color(0xFF0D1113))))
                    .testTag("compass-tip-overlay")
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 76.dp,
                        end = 16.dp,
                        bottom = 32.dp
                    )
                ) {
                    item {
                        CompassTipCard(
                            tip = tip,
                            isSaved = tip.id in savedTips,
                            isVisited = tip.id in visitedTips,
                            expanded = true,
                            showCloseButton = false,
                            onToggleExpanded = ::closeTip,
                            onClose = ::closeTip,
                            onToggleSaved = { toggleSaved(tip.id) },
                            onToggleVisited = { toggleVisited(tip.id) },
                            onOpenMap = {
                                tip.mapsUrl?.let(uriHandler::openUri)
                            },
                            onOpenSource = { tip.sourceUrl?.let(uriHandler::openUri) }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFA101719))
                        .padding(start = 20.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${tip.category.uppercase()} · TIPP",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = Montserrat
                    )
                    TextButton(
                        onClick = ::closeTip,
                        modifier = Modifier
                            .testTag("compass-close-${tip.id}")
                            .semantics {
                                contentDescription = "Tipp schließen und zur Tipps-Übersicht zurückkehren"
                            }
                    ) {
                        Text("×", color = Color.White, fontSize = 28.sp)
                    }
                }
            }
        }
    }
}
