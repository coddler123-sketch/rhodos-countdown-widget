package com.example.rhodoswidget

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import kotlin.math.min
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rhodoswidget.ui.theme.RhodosWidgetTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

private val Montserrat = FontFamily(
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RhodosWidgetTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    RhodosHome(padding)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RhodosHome(padding: PaddingValues) {
    val context = LocalContext.current
    val state = remember { mutableStateOf(HomeState.load(context)) }
    val isRefreshing = remember { mutableStateOf(false) }
    val reloadDone = remember { mutableStateOf(false) }
    val isCheckingUpdate = remember { mutableStateOf(false) }
    val updateAvailable = remember { mutableStateOf<AppUpdate?>(null) }
    val showSettings = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(reloadDone.value) {
        if (reloadDone.value) { delay(1_500); reloadDone.value = false }
    }

    LaunchedEffect(Unit) {
        // Stiller Update-Check beim Start
        val found = withContext(Dispatchers.IO) { AppUpdateRepository.checkLatest() }
        if (found != null) updateAvailable.value = found
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
            HeaderSection(s, onSettings = { showSettings.value = true })
            Spacer(Modifier.height(36.dp))
            CountdownSection(s)
            Spacer(Modifier.height(20.dp))
            FactCard(s.factOfTheDay)
            Spacer(Modifier.height(10.dp))
            HighlightCard(rhodosHighlightOfTheDay())
            Spacer(Modifier.height(12.dp))
            BottomSection(
                s = s,
                isRefreshing = isRefreshing.value,
                reloadDone = reloadDone.value,
                hasUpdateBadge = updateAvailable.value != null,
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
                    isCheckingUpdate = isCheckingUpdate.value,
                    hasUpdate = updateAvailable.value != null,
                    onCheckUpdate = {
                        if (!isCheckingUpdate.value) {
                            scope.launch {
                                isCheckingUpdate.value = true
                                val update = updateAvailable.value
                                    ?: withContext(Dispatchers.IO) { AppUpdateRepository.checkLatest() }
                                if (update == null) {
                                    isCheckingUpdate.value = false
                                } else {
                                    updateAvailable.value = update
                                    val apk = withContext(Dispatchers.IO) {
                                        AppUpdateRepository.download(context, update)
                                    }
                                    if (apk == null) {
                                        isCheckingUpdate.value = false
                                    } else {
                                        showSettings.value = false
                                        context.startActivity(AppUpdateRepository.installIntent(context, apk))
                                        isCheckingUpdate.value = false
                                    }
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
                    }
                )
            }
        }
    }
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
        Text(
            text = "⚙",
            fontSize = 18.sp,
            color = Color(0x80FFFFFF),
            modifier = Modifier
                .padding(top = 4.dp, start = 8.dp)
                .clickable(onClick = onSettings)
        )
    }
}

@Composable
private fun CountdownProgress(fraction: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .height(3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0x33FFFFFF))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xCCFFFFFF))
        )
    }
}

@Composable
private fun CountdownSection(s: HomeState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        if (s.isOnVacation) {
            Text(
                text = "Ihr seid auf Rhodos. 🌊",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Montserrat
            )
            Text(
                text = "Genießt jeden Augenblick.",
                color = Color(0xE6FFFFFF),
                fontSize = 18.sp,
                fontFamily = Montserrat,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else if (s.isReached) {
            Text(
                text = "Es ist soweit!",
                color = Color.White,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Montserrat
            )
        } else {
            // Countdown-Zahlen bei großer System-Schrift cappen, damit die 4 Blöcke
            // nebeneinander passen. Andere Texte (Header, Phrase, Wetter) skalieren weiter.
            CappedFontScale(maxScale = 1.15f) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CountdownBlock(s.days.toString(), "TAGE")
                    CountdownDivider()
                    CountdownBlock(s.hours.toString().padStart(2, '0'), "STD.")
                    CountdownDivider()
                    CountdownBlock(s.minutes.toString().padStart(2, '0'), "MIN.")
                    CountdownDivider()
                    CountdownBlock(s.seconds.toString().padStart(2, '0'), "SEK.")
                }
            }
        }
        if (!s.isOnVacation) {
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "„${s.phrase}“",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = Montserrat
            )
        }
    }
}

@Composable
private fun CountdownBlock(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = Color.White,
            fontSize = 46.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Montserrat
        )
        Text(
            text = label,
            color = Color(0xE6FFFFFF),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Montserrat,
            letterSpacing = 1.sp
        )
    }
}

/**
 * Begrenzt die System-Schriftgrößenskalierung innerhalb des Blocks auf maxScale.
 * Verhindert, dass die Countdown-Zahlen bei "Riesig"-Einstellung den Screen sprengen.
 */
@Composable
private fun CappedFontScale(maxScale: Float, content: @Composable () -> Unit) {
    val current = LocalDensity.current
    val capped = remember(current.density, current.fontScale, maxScale) {
        Density(
            density = current.density,
            fontScale = min(current.fontScale, maxScale)
        )
    }
    CompositionLocalProvider(LocalDensity provides capped, content = content)
}

@Composable
private fun CountdownDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 14.dp)
            .width(1.dp)
            .height(46.dp)
            .background(Color(0x66FFFFFF))
    )
}

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
                fontSize = 10.sp,
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

@Composable
private fun WeatherCard(s: HomeState, isRefreshing: Boolean, reloadDone: Boolean, onRefresh: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x26FFFFFF))
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        if (reloadDone) {
            Text(
                text = "✓",
                color = Color(0xFF66DD88),
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.TopEnd).padding(1.dp)
            )
        } else {
            Image(
                painter = painterResource(R.drawable.ic_refresh_subtle),
                contentDescription = "Wetter aktualisieren",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(17.dp)
                    .clickable(enabled = !isRefreshing, onClick = onRefresh)
                    .padding(1.dp),
                alpha = if (isRefreshing) 0.25f else 0.55f
            )
        }
        if (s.weather == null) {
            Text(
                text = "Kolymbia-Wetter noch nicht geladen",
                color = Color(0xE6FFFFFF),
                fontSize = 13.sp,
                fontFamily = Montserrat,
                modifier = Modifier.padding(end = 28.dp)
            )
        } else {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 28.dp)
                ) {
                    Image(
                        painter = painterResource(s.weather.iconRes),
                        contentDescription = null,
                        modifier = Modifier
                            .height(26.dp)
                            .width(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Kolymbia jetzt",
                            color = Color(0xBFFFFFFF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = Montserrat
                        )
                        Text(
                            text = s.weather.temperatureLabel,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = Montserrat
                        )
                        Text(
                            text = s.weather.apparentTemperatureLabel,
                            color = Color(0xD9FFFFFF),
                            fontSize = 12.sp,
                            fontFamily = Montserrat
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Wind ${s.weather.windSpeedLabel} · Luftfeuchte ${s.weather.humidityLabel} · Regen ${s.weather.precipitationLabel}",
                    color = Color(0xCCFFFFFF),
                    fontSize = 11.sp,
                    fontFamily = Montserrat,
                    modifier = Modifier.padding(end = 28.dp)
                )
                if (s.weather.forecastDays.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(7.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        s.weather.forecastDays.forEach { day ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = day.weekday,
                                    color = Color(0xBFFFFFFF),
                                    fontSize = 10.sp,
                                    fontFamily = Montserrat
                                )
                                Image(
                                    painter = painterResource(day.iconRes),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(vertical = 2.dp)
                                )
                                Text(
                                    text = "${day.maxTemp}°",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = Montserrat
                                )
                                Text(
                                    text = "${day.minTemp}°",
                                    color = Color(0xBFFFFFFF),
                                    fontSize = 10.sp,
                                    fontFamily = Montserrat
                                )
                            }
                        }
                    }
                }
                if (s.sunriseLabel != null || s.sunsetLabel != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (s.sunriseLabel != null) {
                        Text(
                            text = "🌄  Sonnenaufgang: ${s.sunriseLabel}",
                            color = Color(0xCCFFFFFF),
                            fontSize = 11.sp,
                            fontFamily = Montserrat
                        )
                    }
                    if (s.sunsetLabel != null) {
                        Text(
                            text = "🌅  Sonnenuntergang: ${s.sunsetLabel}",
                            color = Color(0xCCFFFFFF),
                            fontSize = 11.sp,
                            fontFamily = Montserrat,
                            modifier = Modifier.padding(top = if (s.sunriseLabel != null) 2.dp else 0.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FactCard(fact: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x1AFFFFFF))
            .padding(horizontal = 15.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "🏛",
            fontSize = 16.sp,
            modifier = Modifier.padding(end = 10.dp, top = 1.dp)
        )
        Column {
            Text(
                text = "Rhodos-Fakt des Tages",
                color = Color(0xBFFFFFFF),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Montserrat
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = fact,
                color = Color.White,
                fontSize = 13.sp,
                fontFamily = Montserrat
            )
        }
    }
}

@Composable
private fun SettingsSheet(
    isCheckingUpdate: Boolean,
    hasUpdate: Boolean,
    onCheckUpdate: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Optionen",
            color = Color(0x99FFFFFF),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Montserrat,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        SettingsRow(
            icon = if (hasUpdate) "🔴" else "✓",
            label = "Version v${BuildConfig.VERSION_NAME}",
            detail = if (hasUpdate) "Update verfügbar" else "Aktuellste Version",
            actionLabel = when {
                isCheckingUpdate -> "Lädt …"
                hasUpdate -> "Installieren"
                else -> "Prüfen"
            },
            onAction = onCheckUpdate,
            enabled = !isCheckingUpdate
        )
        SettingsRow(
            icon = "↗",
            label = "Countdown teilen",
            detail = "Via WhatsApp, SMS, …",
            actionLabel = "Teilen",
            onAction = onShare
        )
    }
}

@Composable
private fun SettingsRow(
    icon: String,
    label: String,
    detail: String,
    actionLabel: String,
    onAction: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 16.sp, modifier = Modifier.width(28.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, color = Color.White, fontSize = 14.sp, fontFamily = Montserrat, fontWeight = FontWeight.SemiBold)
            Text(text = detail, color = Color(0x99FFFFFF), fontSize = 11.sp, fontFamily = Montserrat)
        }
        SecondaryActionButton(text = actionLabel, enabled = enabled, onClick = onAction)
    }
}

@Composable
private fun SecondaryActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0x24FFFFFF)
) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.height(32.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = Color.White,
                disabledContainerColor = Color(0x18FFFFFF),
                disabledContentColor = Color(0x99FFFFFF)
            )
        ) {
            Text(
                text = text,
                fontFamily = Montserrat,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
        }
}

@Composable
private fun UpdateButton(isChecking: Boolean, hasUpdate: Boolean, onClick: () -> Unit) {
    Box {
        SecondaryActionButton(
            text = when {
                isChecking -> "Prüft"
                hasUpdate -> "Update!"
                else -> "Update"
            },
            enabled = !isChecking,
            onClick = onClick,
            containerColor = if (hasUpdate) Color(0x55FF6B35) else Color(0x24FFFFFF)
        )
        if (hasUpdate && !isChecking) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 3.dp, y = (-3).dp)
                    .background(Color(0xFFFF4444), shape = RoundedCornerShape(4.dp))
            )
        }
    }
}

private fun shortWeekday(dateIso: String): String = dateIso.toShortGermanWeekday()

private data class ForecastEntry(val weekday: String, val iconRes: Int, val minTemp: Int, val maxTemp: Int)

private data class WeatherSnapshot(
    val temperatureLabel: String,
    val apparentTemperatureLabel: String,
    val humidityLabel: String,
    val precipitationLabel: String,
    val windSpeedLabel: String,
    val iconRes: Int,
    val forecastDays: List<ForecastEntry>,
    val spokenReport: String
)

private data class HomeState(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val isReached: Boolean,
    val isOnVacation: Boolean,
    val phrase: String,
    val weather: WeatherSnapshot?,
    val weatherStatus: String,
    val backgroundRes: Int,
    val departureDate: String,
    val departureTime: String,
    val progress: Float,
    val sunsetLabel: String?,
    val sunriseLabel: String?,
    val factOfTheDay: String
) {
    companion object {
        private val YEAR = CountdownCalculator.DEPARTURE_YEAR
        private val MONTH = CountdownCalculator.DEPARTURE_MONTH
        private val DAY = CountdownCalculator.DEPARTURE_DAY
        private val HOUR = CountdownCalculator.DEPARTURE_HOUR
        private val MINUTE = CountdownCalculator.DEPARTURE_MINUTE

        fun load(context: android.content.Context): HomeState {
            val remaining = CountdownCalculator.calculate()

            val quotes = context.resources.getStringArray(R.array.widget_phrases)
            val phraseIndex = (quotes.size - 1 - CountdownCalculator.daysUntilDeparture())
                .coerceIn(0, quotes.size - 1)

            val weather = WeatherRepository.cached(context)?.let { w ->
                WeatherSnapshot(
                    temperatureLabel = w.temperatureLabel,
                    apparentTemperatureLabel = w.apparentTemperatureLabel,
                    humidityLabel = w.humidityLabel,
                    precipitationLabel = w.precipitationLabel,
                    windSpeedLabel = w.windSpeedLabel,
                    iconRes = WeatherRepository.iconFor(w),
                    forecastDays = w.forecast.take(7).map { day ->
                        ForecastEntry(
                            weekday = shortWeekday(day.dateIso),
                            iconRes = WeatherRepository.iconForCode(day.weatherCode),
                            minTemp = day.minTemperatureCelsius,
                            maxTemp = day.maxTemperatureCelsius
                        )
                    },
                    spokenReport = WeatherReportFormatter.spokenReport(w)
                )
            }
            val weatherStatus = weatherStatus(context, weather)
            val backgroundRes = Images.resourceOfTheDay(context)
            val df = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
            val tf = SimpleDateFormat("HH:mm", Locale.GERMAN)
            val target = Calendar.getInstance().apply {
                set(Calendar.YEAR, YEAR); set(Calendar.MONTH, MONTH)
                set(Calendar.DAY_OF_MONTH, DAY); set(Calendar.HOUR_OF_DAY, HOUR)
                set(Calendar.MINUTE, MINUTE); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val rawWeather = WeatherRepository.cached(context)
            val sunsetLabel = rawWeather?.sunsetIso?.let { parseSolarEventLabel(it) }
            val sunriseLabel = rawWeather?.sunriseIso?.let { parseSolarEventLabel(it) }
            return HomeState(
                days = remaining.days, hours = remaining.hours,
                minutes = remaining.minutes, seconds = remaining.seconds,
                isReached = remaining.isReached,
                isOnVacation = remaining.isOnVacation,
                phrase = quotes[phraseIndex],
                weather = weather,
                weatherStatus = weatherStatus,
                backgroundRes = backgroundRes,
                departureDate = df.format(target.time),
                departureTime = tf.format(target.time),
                progress = CountdownCalculator.progressFraction(),
                sunsetLabel = sunsetLabel,
                sunriseLabel = sunriseLabel,
                factOfTheDay = rhodosFactOfTheDay()
            )
        }

        private fun parseSolarEventLabel(sunsetIso: String): String? {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US).apply {
                    timeZone = java.util.TimeZone.getTimeZone("Europe/Athens")
                }
                val sunsetTime = sdf.parse(sunsetIso) ?: return null
                val now = System.currentTimeMillis()
                val timeLabel = SimpleDateFormat("HH:mm", Locale.GERMAN).apply {
                    timeZone = java.util.TimeZone.getTimeZone("Europe/Athens")
                }.format(sunsetTime)
                val diffMillis = sunsetTime.time - now
                if (diffMillis > 0) {
                    val h = TimeUnit.MILLISECONDS.toHours(diffMillis)
                    val m = TimeUnit.MILLISECONDS.toMinutes(diffMillis) % 60
                    if (h > 0) "$timeLabel Uhr · noch ${h} Std. ${m} Min."
                    else "$timeLabel Uhr · noch ${m} Min."
                } else {
                    "$timeLabel Uhr"
                }
            } catch (e: Exception) { null }
        }

        private fun weatherStatus(context: android.content.Context, weather: WeatherSnapshot?): String {
            if (weather == null) return "Noch nicht geladen"
            val lastFetch = WeatherRepository.lastFetchMillis(context)
            if (lastFetch <= 0L) return "Gespeichert"

            val ageMillis = (System.currentTimeMillis() - lastFetch).coerceAtLeast(0L)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(ageMillis)
            if (minutes < 1) return "Gerade aktualisiert"
            if (minutes < 60) return "Vor $minutes Min. aktualisiert"

            val hours = TimeUnit.MILLISECONDS.toHours(ageMillis)
            return "Vor $hours Std. aktualisiert"
        }
    }
}

@Composable
private fun HighlightCard(highlight: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x1AFFFFFF))
            .padding(horizontal = 15.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = "📍", fontSize = 16.sp, modifier = Modifier.padding(end = 10.dp, top = 1.dp))
        Column {
            Text(
                text = "Highlight des Tages",
                color = Color(0xBFFFFFFF),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Montserrat
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = highlight,
                color = Color.White,
                fontSize = 13.sp,
                fontFamily = Montserrat
            )
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

private fun rhodosFactOfTheDay(): String {
    val facts = listOf(
        // Geografie & Natur
        "Rhodos ist mit 1.401 km² die viertgrößte Insel Griechenlands – nach Kreta, Euböa und Lesbos.",
        "Der höchste Berg der Insel ist der Attavyros mit 1.215 m – bei klarem Wetter sieht man von dort die türkische Küste.",
        "Rhodos liegt nur 18 km von der türkischen Küste entfernt – näher als Mallorca an Spanien.",
        "Die Insel hat rund 300 Sonnentage im Jahr – damit gehört sie zu den sonnigsten Orten Europas.",
        "Rhodos gehört zur Dodekanes-Gruppe, dem Zwölf-Inseln-Archipel in der südlichen Ägäis.",
        "Im September hat das Meer rund um Rhodos noch angenehme 25–27 °C Wassertemperatur.",
        "Auf der Insel wachsen über 50 endemische Pflanzenarten, die nirgendwo sonst auf der Welt vorkommen.",
        "Das Schmetterlingstal (Petaloudes) ist im Sommer Heimat Tausender Jerseyspinner-Falter.",
        "Auf Rhodos sind Damhirsche heimisch – die berühmten Bronzehirsche im Hafen von Mandraki erinnern daran.",
        "Die Insel ist ein wichtiges Brutgebiet der Meeresschildkröte Caretta caretta.",
        "Die Küstenlinie von Rhodos ist rund 220 km lang – mit über 40 verschiedenen Stränden.",
        "Prasonissi im Süden ist ein weltbekannter Windsurfspot: dort treffen Mittelmeer und Ägäis aufeinander.",
        "Der rhodische Meltemi-Wind weht im Sommer aus Nordwest und sorgt für angenehme Kühle.",
        "Im September ist Rhodos besonders schön – weniger Touristen, noch voll Sonne, erstes Herbstlicht.",
        "Rhodos produziert eigenen Honig mit einzigartigem Aroma – dank des lokalen wilden Thymians.",
        // Kolymbia
        "Kolymbia liegt an der Ostküste, etwa 26 km südlich der Inselhauptstadt.",
        "Die Eukalyptusbäume entlang der Hauptstraße in Kolymbia wurden in den 1950er Jahren zur Trockenlegung von Sümpfen gepflanzt.",
        "Kolymbia ist bekannt für seinen ruhigen, breiten Sandstrand – ideal zum Entspannen.",
        "Der Stegna-Strand nördlich von Kolymbia ist besonders bei Einheimischen beliebt.",
        "Agathi-Strand, wenige Kilometer südlich, gilt als einer der schönsten Strände der Ostküste.",
        "Das Tsambika-Kloster thront auf einem 300 m hohen Felsen bei Kolymbia – der Ausblick ist atemberaubend.",
        "Frauen, die im Tsambika-Kloster beten, nennen ihr Kind traditionell Tsambikos oder Tsambika.",
        "In der Nähe von Kolymbia liegt das Dorf Archangelos – bekannt für handgefertigte Lederstiefel.",
        "An der Ostküste bei Kolymbia weht im Sommer oft eine kühle Seebrise – erfrischender als an der Westküste.",
        // Antike Geschichte
        "Die Insel war in der Antike berühmt für ihre Rosen – daher der Name (griech. rhodon = Rose).",
        "Die Städte Kamiros, Ialyssos und Lindos gründeten 408 v. Chr. gemeinsam die neue Hauptstadt Rhodos.",
        "Der neue Stadtplan von Rhodos folgte dem Rastersystem des Hippodamos von Milet – ein Vorläufer moderner Stadtplanung.",
        "Rhodos war im 3. Jahrhundert v. Chr. eines der bedeutendsten Handelszentren des gesamten Mittelmeers.",
        "Die Rhodier entwickelten das älteste bekannte Seehandelsrecht der Welt – das rhodische Seerecht.",
        "Das rhodische Seerecht beeinflusst bis heute das internationale Schifffahrtsrecht.",
        "Cicero und Julius Cäsar studierten auf Rhodos Rhetorik – die rhodische Schule war berühmt.",
        "Julius Cäsar wurde auf dem Weg nach Rhodos von Piraten entführt und auf einer kleinen Insel festgehalten.",
        "Die rhodische Bildhauerschule schuf Meisterwerke der Antike – darunter vermutlich die Laokoon-Gruppe.",
        "Die Laokoon-Gruppe, heute in den Vatikanischen Museen, wurde wahrscheinlich von Rhodiern erschaffen.",
        "Diagoras von Rhodos war einer der berühmtesten Sportler der Antike und wurde von Pindar in einer Ode gefeiert.",
        "Auf dem Gipfel des Attavyros stand einst ein Zeus-Tempel – von dem noch Ruinen sichtbar sind.",
        "Das antike Kamiros ist eine der besterhaltenen antiken Städte Griechenlands – ohne spätere Überbauung.",
        "In der Antike schickten Städte aus ganz Griechenland Weihgeschenke zum Tempel auf Rhodos.",
        // Koloss von Rhodos
        "Der Koloss von Rhodos war eine riesige Bronzestatue des Sonnengottes Helios – über 30 m hoch.",
        "Der Koloss wurde um 280 v. Chr. fertiggestellt und galt als eines der Sieben Weltwunder der Antike.",
        "Ein Erdbeben 226 v. Chr. warf den Koloss um – er lag über 800 Jahre als Trümmerhaufen.",
        "Der genaue Standort des Kolosses ist bis heute unbekannt – viele vermuten ihn am Eingang des Hafens.",
        "Die Araber transportierten die Trümmer des Kolosses im 7. Jahrhundert angeblich auf 900 Kamelen ab.",
        "Moderne Pläne, eine Nachbildung des Kolosses im Hafen zu errichten, werden seit Jahren diskutiert.",
        // Mittelalter & Ritter
        "Der Johanniterorden eroberte Rhodos 1309 und machte die Insel zu seinem Hauptsitz.",
        "Die Ritter bauten die Befestigungsanlagen von Rhodos zu einer der stärksten Festungen Europas aus.",
        "Die Stadtmauer der Altstadt ist bis zu 4 km lang und an manchen Stellen 12 m dick.",
        "Die Straße der Ritter (Ippoton) gilt als besterhaltene mittelalterliche Straße Europas.",
        "Der Palast des Großmeisters wurde nach einem Pulvermagazin-Explosion 1856 schwer beschädigt und später restauriert.",
        "Die Ritter teilten die Altstadt in sogenannte Zungen auf – Landsmannschaften aus verschiedenen europäischen Ländern.",
        "Die Osmanen unter Süleyman dem Prächtigen eroberten Rhodos 1522 nach sechsmonatiger Belagerung.",
        "Die Johanniterritter zogen nach der Niederlage nach Malta weiter – und wurden dort zu den Maltesern.",
        "Die osmanische Süleymaniye-Moschee in der Altstadt wurde direkt nach der Eroberung 1522 erbaut.",
        "Das D'Amboise-Tor ist das eindrucksvollste der sieben Stadttore der mittelalterlichen Altstadt.",
        // UNESCO & Kulturerbe
        "Die Altstadt von Rhodos ist seit 1988 UNESCO-Weltkulturerbe – wegen ihrer einzigartigen mittelalterlichen Substanz.",
        "In der Altstadt leben noch heute etwa 6.000 Menschen – sie ist keine reine Touristenkulisse.",
        "Die Altstadt vereint griechische, osmanische, jüdische und westeuropäische Architektur auf engstem Raum.",
        "Das jüdische Viertel (La Juderia) in der Altstadt ist eines der ältesten jüdischen Quartiere der Welt.",
        "Die jüdische Gemeinde auf Rhodos wurde 1944 deportiert – von ursprünglich 1.700 Menschen überlebten nur wenige.",
        "Das Jüdische Museum in der Altstadt ist eines der wenigen Museen seiner Art in Griechenland.",
        // Moderne Geschichte
        "Die Dodekanes-Inseln waren von 1912 bis 1943 unter italienischer Verwaltung.",
        "Die Italiener hinterließen auf Rhodos bemerkenswerte Architektur aus den 1930er Jahren – teils im Faschismus-Stil.",
        "Das Aquarium im Norden der Insel wurde 1935 von den Italienern erbaut und ist heute noch in Betrieb.",
        "Die Dodekanes wurden erst 1947 offiziell Teil Griechenlands.",
        "Im Zweiten Weltkrieg war Rhodos ab 1943 von deutschen Truppen besetzt.",
        "Anthony Quinn kaufte nach dem Dreh von 'Die Kanonen von Navarone' (1961) Land auf Rhodos.",
        "Die Bucht, in der Anthony Quinn lebte und schwamm, trägt heute seinen Namen: Anthony Quinn Bay.",
        // Kulinarik & Kultur
        "\"Pitaroudia\" sind frittierte Kichererbsenpuffer – eine rhodische Spezialität, die man unbedingt probieren sollte.",
        "\"Melekouni\" ist ein rhodisches Süßgebäck aus Honig und Sesam – traditionell bei Hochzeiten gereicht.",
        "Der rhodische Muskatwein (Muscat of Rhodes) hat ein EU-Herkunftsschutzsiegel (PDO).",
        "Rhodos ist seit der Antike ein Weinanbaugebiet – die Reben wachsen auf vulkanischem Boden.",
        "Rhodische Keramik ist bunt und für ihre Granatapfelmotive bekannt – ein Symbol für Glück.",
        "Der Granatapfel gilt auf Rhodos als Glücksbringer und hängt in vielen Häusern über der Tür.",
        "In Lindos dürfen keine Autos fahren – die Altstadt ist nur zu Fuß oder per Esel erreichbar.",
        "Die Akropolis von Lindos thront auf einem 116 m hohen Felsen direkt über dem strahlend blauen Meer.",
        "Lindos war in der Antike eine bedeutende Handelsstadt mit eigenem Hafen und weitreichenden Verbindungen.",
        // Natur & Tiere
        "Auf Rhodos wurden über 150 Vogelarten beobachtet – die Insel liegt auf wichtigen Zugvogelrouten.",
        "Im Frühjahr blüht Rhodos in einem Meer aus Wildblumen – Mohn, Asphodel und Orchideen.",
        "Die Insel hat mehrere natürliche Quellen, deren Wasser seit der Antike als heilkräftig gilt.",
        "Das Wasser rund um Rhodos ist so klar, dass man bei ruhiger See bis auf 30–40 m Tiefe sehen kann.",
        "Vor der Küste von Rhodos liegen mehrere antike Schiffswracks – ein Paradies für Taucher.",
        "Im September sind die Feigenbäume auf Rhodos voll mit reifen Früchten – ein unvergleichlicher Genuss.",
        // Zahlen & Fakten
        "Rhodos hat rund 115.000 Einwohner – die meisten davon in der Inselhauptstadt.",
        "Über 2 Millionen Touristen besuchen Rhodos jährlich – fast 20 Mal mehr als Einwohner.",
        "Der Flughafen \"Diagoras\" liegt 14 km südwestlich der Stadt und ist einer der verkehrsreichsten Griechenlands.",
        "Rhodos hat eine eigene Tageszeitung: \"Rhodiaki\" – sie erscheint seit Jahrzehnten.",
        "Auf der Insel gibt es über 300 Kirchen und Kapellen – viele davon winzig und versteckt.",
        "Die Insel hat 11 Gemeinden und über 40 bewohnte Ortschaften.",
        // Besonderes & Kurioses
        "Der Platane im Hafen von Kos soll unter Hippokrates gestanden haben – Rhodos und Kos teilten viel medizinisches Wissen.",
        "Rhodos galt in der Antike als so reich, dass man sagte: \"Selbst die Götter möchten auf Rhodos leben.\"",
        "Die rhodische Marine war in der Antike für ihre Schnelligkeit berühmt – ihre Galeeren galten als unbesiegbar.",
        "Im Mittelalter war Rhodos ein wichtiger Zwischenstopp für Pilger auf dem Weg ins Heilige Land.",
        "Die Windmühlen im Hafen von Mandraki wurden im 15. Jahrhundert gebaut – drei stehen noch heute.",
        "Rhodos ist die Geburtsstadt des griechischen Schriftstellers und Nobelpreisträgers Odysseas Elytis – nein, er stammte aus Kreta, aber Rhodos inspirierte viele seiner Verse.",
        "Das Meer um Rhodos wechselt je nach Tageszeit die Farbe – von tiefem Blau bis smaragdgrün.",
        "Auf Rhodos gibt es eine besondere Lichtstimmung im Spätsommer: golden, warm, fast unwirklich schön.",
        "Im September kühlen die Nächte auf Rhodos leicht ab – perfekt zum Schlafen mit offenen Fenstern.",
        "Ein Urlaub auf Rhodos fühlt sich länger an als er ist – weil jeder Tag vollgepackt mit Eindrücken ist."
    )
    val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
    return facts[dayOfYear % facts.size]
}
