package com.example.rhodoswidget

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun MarineWeatherCard(
    marineWeather: MarineWeather?,
    weather: RhodosWeather?,
    isLoading: Boolean,
    refreshFailed: Boolean,
    onRefresh: () -> Unit
) {
    TravelCardContainer {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.travel_marine_title),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Montserrat
            )
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onRefresh, enabled = !isLoading) {
                Text(stringResource(R.string.travel_marine_refresh), color = HomeAccent)
            }
        }
        when {
            marineWeather == null && isLoading -> MarineStatusText(R.string.travel_marine_loading)
            marineWeather == null -> MarineStatusText(R.string.travel_marine_unavailable)
            else -> {
                MarineMetrics(marineWeather, weather)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        if (isMarineCaution(weather, marineWeather)) R.string.travel_marine_caution
                        else R.string.travel_marine_calm
                    ),
                    color = if (isMarineCaution(weather, marineWeather)) Color(0xFFFFD180) else HomeAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Montserrat
                )
                if (refreshFailed) MarineStatusText(R.string.travel_marine_cached)
                Text(
                    text = stringResource(
                        R.string.travel_marine_updated,
                        SimpleDateFormat("dd.MM. HH:mm", Locale.GERMANY)
                            .format(Date(marineWeather.fetchedAtMillis))
                    ),
                    color = Color(0x80FFFFFF),
                    fontSize = 9.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun MarineMetrics(marine: MarineWeather, weather: RhodosWeather?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Metric(
            R.string.travel_marine_water,
            marine.seaSurfaceTemperatureCelsius?.let {
                stringResource(R.string.travel_marine_water_value, it)
            } ?: stringResource(R.string.travel_value_unknown),
            Modifier.weight(1f)
        )
        Metric(
            R.string.travel_marine_waves,
            marine.waveHeightMeters?.let {
                stringResource(R.string.travel_marine_wave_value, it)
            } ?: stringResource(R.string.travel_value_unknown),
            Modifier.weight(1f)
        )
        Metric(
            R.string.travel_marine_period,
            marine.wavePeriodSeconds?.let {
                stringResource(R.string.travel_marine_period_value, it)
            } ?: stringResource(R.string.travel_value_unknown),
            Modifier.weight(1f)
        )
    }
    Spacer(Modifier.height(10.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Metric(
            R.string.travel_marine_uv,
            weather?.uvIndex?.let {
                stringResource(R.string.travel_marine_uv_value, it)
            } ?: stringResource(R.string.travel_value_unknown),
            Modifier.weight(1f)
        )
        Metric(
            R.string.travel_marine_wind,
            weather?.let {
                stringResource(R.string.travel_marine_wind_value, it.windSpeedKmh)
            } ?: stringResource(R.string.travel_value_unknown),
            Modifier.weight(2f)
        )
    }
}

@Composable
private fun Metric(@StringRes labelRes: Int, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(labelRes),
            color = Color(0x99FFFFFF),
            fontSize = 9.sp,
            fontFamily = Montserrat
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Montserrat
        )
    }
}

@Composable
private fun MarineStatusText(@StringRes textRes: Int) {
    Text(
        text = stringResource(textRes),
        color = Color(0xBFFFFFFF),
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontFamily = Montserrat
    )
}

@Composable
internal fun DayPlanCard(kind: DayPlanKind) {
    val (titleRes, reasonRes) = dayPlanText(kind)
    TravelCardContainer {
        Text(
            text = stringResource(titleRes),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Montserrat
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = stringResource(reasonRes),
            color = Color(0xD9FFFFFF),
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontFamily = Montserrat
        )
    }
}

private fun dayPlanText(kind: DayPlanKind): Pair<Int, Int> = when (kind) {
    DayPlanKind.LINDOS_EARLY -> R.string.travel_plan_lindos_title to R.string.travel_plan_lindos_reason
    DayPlanKind.SHADE -> R.string.travel_plan_shade_title to R.string.travel_plan_shade_reason
    DayPlanKind.INLAND -> R.string.travel_plan_inland_title to R.string.travel_plan_inland_reason
    DayPlanKind.OLD_TOWN -> R.string.travel_plan_old_town_title to R.string.travel_plan_old_town_reason
    DayPlanKind.BEACH -> R.string.travel_plan_beach_title to R.string.travel_plan_beach_reason
    DayPlanKind.EVENING -> R.string.travel_plan_evening_title to R.string.travel_plan_evening_reason
}

@Composable
internal fun EmergencyContactsCard(
    contacts: List<EmergencyContact>,
    onCall: (String) -> Unit
) {
    TravelCardContainer {
        Text(
            text = stringResource(R.string.travel_help_more),
            color = HomeAccent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
        contacts.forEach { contact ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(contact.titleRes),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontFamily = Montserrat,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { onCall(contact.number) }) {
                    Text(
                        text = stringResource(R.string.travel_call_number, contact.number),
                        color = HomeAccent
                    )
                }
            }
        }
    }
}

@Composable
internal fun TravelMapHelpCard(onHospital: () -> Unit, onPharmacy: () -> Unit) {
    TravelCardContainer {
        MapHelpRow(
            titleRes = R.string.travel_hospital_title,
            descriptionRes = R.string.travel_hospital_description,
            onClick = onHospital
        )
        Spacer(Modifier.height(8.dp))
        MapHelpRow(
            titleRes = R.string.travel_pharmacy_title,
            descriptionRes = R.string.travel_pharmacy_description,
            onClick = onPharmacy
        )
    }
}

@Composable
private fun MapHelpRow(
    @StringRes titleRes: Int,
    @StringRes descriptionRes: Int,
    onClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(titleRes),
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Montserrat
            )
            Text(
                text = stringResource(descriptionRes),
                color = Color(0xBFFFFFFF),
                fontSize = 10.sp,
                lineHeight = 15.sp,
                fontFamily = Montserrat
            )
        }
        TextButton(onClick = onClick) {
            Text(stringResource(R.string.travel_map_action), color = HomeAccent)
        }
    }
}

@Composable
internal fun TravelChecklistCard(
    items: List<TravelChecklistItem>,
    completedIds: Set<String>,
    notes: String,
    onToggle: (String) -> Unit,
    onNotesChange: (String) -> Unit
) {
    TravelCardContainer {
        Text(
            text = stringResource(
                R.string.travel_list_progress,
                completedIds.count { id -> items.any { it.id == id } },
                items.size
            ),
            color = HomeAccent,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Montserrat
        )
        Spacer(Modifier.height(6.dp))
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle(item.id) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = item.id in completedIds,
                    onCheckedChange = null
                )
                Text(
                    text = stringResource(item.titleRes),
                    color = Color.White,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    fontFamily = Montserrat,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = notes,
            onValueChange = { if (it.length <= 2_000) onNotesChange(it) },
            label = { Text(stringResource(R.string.travel_notes_label)) },
            placeholder = { Text(stringResource(R.string.travel_notes_placeholder)) },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = stringResource(R.string.travel_notes_saved),
            color = Color(0x80FFFFFF),
            fontSize = 9.sp,
            lineHeight = 14.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
