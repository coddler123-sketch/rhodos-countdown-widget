package com.example.rhodoswidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
    val scope = rememberCoroutineScope()

    // Jede Minute auffrischen, damit der Countdown live wirkt.
    LaunchedEffect(Unit) {
        while (true) {
            state.value = HomeState.load(context)
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
    }
}

@Composable
private fun CountdownSection(s: HomeState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        if (s.isReached) {
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
    onCheckUpdate: () -> Unit
) {
    Column {
        WeatherCard(s)
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryActionButton(
                text = if (isRefreshing) "Lädt ..." else "Wetter neu",
                enabled = !isRefreshing,
                onClick = onRefresh,
                modifier = Modifier.weight(1f)
            )
            SecondaryActionButton(
                text = if (isCheckingUpdate) "Prüft ..." else "Update",
                enabled = !isCheckingUpdate,
                onClick = onCheckUpdate,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${s.weatherStatus} · $updateStatus",
            color = Color(0xBFFFFFFF),
            fontSize = 11.sp,
            fontFamily = Montserrat
        )
    }
}

@Composable
private fun WeatherCard(s: HomeState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x2EFFFFFF))
            .padding(horizontal = 15.dp, vertical = 13.dp)
    ) {
        if (s.weather == null) {
            Text(
                text = "Kolymbia-Wetter noch nicht geladen",
                color = Color(0xE6FFFFFF),
                fontSize = 13.sp,
                fontFamily = Montserrat
            )
        } else {
            Column {
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
                            text = "${s.weather.temperatureLabel} in Kolymbia",
                            color = Color.White,
                            fontSize = 18.sp,
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
                    fontSize = 12.sp,
                    fontFamily = Montserrat
                )
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
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0x33FFFFFF),
                contentColor = Color.White,
                disabledContainerColor = Color(0x22FFFFFF),
                disabledContentColor = Color(0x99FFFFFF)
            )
        ) {
            Text(
                text = text,
                fontFamily = Montserrat,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
}

private data class WeatherSnapshot(
    val temperatureLabel: String,
    val apparentTemperatureLabel: String,
    val humidityLabel: String,
    val precipitationLabel: String,
    val windSpeedLabel: String,
    val iconRes: Int
)

private data class HomeState(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val isReached: Boolean,
    val phrase: String,
    val weather: WeatherSnapshot?,
    val weatherStatus: String,
    val backgroundRes: Int,
    val departureDate: String,
    val departureTime: String
) {
    companion object {
        // 20.09.2026 14:30 — gleich wie im Provider, dort ist der "Source of Truth".
        private const val YEAR = 2026
        private const val MONTH = Calendar.SEPTEMBER
        private const val DAY = 20
        private const val HOUR = 14
        private const val MINUTE = 30

        fun load(context: android.content.Context): HomeState {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.YEAR, YEAR); set(Calendar.MONTH, MONTH)
                set(Calendar.DAY_OF_MONTH, DAY); set(Calendar.HOUR_OF_DAY, HOUR)
                set(Calendar.MINUTE, MINUTE); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val arrivalDayStart = (target.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 0)
            }
            val remaining = (target.timeInMillis - now.timeInMillis).coerceAtLeast(0L)
            val days = TimeUnit.MILLISECONDS.toDays(remaining)
            val afterDays = remaining - TimeUnit.DAYS.toMillis(days)
            val hours = TimeUnit.MILLISECONDS.toHours(afterDays)
            val afterHours = afterDays - TimeUnit.HOURS.toMillis(hours)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(afterHours)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(afterHours - TimeUnit.MINUTES.toMillis(minutes))
            val isReached = now.timeInMillis >= arrivalDayStart.timeInMillis

            val quotes = context.resources.getStringArray(R.array.widget_phrases)
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val deptMidnight = (target.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 0)
            }
            val daysUntilDeparture =
                Math.round((deptMidnight.timeInMillis - today.timeInMillis) / 86_400_000.0).toInt()
            val phraseIndex = (quotes.size - 1 - daysUntilDeparture)
                .coerceIn(0, quotes.size - 1)

            val weather = WeatherRepository.cached(context)?.let {
                WeatherSnapshot(
                    temperatureLabel = it.temperatureLabel,
                    apparentTemperatureLabel = it.apparentTemperatureLabel,
                    humidityLabel = it.humidityLabel,
                    precipitationLabel = it.precipitationLabel,
                    windSpeedLabel = it.windSpeedLabel,
                    iconRes = WeatherRepository.iconFor(it)
                )
            }
            val weatherStatus = weatherStatus(context, weather)
            val backgroundRes = Images.resourceOfTheDay(context)
            val df = SimpleDateFormat("d. MMMM yyyy", Locale.GERMAN)
            val tf = SimpleDateFormat("HH:mm", Locale.GERMAN)
            return HomeState(
                days = days, hours = hours, minutes = minutes, seconds = seconds,
                isReached = isReached,
                phrase = quotes[phraseIndex],
                weather = weather,
                weatherStatus = weatherStatus,
                backgroundRes = backgroundRes,
                departureDate = df.format(target.time),
                departureTime = tf.format(target.time)
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
