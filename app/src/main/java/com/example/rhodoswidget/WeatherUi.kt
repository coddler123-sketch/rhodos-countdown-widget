package com.example.rhodoswidget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ForecastEntry(val weekday: String, val iconRes: Int, val minTemp: Int, val maxTemp: Int)

data class WeatherSnapshot(
    val temperatureLabel: String,
    val apparentTemperatureLabel: String,
    val humidityLabel: String,
    val precipitationLabel: String,
    val windSpeedLabel: String,
    val iconRes: Int,
    val forecastDays: List<ForecastEntry>,
    val spokenReport: String
)

@Composable
fun WeatherCard(
    s: HomeState,
    isRefreshing: Boolean,
    reloadDone: Boolean,
    hasError: Boolean,
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HomeCardShape)
            .background(HomeCardColor)
            .border(1.dp, HomeCardBorder, HomeCardShape)
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        if (s.weather != null) {
            if (reloadDone) {
                Text(
                    text = "✓",
                    color = Color(0xFF66DD88),
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.TopEnd).padding(1.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(48.dp)
                        .clickable(enabled = !isRefreshing, onClick = onRefresh)
                        .clearAndSetSemantics {
                            contentDescription = "Wetter aktualisieren"
                            if (isRefreshing) {
                                disabled()
                            } else {
                                onClick {
                                    onRefresh()
                                    true
                                }
                            }
                        }
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_refresh_subtle),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(17.dp)
                            .padding(1.dp),
                        alpha = if (isRefreshing) 0.25f else 0.55f
                    )
                }
            }
        }
        if (s.weather == null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "KOLYMBIA-WETTER",
                        color = HomeAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Montserrat,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = when {
                            isRefreshing -> "Wetter wird geladen"
                            hasError -> "Wetter konnte nicht geladen werden"
                            else -> "Noch keine Wetterdaten"
                        },
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Montserrat
                    )
                }
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .width(104.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(HomeAccent.copy(alpha = 0.16f))
                        .clickable(enabled = !isRefreshing, onClick = onRefresh)
                        .clearAndSetSemantics {
                            contentDescription = "Wetter jetzt laden"
                            if (isRefreshing) disabled() else onClick { onRefresh(); true }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            isRefreshing -> "Lädt …"
                            hasError -> "Erneut"
                            else -> "Jetzt laden"
                        },
                        color = HomeAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Montserrat
                    )
                }
            }
        } else {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 48.dp)
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
                            fontSize = 11.sp,
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
                    modifier = Modifier.padding(end = 48.dp)
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
                                    fontSize = 11.sp,
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
                                    fontSize = 11.sp,
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
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = s.weatherStatus,
                    color = Color(0xA6FFFFFF),
                    fontSize = 10.sp,
                    fontFamily = Montserrat,
                    maxLines = 1
                )
            }
        }
    }
}
