package com.example.rhodoswidget

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rhodoswidget.ui.theme.RhodosWidgetTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

private const val COMMUNITY_URL = "https://www.facebook.com/groups/urlaubrhodos"

class MainActivity : ComponentActivity() {
    companion object {
        const val EXTRA_OPEN_TRAVEL = "open_travel"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        setContent {
            RhodosWidgetTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    RhodosApp(padding, startInTravel = intent.getBooleanExtra(EXTRA_OPEN_TRAVEL, false))
                }
            }
        }
    }
}

@Composable
private fun RhodosApp(padding: PaddingValues, startInTravel: Boolean = false) {
    val context = LocalContext.current
    val newsRepository = remember(context) {
        DefaultNewsRepository(context.applicationContext, BuildConfig.NEWS_API_URL)
    }
    val newsViewModel: NewsViewModel = viewModel(factory = NewsViewModel.factory(newsRepository))
    val newsState by newsViewModel.uiState.collectAsStateWithLifecycle()
    val showNews = remember { mutableStateOf(false) }
    val showCompass = remember { mutableStateOf(false) }
    val showTravel = remember { mutableStateOf(startInTravel) }
    val initialTravelScheduleId = remember { mutableStateOf<String?>(null) }
    val selectedArticle = remember { mutableStateOf<NewsArticle?>(null) }

    val article = selectedArticle.value
    BackHandler(enabled = article != null) {
        selectedArticle.value = null
    }
    BackHandler(enabled = article == null && showNews.value) {
        showNews.value = false
    }
    BackHandler(enabled = article == null && !showNews.value && showCompass.value) {
        showCompass.value = false
    }
    BackHandler(enabled = article == null && !showNews.value && !showCompass.value && showTravel.value) {
        showTravel.value = false
    }

    if (article != null) {
        NewsDetailScreen(
            article = article,
            padding = padding,
            repository = newsRepository,
            onBack = { selectedArticle.value = null }
        )
    } else if (showNews.value) {
        NewsScreen(
            state = newsState,
            padding = padding,
            onBack = { showNews.value = false },
            onRefresh = newsViewModel::refresh,
            onOpenDetail = { selectedArticle.value = it }
        )
    } else if (showTravel.value) {
        TravelScreen(
            padding = padding,
            onBack = {
                initialTravelScheduleId.value = null
                showTravel.value = false
            },
            initialScheduleId = initialTravelScheduleId.value
        )
    } else if (showCompass.value) {
        CompassScreen(padding = padding, onBack = { showCompass.value = false })
    } else {
        RhodosHome(
            padding = padding,
            newsState = newsState,
            onOpenNews = { showNews.value = true },
            onOpenCompass = { showCompass.value = true },
            onOpenTravel = {
                initialTravelScheduleId.value = null
                showTravel.value = true
            },
            onOpenKolymbia = {
                initialTravelScheduleId.value = "ktel_kolymbia"
                showTravel.value = true
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RhodosHome(
    padding: PaddingValues,
    newsState: NewsUiState,
    onOpenNews: () -> Unit,
    onOpenCompass: () -> Unit,
    onOpenTravel: () -> Unit,
    onOpenKolymbia: () -> Unit
) {
    val context = LocalContext.current
    val state = remember { mutableStateOf(HomeState.load(context)) }
    val isRefreshing = remember { mutableStateOf(false) }
    val reloadDone = remember { mutableStateOf(false) }
    val weatherError = remember { mutableStateOf(false) }
    val updateController = remember(context) { AppUpdateController(context) }
    val showSettings = remember { mutableStateOf(false) }
    val showGallery = remember { mutableStateOf(false) }
    val pinnedImage = remember { mutableStateOf(Images.getPinnedImage(context)) }
    val backgroundDim = remember { mutableStateOf(Images.getBackgroundDim(context)) }
    val animatedDim by animateFloatAsState(backgroundDim.value, label = "backgroundDim")
    val scope = rememberCoroutineScope()

    LaunchedEffect(reloadDone.value) {
        if (reloadDone.value) { delay(1_500); reloadDone.value = false }
    }

    LaunchedEffect(updateController) {
        updateController.checkOnStartup()
    }

    LaunchedEffect("weather-autoload") {
        if (state.value.weather == null && !isRefreshing.value) {
            isRefreshing.value = true
            val success = fetchAndSaveWeather(context)
            weatherError.value = !success
            if (success) RhodosCountdownLargeWidgetProvider.updateAllLargeWidgets(context)
            state.value = HomeState.load(context)
            isRefreshing.value = false
            reloadDone.value = success
        }
    }

    LaunchedEffect(Unit) {
        var tick = 0
        while (true) {
            if (tick % 60 == 0) {
                state.value = HomeState.load(context)
            } else {
                val r = CountdownCalculator.calculate()
                state.value = state.value.copy(
                    days = r.days, hours = r.hours,
                    minutes = r.minutes, seconds = r.seconds,
                    isReached = r.isReached, isOnVacation = r.isOnVacation
                )
            }
            tick++
            delay(1_000)
        }
    }

    val s = state.value
    Box(modifier = Modifier.fillMaxSize()) {
        if (s.backgroundRes != 0) {
            Image(
                painter = painterResource(s.backgroundRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = (animatedDim + 0.15f).coerceAtMost(0.95f)),
                            Color.Black.copy(alpha = (animatedDim - 0.10f).coerceAtLeast(0.25f)),
                            Color.Black.copy(alpha = (animatedDim + 0.25f).coerceAtMost(0.95f))
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            updateController.availableUpdate?.let { update ->
                UpdateBanner(
                    update = update,
                    isDownloading = updateController.isWorking,
                    onClick = {
                        if (!updateController.isWorking) {
                            scope.launch {
                                when (updateController.install(update)) {
                                    UpdateInstallResult.DOWNLOAD_FAILED -> showUpdateDownloadError(context)
                                    UpdateInstallResult.INSTALLED,
                                    UpdateInstallResult.NOT_AVAILABLE,
                                    UpdateInstallResult.BUSY -> Unit
                                }
                            }
                        }
                    }
                )
                Spacer(Modifier.height(16.dp))
            }
            HeaderSection(
                s = s,
                hasUpdateBadge = updateController.availableUpdate != null,
                onSettings = { showSettings.value = true }
            )
            Spacer(Modifier.height(18.dp))
            CountdownSection(s)
            Spacer(Modifier.height(16.dp))
            HomeQuickActions(
                onOpenTravel = onOpenTravel,
                onOpenKolymbia = onOpenKolymbia
            )
            Spacer(Modifier.height(16.dp))
            when (Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % 3) {
                0 -> NewsTicker(newsState, onOpenNews)
                1 -> FactCard(s.factOfTheDay)
                else -> HighlightCard(rhodosHighlightOfTheDay())
            }
            Spacer(Modifier.height(12.dp))
            CompassCard(onClick = onOpenCompass)
            Spacer(Modifier.height(12.dp))
            CommunityCard(
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(COMMUNITY_URL)))
                }
            )
            Spacer(Modifier.height(12.dp))
            BottomSection(
                s = s,
                isRefreshing = isRefreshing.value,
                reloadDone = reloadDone.value,
                hasError = weatherError.value,
                onRefresh = {
                    if (!isRefreshing.value) {
                        scope.launch {
                            isRefreshing.value = true
                            val success = fetchAndSaveWeather(context)
                            weatherError.value = !success
                            if (success) RhodosCountdownLargeWidgetProvider.updateAllLargeWidgets(context)
                            state.value = HomeState.load(context)
                            isRefreshing.value = false
                            reloadDone.value = success
                        }
                    }
                }
            )
        }

        // Settings-BottomSheet: Update + Teilen + Version
        if (showSettings.value) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showSettings.value = false },
                sheetState = sheetState,
                containerColor = Color(0xFF111116)
            ) {
                SettingsSheet(
                    isCheckingUpdate = updateController.isWorking,
                    hasUpdate = updateController.availableUpdate != null,
                    onCheckUpdate = {
                        if (!updateController.isWorking) {
                            scope.launch {
                                when (updateController.checkAndInstall()) {
                                    UpdateInstallResult.INSTALLED -> {
                                        showSettings.value = false
                                    }
                                    UpdateInstallResult.DOWNLOAD_FAILED -> showUpdateDownloadError(context)
                                    UpdateInstallResult.NOT_AVAILABLE,
                                    UpdateInstallResult.BUSY -> Unit
                                }
                            }
                        }
                    },
                    onShare = {
                        val sv = state.value
                        val msg = buildString {
                            when {
                                sv.isOnVacation -> { appendLine("🌊 Wir sind auf Rhodos!"); append("Genießt jeden Augenblick.") }
                                sv.isReached -> { appendLine("🛫 Heute geht's los nach Rhodos!"); append("Abflug: ${sv.departureDate} um ${sv.departureTime} Uhr") }
                                else -> {
                                    appendLine("🌊 Noch ${sv.days} Tage bis Rhodos!")
                                    appendLine("Abflug: ${sv.departureDate} um ${sv.departureTime} Uhr")
                                    appendLine()
                                    appendLine("${sv.days} Tage · ${sv.hours.toString().padStart(2,'0')} Std. · ${sv.minutes.toString().padStart(2,'0')} Min.")
                                    appendLine(); append("»${sv.phrase}«")
                                }
                            }
                        }
                        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, msg) }, "Countdown teilen"))
                        showSettings.value = false
                    },
                    onOpenGallery = {
                        showSettings.value = false
                        showGallery.value = true
                    }
                )
            }
        }

        if (showGallery.value) {
            GallerySheet(
                onDismissRequest = { showGallery.value = false },
                onApply = { newImage, newDim ->
                    Images.setPinnedImage(context, newImage)
                    Images.setBackgroundDim(context, newDim)
                    pinnedImage.value = newImage
                    backgroundDim.value = newDim
                    RhodosCountdownLargeWidgetProvider.updateAllLargeWidgets(context)
                    state.value = HomeState.load(context)
                    showGallery.value = false
                },
                currentImageName = Images.rotationImageName(),
                pinnedImageName = pinnedImage.value,
                backgroundDim = backgroundDim.value
            )
        }

        if (updateController.showStartupDialog) {
            updateController.availableUpdate?.let { update ->
                StartupUpdateDialog(
                    update = update,
                    isDownloading = updateController.isWorking,
                    onDismiss = updateController::dismissStartupDialog,
                    onInstall = {
                        if (!updateController.isWorking) {
                            scope.launch {
                                when (updateController.install(update)) {
                                    UpdateInstallResult.INSTALLED -> updateController.dismissStartupDialog()
                                    UpdateInstallResult.DOWNLOAD_FAILED -> showUpdateDownloadError(context)
                                    UpdateInstallResult.NOT_AVAILABLE,
                                    UpdateInstallResult.BUSY -> Unit
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

private fun showUpdateDownloadError(context: android.content.Context) {
    Toast.makeText(
        context,
        "Fehler beim Herunterladen des Updates.",
        Toast.LENGTH_SHORT
    ).show()
}

@Composable
private fun HeaderSection(s: HomeState, hasUpdateBadge: Boolean, onSettings: () -> Unit) {
    Row(verticalAlignment = Alignment.Top) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "RHODOS",
                color = Color.White,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Montserrat,
                letterSpacing = 5.sp
            )
            Text(
                text = "Bis zu unserem Rhodos-Urlaub",
                color = Color(0xE6FFFFFF),
                fontSize = 15.sp,
                fontFamily = Montserrat
            )
            Text(
                text = "${s.departureDate} · ${s.departureTime} Uhr",
                color = Color(0xCCFFFFFF),
                fontSize = 12.sp,
                fontFamily = Montserrat,
                modifier = Modifier.padding(top = 8.dp)
            )
            CountdownProgress(s.progress)
        }
        // Dezentes Gear-Icon oben rechts
        Box(
            modifier = Modifier
                .padding(top = 4.dp, start = 8.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(HomeCardColor)
                .testTag("settings-button")
                .clickable(onClick = onSettings)
                .clearAndSetSemantics {
                    contentDescription = "Einstellungen öffnen"
                    onClick {
                        onSettings()
                        true
                    }
                }
        ) {
            Text(
                text = "⚙",
                fontSize = 19.sp,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
            )
            if (hasUpdateBadge) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(9.dp)
                        .background(Color(0xFFFF5A5F), CircleShape)
                )
            }
        }
    }
}










/**
 * Begrenzt die System-Schriftgrößenskalierung innerhalb des Blocks auf maxScale.
 * Verhindert, dass die Countdown-Zahlen bei "Riesig"-Einstellung den Screen sprengen.
 */






@Composable
private fun BottomSection(
    s: HomeState,
    isRefreshing: Boolean,
    reloadDone: Boolean,
    hasError: Boolean,
    onRefresh: () -> Unit,
) {
    WeatherCard(s, isRefreshing, reloadDone, hasError, onRefresh)
}

private suspend fun fetchAndSaveWeather(context: android.content.Context): Boolean =
    withContext(Dispatchers.IO) {
        val weather = WeatherRepository.fetch() ?: return@withContext false
        WeatherRepository.save(context, weather)
        true
    }



























private fun rhodosHighlightOfTheDay(): String {
    val highlights = listOf(
        "Tsambika-Strand: Feiner roter Sand, ruhiges Wasser — ideal zum Entspannen. Früh morgens ist er fast leer.",
        "Anthony Quinn Bay: Kristallklares Wasser direkt vom Fels — der perfekte Ort zum Schnorcheln.",
        "Lindos: Weiße Häuser, blaue Kuppeln und die Akropolis über dem Meer. Am besten vor 9 Uhr besuchen.",
        "Agathi-Strand: Klein, versteckt und wunderschön — einer der schönsten Strände der Ostküste.",
        "Rhodos Altstadt: UNESCO-Weltkulturerbe — durch mittelalterliche Gassen schlendern und die Atmosphäre genießen.",
        "Pitaroudia probieren: Frittierte Kichererbsenpuffer — die rhodische Spezialität schlechthin.",
        "Schmetterlingstal (Petaloudes): Tausende Jerseyspinner-Falter im Sommer — ein stilles Naturerlebnis.",
        "Motorroller mieten: Die beste Art, die Ostküste auf eigene Faust zu erkunden.",
        "Stegna-Strand: Einheimischenstrand nördlich von Kolymbia, ruhig und unkommerziell.",
        "Prasonissi: Die Südspitze der Insel — hier treffen Mittelmeer und Ägäis aufeinander.",
        "Mandraki-Hafen: Die drei Windmühlen und die Bronzehirsche — für Fotos ein Muss.",
        "Rhodischen Muskatwein trinken: PDO-geschützt, süß und aromatisch — am besten gekühlt.",
        "Tsambika-Kloster: 300 Stufen hoch, aber der Ausblick über die Küste ist atemberaubend.",
        "Bootsausflug nach Symi: Die benachbarte Insel mit bunten Häuserfassaden ist einen Tagesausflug wert.",
        "Palast des Großmeisters: Beeindruckende Johanniterritter-Architektur — das Museum lohnt sich.",
        "Melekouni kosten: Rhodisches Honig-Sesam-Gebäck — traditionell bei Hochzeiten gereicht.",
        "Quad-Tour durch die Insel: Flexibel, abenteuerlich — perfekt für die Ostküste.",
        "Abendspaziergang in der Altstadt: Wenn die Tagestouristen weg sind, erwacht die Magie der Gassen.",
        "Schnorcheln bei Ladiko Bay: Kleiner Naturstrand, klares Wasser, viel zu entdecken.",
        "Sonnenuntergang von der Stadtmauer: Die Mauer der Altstadt bietet spektakuläre Ausblicke.",
        "Granatapfel kaufen: Im September sind sie reif — ein Symbol für Glück auf Rhodos.",
        "Archangelos besuchen: Das Dorf ist bekannt für handgefertigte Lederstiefel.",
        "Faliraki Wasserpark: Für einen erfrischenden Tag mit Rutschen und Pools.",
        "Antike Kamiros: Die besterhaltene antike Stadt Rhodos — kaum Touristen, viel Atmosphäre.",
        "Lokalen Markt erkunden: Frische Feigen, Wassermelonen und Tomaten im September.",
        "Elli-Strand in der Stadt: Lebhafter Stadtstrand mit Bars und Liegestühlen.",
        "Weinverkostung: Rhodos hat eigene Weinbaugebiete — lokale Weingüter bieten Führungen an.",
        "Kolymbia-Eukalyptusallee: Die alte Eukalyptusstraße abends spazieren — kühl und duftend.",
        "Gyros in der Altstadt: Die kleinen Läden in den Seitengassen machen die besten.",
        "Aquarium Rhodos: Das älteste Aquarium Griechenlands — 1935 von den Italienern erbaut.",
        "Frühmorgens schwimmen: Das Meer ist um 7 Uhr noch wie ein Spiegel — unvergesslich.",
        "Dolmathes probieren: Gefüllte Weinblätter mit Reis und Kräutern — rhodische Hausmannskost.",
        "Anthony Quinn Bay bei Sonnenuntergang: Das Licht auf dem Wasser ist zu dieser Zeit magisch.",
        "Bootsverleih ab Kolymbia: Kleine Motorboote mieten und die Küste vom Meer aus erkunden.",
        "Straße der Ritter: Die besterhaltene mittelalterliche Straße Europas — nachts besonders schön.",
        "Jüdisches Viertel (La Juderia): Geschichte und Stille — das kleine Museum ist sehr bewegend.",
        "Souvlaki am Hafen: Nach dem Altstadt-Bummel den Abend am Wasser ausklingen lassen.",
        "Raki/Souma probieren: Rhodischer Traubenschnaps — am besten beim Wirt des Vertrauens.",
        "Nachtbaden: Das warme Mittelmeer im September ist nachts besonders einladend.",
        "Atavyros-Blick: Wer den Aufstieg scheut — schon von weitem ist der Berg ein Blickfang."
    )
    val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
    return highlights[dayOfYear % highlights.size]
}





