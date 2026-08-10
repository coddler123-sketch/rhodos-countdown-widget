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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                    stringResource(R.string.travel_german_timetable_validity, KolymbiaTimetable.VALIDITY),
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
        item { TimetableSectionTitle(R.string.travel_german_timetable_from_kolymbia) }
        items(KolymbiaTimetable.fromKolymbia, key = { "from_kolymbia_${it.place}" }) { route ->
            TimetableRouteCard(route, R.string.travel_german_timetable_to)
        }
        item { TimetableSectionTitle(R.string.travel_german_timetable_to_kolymbia) }
        items(KolymbiaTimetable.toKolymbia, key = { "to_kolymbia_${it.place}" }) { route ->
            TimetableRouteCard(route, R.string.travel_german_timetable_from)
        }
    }
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
