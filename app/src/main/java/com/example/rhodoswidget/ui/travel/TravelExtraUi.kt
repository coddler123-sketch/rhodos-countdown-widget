package com.example.rhodoswidget.ui.travel
import com.example.rhodoswidget.*

import com.example.rhodoswidget.R
import com.example.rhodoswidget.MainActivity
import com.example.rhodoswidget.ui.home.*
import com.example.rhodoswidget.ui.compass.*
import com.example.rhodoswidget.ui.news.*
import com.example.rhodoswidget.ui.weather.*
import com.example.rhodoswidget.ui.settings.*
import com.example.rhodoswidget.ui.theme.*
import com.example.rhodoswidget.widget.*

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
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
internal fun DayPlanCard(kind: DayPlanKind, label: String) {
    val (titleRes, reasonRes) = dayPlanText(kind)
    TravelCardContainer {
        Text(
            text = label,
            color = HomeAccent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Montserrat,
            letterSpacing = 0.8.sp
        )
        Spacer(Modifier.height(5.dp))
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

@Composable
internal fun TavernCalculatorCard() {
    var amountInput by rememberSaveable { mutableStateOf("") }
    var selectedTipPercent by rememberSaveable { mutableStateOf(10) }
    var peopleCount by rememberSaveable { mutableStateOf(2) }

    val amount = amountInput.replace(',', '.').toDoubleOrNull() ?: 0.0
    val tipMultiplier = 1.0 + (selectedTipPercent / 100.0)
    val totalWithTip = amount * tipMultiplier
    val perPerson = if (peopleCount > 0) totalWithTip / peopleCount else totalWithTip

    TravelCardContainer {
        Text(
            text = "💶 TAVERNEN- & TRINKGELD-RECHNER",
            color = HomeAccent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Montserrat,
            letterSpacing = 0.8.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Rechnung entspannt aufteilen",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Montserrat
        )
        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = amountInput,
            onValueChange = { if (it.length <= 7) amountInput = it },
            label = { Text("Rechnungsbetrag (€)", color = Color(0xCCFFFFFF)) },
            placeholder = { Text("z.B. 45.00", color = Color(0x66FFFFFF)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = HomeAccent,
                unfocusedBorderColor = Color(0x66FFFFFF)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tavern-calculator-amount-input")
        )

        Spacer(Modifier.height(10.dp))
        Text("Trinkgeld wählen (Griecheland ca. 5–10 %):", color = Color(0xBFFFFFFF), fontSize = 11.sp, fontFamily = Montserrat)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(5, 10, 15).forEach { percent ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTipPercent == percent) HomeAccent else Color(0xFF22363B))
                        .clickable { selectedTipPercent = percent }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        "$percent %",
                        color = if (selectedTipPercent == percent) Color(0xFF102A2F) else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Personen:", color = Color(0xBFFFFFFF), fontSize = 12.sp, fontFamily = Montserrat)
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22363B))
                    .clickable { if (peopleCount > 1) peopleCount-- }
            ) {
                Text("-", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
            }
            Text("$peopleCount", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 14.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22363B))
                    .clickable { if (peopleCount < 10) peopleCount++ }
            ) {
                Text("+", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
            }
        }

        if (amount > 0) {
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF173D44))
                    .padding(12.dp)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gesamt inkl. Trinkgeld:", color = Color.White, fontSize = 12.sp)
                        Text(String.format(Locale.GERMANY, "%.2f €", totalWithTip), color = HomeAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    if (peopleCount > 1) {
                        Spacer(Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pro Person ($peopleCount P.):", color = Color.White, fontSize = 12.sp)
                            Text(String.format(Locale.GERMANY, "%.2f €", perPerson), color = HomeAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun OfflineTravelSummaryCard() {
    TravelCardContainer {
        Text(
            text = "📴 ANREISE & OFFLINE-NOTFALLkarte",
            color = HomeAccent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Montserrat,
            letterSpacing = 0.8.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Wichtiges auf einen Blick (Ohne Internet)",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Montserrat
        )
        Spacer(Modifier.height(10.dp))
        Text("✈️ Hinflug: So. 20.09.2026 | 14:30 Uhr (Hamburg HAM ➔ Rhodos RHO)", color = Color.White, fontSize = 12.sp, lineHeight = 17.sp)
        Text("🏨 Hotel: Relax Hotel Kolymbia (Doppelzimmer Superior Shared Pool)", color = Color(0xEEFFFFFF), fontSize = 11.sp, lineHeight = 16.sp)
        Text("📍 Adresse: Eucalyptus Street, Kolymbia 851 02, Rhodos", color = Color(0xCCFFFFFF), fontSize = 11.sp, lineHeight = 16.sp)
        Spacer(Modifier.height(6.dp))
        Text("🚨 Notrufnummern: 112 (EU-Notruf) | 100 (Polizei) | 166 (Notarzt)", color = HomeAccent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
