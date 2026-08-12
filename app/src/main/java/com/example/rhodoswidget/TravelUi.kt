package com.example.rhodoswidget

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class TravelArea {
    TODAY,
    MOBILITY,
    EXPLORE,
    HELP
}

@Composable
fun TravelScreen(
    padding: PaddingValues,
    scrollToTopRequest: Int = 0,
    onBack: () -> Unit,
    initialScheduleId: String? = null,
    openChecklistDirectly: Boolean = false,
    onDetailVisibilityChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    LaunchedEffect(scrollToTopRequest) {
        if (scrollToTopRequest > 0) listState.animateScrollToItem(0)
    }
    var marineWeather by remember(context) {
        mutableStateOf(MarineWeatherRepository.cached(context))
    }
    var isMarineLoading by remember { mutableStateOf(false) }
    var marineRefreshFailed by remember { mutableStateOf(false) }
    var favorites by remember(context) {
        mutableStateOf(TravelPreferences.favorites(context))
    }
    var completedItems by remember(context) {
        mutableStateOf(TravelPreferences.completedChecklistItems(context))
    }
    var notes by remember(context) {
        mutableStateOf(TravelPreferences.notes(context))
    }
    var transitDocuments by remember(context) {
        mutableStateOf(LiveTravelRepository.cachedTransit(context))
    }
    var events by remember(context) {
        mutableStateOf(LiveTravelRepository.cachedEvents(context))
    }
    var eventTranslations by remember(context) {
        mutableStateOf(TravelTranslationRepository.cached(context, events))
    }
    var isTranslationLoading by remember { mutableStateOf(false) }
    var isTranslationPending by remember { mutableStateOf(false) }
    var selectedSchedule by remember(initialScheduleId) {
        mutableStateOf(initialScheduleId?.let(LiveTravelRepository::placeholderTransitDocument))
    }
    var pendingScheduleId by remember(initialScheduleId) {
        mutableStateOf(initialScheduleId.takeIf { selectedSchedule == null })
    }
    var isLiveLoading by remember { mutableStateOf(false) }
    var liveRefreshFailed by remember { mutableStateOf(false) }
    var showingCachedLiveData by remember { mutableStateOf(transitDocuments.isNotEmpty() || events.isNotEmpty()) }
    var alertsEnabled by remember(context) {
        mutableStateOf(TravelAlertSettings.isEnabled(context))
    }
    var showMobilitySources by rememberSaveable { mutableStateOf(false) }
    var showFerrySources by rememberSaveable { mutableStateOf(false) }
    var showMoreHelp by rememberSaveable { mutableStateOf(false) }
    var selectedArea by rememberSaveable { mutableStateOf<TravelArea?>(null) }

    LaunchedEffect(selectedSchedule, openChecklistDirectly, selectedArea) {
        onDetailVisibilityChanged(
            selectedSchedule != null || openChecklistDirectly || selectedArea != null
        )
    }
    DisposableEffect(Unit) {
        onDispose { onDetailVisibilityChanged(false) }
    }
    BackHandler(enabled = selectedSchedule != null || openChecklistDirectly || selectedArea != null) {
        when {
            initialScheduleId != null || openChecklistDirectly -> onBack()
            selectedSchedule != null -> selectedSchedule = null
            else -> selectedArea = null
        }
    }
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            TravelAlertSettings.setEnabled(context.applicationContext, true)
            alertsEnabled = true
        }
    }
    val weather = WeatherRepository.cached(context)
    val openSource: (String) -> Unit = { url ->
        if (isTrustedTravelUrl(url)) runCatching { uriHandler.openUri(url) }
    }
    val openMap: (String) -> Unit = { query ->
        val uri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
    }
    val callNumber: (String) -> Unit = { number ->
        runCatching {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
        }
    }
    val refreshMarine: () -> Unit = {
        if (!isMarineLoading) {
            scope.launch {
                isMarineLoading = true
                marineRefreshFailed = false
                val fresh = withContext(Dispatchers.IO) {
                    MarineWeatherRepository.fetch()?.also {
                        MarineWeatherRepository.save(context.applicationContext, it)
                    }
                }
                if (fresh != null) marineWeather = fresh else marineRefreshFailed = true
                isMarineLoading = false
            }
        }
    }
    val refreshLiveData: () -> Unit = {
        if (!isLiveLoading) {
            scope.launch {
                isLiveLoading = true
                liveRefreshFailed = false
                val (freshTransit, freshEvents) = withContext(Dispatchers.IO) {
                    LiveTravelRepository.fetchTransit() to LiveTravelRepository.fetchEvents()
                }
                freshTransit?.let {
                    transitDocuments = it
                    LiveTravelRepository.saveTransit(context.applicationContext, it)
                }
                freshEvents?.let {
                    events = it
                    LiveTravelRepository.saveEvents(context.applicationContext, it)
                }
                liveRefreshFailed = freshTransit == null || freshEvents == null
                showingCachedLiveData = liveRefreshFailed
                isLiveLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        val ageMillis = System.currentTimeMillis() - (marineWeather?.fetchedAtMillis ?: 0L)
        if (marineWeather == null || ageMillis > 3 * 60 * 60 * 1000L) refreshMarine()
        refreshLiveData()
    }
    LaunchedEffect(events) {
        val missingTranslation = events.any { event ->
            event.id !in eventTranslations &&
                (TravelTranslationRepository.containsGreek(event.title) ||
                    TravelTranslationRepository.containsGreek(event.venue.orEmpty()))
        }
        if (missingTranslation) {
            isTranslationLoading = true
            eventTranslations = withContext(Dispatchers.IO) {
                TravelTranslationRepository.translateMissing(context.applicationContext, events)
            }
            isTranslationPending = events.any { event ->
                TravelTranslationRepository.containsGreek(event.title) && event.id !in eventTranslations
            }
            isTranslationLoading = false
        } else {
            isTranslationPending = false
        }
    }
    LaunchedEffect(transitDocuments, pendingScheduleId) {
        pendingScheduleId?.let { scheduleId ->
            transitDocuments.firstOrNull { it.id == scheduleId }?.let { document ->
                selectedSchedule = document
                pendingScheduleId = null
            }
        }
    }
    LaunchedEffect(transitDocuments, initialScheduleId) {
        initialScheduleId?.let { scheduleId ->
            transitDocuments.firstOrNull { it.id == scheduleId }?.let { document ->
                selectedSchedule = document
            }
        }
    }

    selectedSchedule?.let { document ->
        TransitPdfScreen(
            padding = padding,
            document = document,
            onBack = {
                if (initialScheduleId != null) onBack() else selectedSchedule = null
            },
            onOpenSource = { openSource(document.sourceUrl) }
        )
        return
    }

    if (openChecklistDirectly) {
        TravelChecklistScreen(
            padding = padding,
            completedItems = completedItems,
            notes = notes,
            onBack = onBack,
            onToggle = { id ->
                completedItems = TravelPreferences.toggleChecklistItem(context, id)
            },
            onNotesChange = { updated ->
                notes = updated
                TravelPreferences.saveNotes(context, updated)
            }
        )
        return
    }

    if (selectedArea == null) {
        TravelOverviewScreen(
            padding = padding,
            listState = listState,
            onOpenArea = { selectedArea = it }
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF142E34), Color(0xFF0D1113))))
            .padding(padding)
            .testTag("travel-area-screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TravelAreaDetailHeader(
                area = selectedArea ?: return@item,
                onBack = { selectedArea = null }
            )
        }
        when (selectedArea) {
            TravelArea.TODAY -> {
                item {
                    MarineWeatherCard(
                        marineWeather = marineWeather,
                        weather = weather,
                        isLoading = isMarineLoading,
                        refreshFailed = marineRefreshFailed,
                        onRefresh = refreshMarine
                    )
                }
                item {
                    TravelAlertsCard(
                        enabled = alertsEnabled,
                        onEnabledChange = { enabled ->
                            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                                PackageManager.PERMISSION_GRANTED
                            ) {
                                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                TravelAlertSettings.setEnabled(context.applicationContext, enabled)
                                alertsEnabled = enabled
                            }
                        }
                    )
                }
            }
            TravelArea.MOBILITY -> {
                item { BusOverviewCard() }
                item {
                    LiveTimetablesCard(
                        documents = transitDocuments,
                        isLoading = isLiveLoading,
                        refreshFailed = liveRefreshFailed,
                        isCached = showingCachedLiveData,
                        onRefresh = refreshLiveData,
                        onOpen = { selectedSchedule = it }
                    )
                }
                item {
                    ExpandableTravelGroup(
                        titleRes = R.string.travel_more_mobility_sources,
                        testTag = "travel-more-mobility",
                        expanded = showMobilitySources,
                        onToggle = { showMobilitySources = !showMobilitySources }
                    )
                }
                if (showMobilitySources) {
                    items(travelSources, key = { "mobility_${it.url}" }) { source ->
                        TravelSourceCard(source = source, onOpen = { openSource(source.url) })
                    }
                    item { SupportingTravelText(R.string.travel_sources_hint) }
                }
            }
            TravelArea.EXPLORE -> {
                item {
                    LiveEventsCard(
                        events = events,
                        translations = eventTranslations,
                        isTranslationLoading = isTranslationLoading,
                        isTranslationPending = isTranslationPending,
                        isLoading = isLiveLoading,
                        refreshFailed = liveRefreshFailed,
                        isCached = showingCachedLiveData,
                        onRefresh = refreshLiveData,
                        onOpen = { openSource(it.url) },
                        onTranslationInfo = {
                            runCatching { uriHandler.openUri("https://translate.google.com/") }
                        }
                    )
                }
                items(excursionIdeas, key = { it.id }) { idea ->
                    ExcursionCard(
                        idea = idea,
                        isFavorite = idea.id in favorites,
                        onToggleFavorite = {
                            favorites = TravelPreferences.toggleFavorite(context, idea.id)
                        },
                        onMap = { openMap(idea.mapQuery) },
                        onOpen = { openSource(idea.url) }
                    )
                }
                item {
                    ExpandableTravelGroup(
                        titleRes = R.string.travel_more_ferry_sources,
                        testTag = "travel-more-explore",
                        expanded = showFerrySources,
                        onToggle = { showFerrySources = !showFerrySources }
                    )
                }
                if (showFerrySources) {
                    items(ferryAndEventSources, key = { "explore_${it.url}" }) { source ->
                        TravelSourceCard(source = source, onOpen = { openSource(source.url) })
                    }
                    item { SupportingTravelText(R.string.travel_offline_hint) }
                }
            }
            TravelArea.HELP -> {
                item { EmergencyCard(onCall = { callNumber("112") }) }
                item {
                    ExpandableTravelGroup(
                        titleRes = R.string.travel_more_help,
                        testTag = "travel-more-help",
                        expanded = showMoreHelp,
                        onToggle = { showMoreHelp = !showMoreHelp }
                    )
                }
                if (showMoreHelp) {
                    item { EmergencyContactsCard(emergencyContacts, callNumber) }
                    item {
                        TravelMapHelpCard(
                            onHospital = { openMap("General Hospital of Rhodes") },
                            onPharmacy = { openMap("pharmacy near me") }
                        )
                    }
                    item { SupportingTravelText(R.string.travel_emergency_disclaimer) }
                }
                item {
                    TravelChecklistCard(
                        items = travelChecklist,
                        completedIds = completedItems,
                        notes = notes,
                        onToggle = { id ->
                            completedItems = TravelPreferences.toggleChecklistItem(context, id)
                        },
                        onNotesChange = { updated ->
                            notes = updated
                            TravelPreferences.saveNotes(context, updated)
                        }
                    )
                }
            }
            null -> Unit
        }
    }
}

@Composable
private fun TravelOverviewScreen(
    padding: PaddingValues,
    listState: LazyListState,
    onOpenArea: (TravelArea) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF142E34), Color(0xFF0D1113))))
            .padding(padding)
            .testTag("travel-screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { TravelHeader() }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TravelOverviewCard(
                        titleRes = R.string.travel_area_today,
                        descriptionRes = R.string.travel_area_today_description,
                        testTag = "travel-area-today",
                        onClick = { onOpenArea(TravelArea.TODAY) },
                        modifier = Modifier.weight(1f)
                    )
                    TravelOverviewCard(
                        titleRes = R.string.travel_area_mobility,
                        descriptionRes = R.string.travel_area_mobility_description,
                        testTag = "travel-area-mobility",
                        onClick = { onOpenArea(TravelArea.MOBILITY) },
                        modifier = Modifier.weight(1f),
                        emphasized = true
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TravelOverviewCard(
                        titleRes = R.string.travel_area_explore,
                        descriptionRes = R.string.travel_area_explore_description,
                        testTag = "travel-area-explore",
                        onClick = { onOpenArea(TravelArea.EXPLORE) },
                        modifier = Modifier.weight(1f)
                    )
                    TravelOverviewCard(
                        titleRes = R.string.travel_area_help,
                        descriptionRes = R.string.travel_area_help_description,
                        testTag = "travel-area-help",
                        onClick = { onOpenArea(TravelArea.HELP) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TravelOverviewCard(
    titleRes: Int,
    descriptionRes: Int,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false
) {
    Column(
        modifier = modifier
            .heightIn(min = 132.dp)
            .background(
                if (emphasized) HomeAccent.copy(alpha = 0.22f) else HomeCardColor,
                HomeCardShape
            )
            .border(
                1.dp,
                if (emphasized) HomeAccent.copy(alpha = 0.85f) else HomeCardBorder,
                HomeCardShape
            )
            .testTag(testTag)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(titleRes),
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Montserrat
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(descriptionRes),
            color = Color(0xC7FFFFFF),
            fontSize = 10.sp,
            lineHeight = 15.sp,
            fontFamily = Montserrat
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.travel_area_open),
            color = HomeAccent,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Montserrat
        )
    }
}

@Composable
private fun TravelAreaDetailHeader(area: TravelArea, onBack: () -> Unit) {
    val titleRes = when (area) {
        TravelArea.TODAY -> R.string.travel_area_today
        TravelArea.MOBILITY -> R.string.travel_area_mobility
        TravelArea.EXPLORE -> R.string.travel_area_explore
        TravelArea.HELP -> R.string.travel_area_help
    }
    Column {
        TextButton(onClick = onBack, modifier = Modifier.testTag("travel-area-back")) {
            Text(stringResource(R.string.travel_back), color = HomeAccent)
        }
        Text(
            text = stringResource(titleRes),
            color = Color.White,
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Montserrat
        )
    }
}

@Composable
private fun TravelChecklistScreen(
    padding: PaddingValues,
    completedItems: Set<String>,
    notes: String,
    onBack: () -> Unit,
    onToggle: (String) -> Unit,
    onNotesChange: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF142E34), Color(0xFF0D1113))))
            .padding(padding)
            .testTag("travel-checklist-screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                TextButton(onClick = onBack) {
                    Text(stringResource(R.string.travel_back), color = HomeAccent)
                }
                Text(
                    text = stringResource(R.string.travel_checklist_screen_title),
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Montserrat
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.travel_checklist_screen_intro),
                    color = Color(0xCCFFFFFF),
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    fontFamily = Montserrat
                )
            }
        }
        item { SectionLabel(R.string.travel_list_section) }
        item {
            TravelChecklistCard(
                items = travelChecklist,
                completedIds = completedItems,
                notes = notes,
                onToggle = onToggle,
                onNotesChange = onNotesChange
            )
        }
    }
}

@Composable
private fun TravelHeader() {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(R.string.travel_screen_label),
                color = HomeAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = stringResource(R.string.travel_screen_title),
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Montserrat,
            modifier = Modifier.testTag("travel-header-title")
        )
        Text(
            text = stringResource(R.string.travel_screen_intro),
            color = Color(0xBFFFFFFF),
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontFamily = Montserrat
        )
    }
}

@Composable
private fun SectionLabel(textRes: Int) {
    Text(
        text = stringResource(textRes),
        color = HomeAccent,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(top = 8.dp, start = 4.dp)
    )
}

@Composable
private fun ExpandableTravelGroup(
    titleRes: Int,
    testTag: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    TextButton(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth().testTag(testTag)
    ) {
        Text(
            text = stringResource(titleRes),
            color = Color(0xCCFFFFFF),
            fontFamily = Montserrat,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (expanded) "−" else "+",
            color = HomeAccent,
            fontSize = 20.sp
        )
    }
}

@Composable
private fun SupportingTravelText(textRes: Int) {
    Text(
        text = stringResource(textRes),
        color = Color(0x99FFFFFF),
        fontSize = 10.sp,
        lineHeight = 15.sp,
        fontFamily = Montserrat,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}

@Composable
private fun BusOverviewCard() {
    val destinations = listOf(
        R.string.travel_bus_rhodes,
        R.string.travel_bus_lindos,
        R.string.travel_bus_tsambika,
        R.string.travel_bus_seven_springs,
        R.string.travel_bus_kallithea
    )
    TravelCardContainer {
        Text(
            text = stringResource(R.string.travel_bus_intro),
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Montserrat
        )
        Spacer(Modifier.height(8.dp))
        destinations.forEach { destination ->
            Text(
                text = "• ${stringResource(destination)}",
                color = Color(0xE6FFFFFF),
                fontSize = 12.sp,
                lineHeight = 20.sp,
                fontFamily = Montserrat
            )
        }
    }
}

@Composable
private fun TravelSourceCard(source: TravelSource, onOpen: () -> Unit) {
    TravelCardContainer {
        Text(
            text = stringResource(source.titleRes),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Montserrat
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(source.descriptionRes),
            color = Color(0xD9FFFFFF),
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontFamily = Montserrat
        )
        TextButton(onClick = onOpen, contentPadding = PaddingValues(top = 6.dp)) {
            Text(
                text = stringResource(R.string.travel_open_source),
                color = HomeAccent,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ExcursionCard(
    idea: ExcursionIdea,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onMap: () -> Unit,
    onOpen: () -> Unit
) {
    TravelCardContainer {
        Text(
            text = stringResource(idea.titleRes),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Montserrat
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = stringResource(idea.metaRes),
            color = HomeAccent,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Montserrat
        )
        Spacer(Modifier.height(7.dp))
        Text(
            text = stringResource(idea.descriptionRes),
            color = Color(0xE6FFFFFF),
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontFamily = Montserrat
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onToggleFavorite) {
                Text(
                    text = stringResource(
                        if (isFavorite) R.string.travel_favorite_remove
                        else R.string.travel_favorite_add
                    ),
                    color = if (isFavorite) Color.White else HomeAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onMap) {
                Text(
                    text = stringResource(R.string.travel_map_action),
                    color = HomeAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        TextButton(onClick = onOpen, contentPadding = PaddingValues(top = 2.dp)) {
            Text(
                text = stringResource(R.string.travel_more_information),
                color = HomeAccent,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EmergencyCard(onCall: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x331D9A6C), HomeCardShape)
            .border(1.dp, Color(0xFF5ED6A2), HomeCardShape)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.travel_emergency_title),
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Montserrat
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.travel_emergency_description),
            color = Color(0xE6FFFFFF),
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontFamily = Montserrat
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onCall,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5ED6A2),
                contentColor = Color(0xFF102126)
            )
        ) {
            Text(
                text = stringResource(R.string.travel_emergency_action),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
internal fun TravelCardContainer(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HomeCardColor, HomeCardShape)
            .border(1.dp, HomeCardBorder, HomeCardShape)
            .padding(16.dp),
        content = content
    )
}
