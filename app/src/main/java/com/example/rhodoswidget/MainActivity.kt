package com.example.rhodoswidget

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.rhodoswidget.ui.theme.RhodosWidgetTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

private const val COMMUNITY_URL = "https://www.facebook.com/groups/urlaubrhodos"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RhodosWidgetTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    RhodosApp(padding)
                }
            }
        }
    }
}

@Composable
private fun RhodosApp(padding: PaddingValues) {
    val context = LocalContext.current
    val controller = remember(context) { NewsController(context.applicationContext) }
    val showNews = remember { mutableStateOf(false) }
    val selectedArticle = remember { mutableStateOf<NewsArticle?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(controller) { controller.refresh() }

    val article = selectedArticle.value
    BackHandler(enabled = article != null) {
        selectedArticle.value = null
    }
    BackHandler(enabled = article == null && showNews.value) {
        showNews.value = false
    }

    if (article != null) {
        NewsDetailScreen(
            article = article,
            padding = padding,
            onBack = { selectedArticle.value = null }
        )
    } else if (showNews.value) {
        NewsScreen(
            state = controller.state,
            padding = padding,
            onBack = { showNews.value = false },
            onRefresh = { scope.launch { controller.refresh() } },
            onOpenDetail = { selectedArticle.value = it }
        )
    } else {
        RhodosHome(
            padding = padding,
            newsState = controller.state,
            onOpenNews = { showNews.value = true }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RhodosHome(
    padding: PaddingValues,
    newsState: NewsUiState,
    onOpenNews: () -> Unit
) {
    val context = LocalContext.current
    val state = remember { mutableStateOf(HomeState.load(context)) }
    val isRefreshing = remember { mutableStateOf(false) }
    val reloadDone = remember { mutableStateOf(false) }
    val updateController = remember(context) { AppUpdateController(context) }
    val showSettings = remember { mutableStateOf(false) }
    val showGallery = remember { mutableStateOf(false) }
    val pinnedImage = remember { mutableStateOf(Images.getPinnedImage(context)) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(reloadDone.value) {
        if (reloadDone.value) { delay(1_500); reloadDone.value = false }
    }

    LaunchedEffect(updateController) {
        updateController.checkOnStartup()
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
                        listOf(Color(0xCC000000), Color(0x99000000), Color(0xE6000000))
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
            HeaderSection(s, onSettings = { showSettings.value = true })
            Spacer(Modifier.height(36.dp))
            CountdownSection(s)
            Spacer(Modifier.height(20.dp))
            NewsTicker(newsState, onOpenNews)
            Spacer(Modifier.height(12.dp))
            FactCard(s.factOfTheDay)
            Spacer(Modifier.height(10.dp))
            HighlightCard(rhodosHighlightOfTheDay())
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
                hasUpdateBadge = updateController.availableUpdate != null,
                onRefresh = {
                    if (!isRefreshing.value) {
                        scope.launch {
                            isRefreshing.value = true
                            withContext(Dispatchers.IO) {
                                WeatherRepository.fetch()?.let {
                                    WeatherRepository.save(context, it)
                                }
                            }
                            RhodosCountdownLargeWidgetProvider.updateAllLargeWidgets(context)
                            state.value = HomeState.load(context)
                            isRefreshing.value = false
                            reloadDone.value = true
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
                onSelectImage = { newImage ->
                    Images.setPinnedImage(context, newImage)
                    pinnedImage.value = newImage
                    RhodosCountdownLargeWidgetProvider.updateAllLargeWidgets(context)
                    state.value = HomeState.load(context)
                    showGallery.value = false
                },
                currentImageName = Images.currentImageName(context),
                pinnedImageName = pinnedImage.value
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
private fun HeaderSection(s: HomeState, onSettings: () -> Unit) {
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
                text = "Unser Urlaubs-Countdown",
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
                fontSize = 18.sp,
                color = Color(0x80FFFFFF),
                modifier = Modifier
                    .align(Alignment.Center)
            )
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
    hasUpdateBadge: Boolean,
    onRefresh: () -> Unit,
) {
    Column {
        WeatherCard(s, isRefreshing, reloadDone, onRefresh)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = s.weatherStatus,
                color = Color(0xA6FFFFFF),
                fontSize = 11.sp,
                fontFamily = Montserrat,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            if (hasUpdateBadge) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(Color(0xFFFF4444), shape = RoundedCornerShape(4.dp))
                )
            }
        }
    }
}



























private fun rhodosHighlightOfTheDay(): String {
    val highlights = listOf(
        "Tsambika Beach: Feiner roter Sand, ruhiges Wasser — ideal zum Entspannen. Früh morgens ist er fast leer.",
        "Anthony Quinn Bay: Kristallklares Wasser direkt vom Fels — der perfekte Ort zum Schnorcheln.",
        "Lindos: Weiße Häuser, blaue Kuppeln und die Akropolis über dem Meer. Am besten vor 9 Uhr besuchen.",
        "Agathi Beach: Klein, versteckt und wunderschön — einer der schönsten Strände der Ostküste.",
        "Rhodos Altstadt: UNESCO-Weltkulturerbe — durch mittelalterliche Gassen schlendern und die Atmosphäre genießen.",
        "Pitaroudia probieren: Frittierte Kichererbsenpuffer — die rhodische Spezialität schlechthin.",
        "Schmetterlingstal (Petaloudes): Tausende Jerseyspinner-Falter im Sommer — ein stilles Naturerlebnis.",
        "Motorroller mieten: Die beste Art, die Ostküste auf eigene Faust zu erkunden.",
        "Stegna Beach: Einheimischenstrand nördlich von Kolymbia, ruhig und unkommerziell.",
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
        "Elli Beach in der Stadt: Lebhafter Stadtrand-Strand mit Bars und Liegestühlen.",
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





