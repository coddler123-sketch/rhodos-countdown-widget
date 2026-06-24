package com.example.rhodoswidget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
fun WeatherCard(s: HomeState, isRefreshing: Boolean, reloadDone: Boolean, onRefresh: () -> Unit) {
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
