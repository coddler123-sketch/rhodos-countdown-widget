package com.example.rhodoswidget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun LindosGermanTimetable(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    stringResource(R.string.travel_german_timetable_title),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Montserrat
                )
                Text(
                    stringResource(R.string.travel_german_timetable_validity, LindosTimetable.VALIDITY),
                    color = HomeAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Montserrat
                )
                Text(
                    stringResource(R.string.travel_german_timetable_note),
                    color = Color(0xB3FFFFFF),
                    fontSize = 10.sp,
                    fontFamily = Montserrat
                )
            }
        }
        item { TimetableSectionTitle(R.string.travel_german_timetable_from_lindos) }
        items(LindosTimetable.fromLindos, key = { "from_${it.place}" }) { route ->
            TimetableRouteCard(route, R.string.travel_german_timetable_to)
        }
        item { TimetableSectionTitle(R.string.travel_german_timetable_to_lindos) }
        items(LindosTimetable.toLindos, key = { "to_${it.place}" }) { route ->
            TimetableRouteCard(route, R.string.travel_german_timetable_from)
        }
    }
}

@Composable
internal fun KolymbiaGermanTimetable(modifier: Modifier = Modifier) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedPlace by rememberSaveable { mutableStateOf<String?>(null) }
    var showReferenceSchedule by rememberSaveable { mutableStateOf(false) }
    val connections = KolymbiaTimetable.searchConnections(query)
    val selectedConnection = KolymbiaTimetable.connections.firstOrNull { it.place == selectedPlace }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    stringResource(R.string.travel_timetable_trip_status_title),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Montserrat
                )
                Text(
                    stringResource(R.string.travel_timetable_trip_status_dates),
                    color = HomeAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Montserrat
                )
                Text(
                    stringResource(R.string.travel_timetable_trip_status_note, KolymbiaTimetable.VALIDITY),
                    color = Color(0xB3FFFFFF),
                    fontSize = 10.sp,
                    fontFamily = Montserrat
                )
            }
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    selectedPlace = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.travel_timetable_search_label)) },
                placeholder = { Text(stringResource(R.string.travel_timetable_search_placeholder)) },
                singleLine = true
            )
        }
        item {
            Text(
                text = stringResource(R.string.travel_timetable_quick_destinations),
                color = HomeAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Montserrat
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickDestinationRow(
                    places = listOf("Rhodos-Stadt", "Lindos"),
                    onSelect = { place ->
                        query = place
                        selectedPlace = place
                    }
                )
                QuickDestinationRow(
                    places = listOf("Tsambika-Strand", "Sieben Quellen"),
                    onSelect = { place ->
                        query = place
                        selectedPlace = place
                    }
                )
            }
        }
        if (selectedConnection != null) {
            item {
                KolymbiaConnectionDetail(
                    connection = selectedConnection,
                    showSchedule = KolymbiaTimetable.isValidForTrip || showReferenceSchedule,
                    isReferenceSchedule = !KolymbiaTimetable.isValidForTrip,
                    onShowReference = { showReferenceSchedule = true },
                    onHideReference = { showReferenceSchedule = false },
                    onChooseAnother = {
                        query = ""
                        selectedPlace = null
                        showReferenceSchedule = false
                    }
                )
            }
        } else if (connections.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x14FFFFFF), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.travel_timetable_no_results),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontFamily = Montserrat
                    )
                    TextButton(onClick = { query = "" }) {
                        Text(stringResource(R.string.travel_timetable_clear_search), color = HomeAccent)
                    }
                }
            }
        } else {
            item {
                Text(
                    text = pluralStringResource(
                        R.plurals.travel_timetable_results,
                        connections.size,
                        connections.size
                    ),
                    color = Color(0xB3FFFFFF),
                    fontSize = 10.sp,
                    fontFamily = Montserrat
                )
            }
            items(connections, key = KolymbiaConnection::place) { connection ->
                KolymbiaConnectionSummary(
                    connection = connection,
                    onOpen = { selectedPlace = connection.place }
                )
            }
        }
    }
}

@Composable
private fun QuickDestinationRow(
    places: List<String>,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        places.forEach { place ->
            Button(
                onClick = { onSelect(place) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0x1FFFFFFF),
                    contentColor = Color.White
                )
            ) {
                Text(place, fontSize = 10.sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun KolymbiaConnectionSummary(
    connection: KolymbiaConnection,
    onOpen: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x14FFFFFF), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = connection.place,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Montserrat
            )
            Text(
                text = connection.outbound.price,
                color = HomeAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Montserrat
            )
        }
        Text(
            text = stringResource(R.string.travel_timetable_connection_hint),
            color = Color(0x99FFFFFF),
            fontSize = 10.sp,
            fontFamily = Montserrat
        )
        TextButton(onClick = onOpen) {
            Text(stringResource(R.string.travel_timetable_view_connection), color = HomeAccent)
        }
    }
}

@Composable
private fun KolymbiaConnectionDetail(
    connection: KolymbiaConnection,
    showSchedule: Boolean,
    isReferenceSchedule: Boolean,
    onShowReference: () -> Unit,
    onHideReference: () -> Unit,
    onChooseAnother: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x14FFFFFF), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = connection.place,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Montserrat
            )
            Text(
                text = connection.outbound.price,
                color = HomeAccent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Montserrat
            )
        }
        TextButton(onClick = onChooseAnother) {
            Text(stringResource(R.string.travel_timetable_choose_another), color = HomeAccent)
        }
        if (!showSchedule) {
            Text(
                text = stringResource(R.string.travel_timetable_reference_warning),
                color = Color(0xCCFFFFFF),
                fontSize = 11.sp,
                fontFamily = Montserrat
            )
            Button(
                onClick = onShowReference,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HomeAccent,
                    contentColor = Color(0xFF102126)
                )
            ) {
                Text(stringResource(R.string.travel_timetable_show_reference))
            }
        } else {
            if (isReferenceSchedule) {
                Text(
                    text = stringResource(R.string.travel_timetable_reference_label),
                    color = HomeAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Montserrat
                )
            }
            ConnectionTimes(
                titleRes = R.string.travel_timetable_outbound,
                route = connection.outbound
            )
            ConnectionTimes(
                titleRes = R.string.travel_timetable_return,
                route = connection.returnTrip
            )
            if (isReferenceSchedule) {
                TextButton(onClick = onHideReference) {
                    Text(stringResource(R.string.travel_timetable_hide_reference), color = HomeAccent)
                }
            }
        }
    }
}

@Composable
private fun ConnectionTimes(titleRes: Int, route: LindosTimetableRoute) {
    Text(
        text = stringResource(titleRes),
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = Montserrat
    )
    Text(
        text = route.departureTimes.joinToString("  ·  "),
        color = Color(0xE6FFFFFF),
        fontSize = 12.sp,
        lineHeight = 19.sp,
        fontFamily = Montserrat
    )
}

@Composable
private fun TimetableSectionTitle(textRes: Int) {
    Text(
        text = stringResource(textRes),
        color = HomeAccent,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = Montserrat,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun TimetableRouteCard(route: LindosTimetableRoute, directionRes: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x14FFFFFF), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(directionRes, route.place),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = Montserrat
                )
                route.greekName?.let { greek ->
                    Text(greek, color = Color(0x8FFFFFFF), fontSize = 10.sp, fontFamily = Montserrat)
                }
            }
            Text(
                text = route.price,
                color = HomeAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Montserrat
            )
        }
        Text(
            text = stringResource(R.string.travel_german_timetable_departures),
            color = Color(0x99FFFFFF),
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Montserrat,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = route.departureTimes.joinToString("  ·  "),
            color = Color(0xE6FFFFFF),
            fontSize = 12.sp,
            lineHeight = 19.sp,
            fontFamily = Montserrat
        )
    }
}
