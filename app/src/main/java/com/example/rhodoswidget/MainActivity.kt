package com.example.rhodoswidget

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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

@Composable
private fun RhodosHome(padding: PaddingValues) {
    val context = LocalContext.current
    val state = remember { mutableStateOf(HomeState.load(context)) }
    val isRefreshing = remember { mutableStateOf(false) }
    val updateStatus = remember { mutableStateOf("App-Version ${BuildConfig.VERSION_NAME}") }
    val isCheckingUpdate = remember { mutableStateOf(false) }
    val textToSpeech = remember { mutableStateOf<TextToSpeech?>(null) }
    val isSpeechReady = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    DisposableEffect(context) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = engine?.setLanguage(Locale.GERMAN)
                isSpeechReady.value = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
            }
        }
        textToSpeech.value = engine
        onDispose {
            engine.stop()
            engine.shutdown()
            textToSpeech.value = null
            isSpeechReady.value = false
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
                        listOf(Color(0xCC000000), Color(0x99000000), Color(0xE6000000))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HeaderSection(s)
            CountdownSection(s)
            BottomSection(
                s = s,
                isRefreshing = isRefreshing.value,
                updateStatus = updateStatus.value,
                isCheckingUpdate = isCheckingUpdate.value,
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
                        }
                    }
                },
                onSpeakWeather = {
                    val message = state.value.weather?.spokenReport
                        ?: "Das Wetter ist noch nicht geladen. Bitte zuerst aktualisieren."
                    textToSpeech.value?.takeIf { isSpeechReady.value }?.speak(
                        message,
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "rhodos-weather-report"
                    )
                },
                onCheckUpdate = {
                    if (!isCheckingUpdate.value) {
                        scope.launch {
                            isCheckingUpdate.value = true
                            updateStatus.value = "Suche nach Update ..."
                            val update = withContext(Dispatchers.IO) {
                                AppUpdateRepository.checkLatest()
                            }
                            if (update == null) {
                                updateStatus.value = "Kein Update gefunden"
                                isCheckingUpdate.value = false
                            } else {
                                updateStatus.value = "Update ${update.versionName} wird geladen ..."
                                val apk = withContext(Dispatchers.IO) {
                                    AppUpdateRepository.download(context, update)
                                }
                                if (apk == null) {
                                    updateStatus.value = "Update konnte nicht geladen werden"
                                    isCheckingUpdate.value = false
                                } else {
                                    updateStatus.value = "Installer wird geöffnet"
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
                            sv.isOnVacation -> {
                                appendLine("🌊 Wir sind auf Rhodos!")
                                append("Genießt jeden Augenblick.")
                            }
                            sv.isReached -> {
                                appendLine("🛫 Heute geht's los nach Rhodos!")
                                append("Abflug: ${sv.departureDate} um ${sv.departureTime} Uhr")
                            }
                            else -> {
                                appendLine("🌊 Noch ${sv.days} Tage bis Rhodos!")
                                appendLine("Abflug: ${sv.departureDate} um ${sv.departureTime} Uhr")
                                appendLine()
                                appendLine("${sv.days} Tage · ${sv.hours.toString().padStart(2, '0')} Std. · ${sv.minutes.toString().padStart(2, '0')} Min.")
                                appendLine()
                                append("»${sv.phrase}«")
                            }
                        }
                    }
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, msg)
                    }
                    context.startActivity(Intent.createChooser(intent, "Countdown teilen"))
                }
            )
        }
    }
}

@Composable
private fun HeaderSection(s: HomeState) {
    Column {
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
            text = "Abflug am ${s.departureDate} um ${s.departureTime} Uhr",
            color = Color(0xCCFFFFFF),
            fontSize = 12.sp,
            fontFamily = Montserrat,
            modifier = Modifier.padding(top = 8.dp)
        )
        CountdownProgress(s.progress)
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
    updateStatus: String,
    isCheckingUpdate: Boolean,
    onRefresh: () -> Unit,
    onSpeakWeather: () -> Unit,
    onCheckUpdate: () -> Unit,
    onShare: () -> Unit
) {
    Column {
        WeatherCard(s, onSpeakWeather)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${s.weatherStatus} · $updateStatus",
                color = Color(0xA6FFFFFF),
                fontSize = 10.sp,
                fontFamily = Montserrat,
                modifier = Modifier.weight(1f)
            )
            SecondaryActionButton(
                text = if (isRefreshing) "Lädt" else "Wetter",
                enabled = !isRefreshing,
                onClick = onRefresh
            )
            SecondaryActionButton(
                text = if (isCheckingUpdate) "Prüft" else "Update",
                enabled = !isCheckingUpdate,
                onClick = onCheckUpdate
            )
            SecondaryActionButton(
                text = "Teilen",
                enabled = true,
                onClick = onShare
            )
        }
    }
}

@Composable
private fun WeatherCard(s: HomeState, onSpeakWeather: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x26FFFFFF))
            .padding(horizontal = 15.dp, vertical = 12.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.ic_speaker_subtle),
            contentDescription = "Wetterbericht abspielen",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(18.dp)
                .clickable(onClick = onSpeakWeather)
                .padding(1.dp),
            alpha = 0.72f
        )
        if (s.weather == null) {
            Text(
                text = "Kolymbia-Wetter noch nicht geladen",
                color = Color(0xE6FFFFFF),
                fontSize = 13.sp,
                fontFamily = Montserrat,
                modifier = Modifier.padding(end = 28.dp)
            )
        } else {
            Column(modifier = Modifier.padding(end = 28.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                Spacer(modifier = Modifier.height(9.dp))
                Text(
                    text = "Wind ${s.weather.windSpeedLabel} · Luftfeuchte ${s.weather.humidityLabel} · Regen ${s.weather.precipitationLabel}",
                    color = Color(0xCCFFFFFF),
                    fontSize = 11.sp,
                    fontFamily = Montserrat
                )
                if (s.weather.forecastDays.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
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
            }
        }
    }
}

@Composable
private fun SecondaryActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier.height(32.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0x24FFFFFF),
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

private fun shortWeekday(dateIso: String): String {
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateIso) ?: return "?"
    val cal = Calendar.getInstance().apply { time = date }
    return when (cal.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Mo"
        Calendar.TUESDAY -> "Di"
        Calendar.WEDNESDAY -> "Mi"
        Calendar.THURSDAY -> "Do"
        Calendar.FRIDAY -> "Fr"
        Calendar.SATURDAY -> "Sa"
        else -> "So"
    }
}

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
    val progress: Float
) {
    companion object {
        private const val YEAR = 2026
        private const val MONTH = Calendar.SEPTEMBER
        private const val DAY = 20
        private const val HOUR = 14
        private const val MINUTE = 30

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
            val df = SimpleDateFormat("d. MMMM yyyy", Locale.GERMAN)
            val tf = SimpleDateFormat("HH:mm", Locale.GERMAN)
            val target = Calendar.getInstance().apply {
                set(Calendar.YEAR, YEAR); set(Calendar.MONTH, MONTH)
                set(Calendar.DAY_OF_MONTH, DAY); set(Calendar.HOUR_OF_DAY, HOUR)
                set(Calendar.MINUTE, MINUTE); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
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
                progress = CountdownCalculator.progressFraction()
            )
        }

        private fun weatherStatus(context: android.content.Context, weather: WeatherSnapshot?): String {
            if (weather == null) return "Noch nicht geladen"
            val lastFetch = WeatherRepository.lastFetchMillis(context)
            if (lastFetch <= 0L) return "Gespeichert"

            val ageMillis = (System.currentTimeMillis() - lastFetch).coerceAtLeast(0L)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(ageMillis)
            if (minutes < 1) return "Gerade aktualisiert"
            if (minutes < 60) return "Vor $minutes Min."

            val hours = TimeUnit.MILLISECONDS.toHours(ageMillis)
            return "Vor $hours Std."
        }
    }
}
