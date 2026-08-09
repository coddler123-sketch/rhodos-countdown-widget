package com.example.rhodoswidget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import java.util.Locale

@Composable
internal fun LiveTimetablesCard(
    documents: List<TransitDocument>,
    isLoading: Boolean,
    refreshFailed: Boolean,
    isCached: Boolean,
    onRefresh: () -> Unit,
    onOpen: (TransitDocument) -> Unit
) {
    TravelCardContainer {
        LiveSectionHeader(
            title = stringResource(R.string.travel_live_timetables_title),
            description = stringResource(R.string.travel_live_timetables_description),
            isLoading = isLoading,
            onRefresh = onRefresh
        )
        when {
            documents.isEmpty() && isLoading -> LiveStatus(R.string.travel_live_loading)
            documents.isEmpty() -> LiveStatus(R.string.travel_live_empty)
            else -> documents.forEach { document ->
                TimetableRow(document = document, onOpen = { onOpen(document) })
            }
        }
        if (refreshFailed) LiveStatus(R.string.travel_live_failed)
        else if (isCached && documents.isNotEmpty()) LiveStatus(R.string.travel_live_cached)
    }
}

@Composable
internal fun LiveEventsCard(
    events: List<RhodesEvent>,
    translations: Map<Int, TranslatedEventText>,
    isTranslationLoading: Boolean,
    isTranslationPending: Boolean,
    isLoading: Boolean,
    refreshFailed: Boolean,
    isCached: Boolean,
    onRefresh: () -> Unit,
    onOpen: (RhodesEvent) -> Unit,
    onTranslationInfo: () -> Unit
) {
    TravelCardContainer {
        LiveSectionHeader(
            title = stringResource(R.string.travel_live_events_title),
            description = stringResource(R.string.travel_live_events_description),
            isLoading = isLoading,
            onRefresh = onRefresh
        )
        when {
            events.isEmpty() && isLoading -> LiveStatus(R.string.travel_live_loading)
            events.isEmpty() -> LiveStatus(R.string.travel_live_events_empty)
            else -> events.forEach { event ->
                EventRow(
                    event = event,
                    translation = translations[event.id],
                    onOpen = { onOpen(event) },
                    onTranslationInfo = onTranslationInfo
                )
            }
        }
        if (isTranslationLoading) LiveStatus(R.string.travel_translation_loading)
        else if (isTranslationPending) LiveStatus(R.string.travel_translation_wifi)
        if (translations.isNotEmpty()) LiveStatus(R.string.travel_translation_disclaimer)
        if (refreshFailed) LiveStatus(R.string.travel_live_failed)
        else if (isCached && events.isNotEmpty()) LiveStatus(R.string.travel_live_cached)
    }
}

@Composable
private fun LiveSectionHeader(
    title: String,
    description: String,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Montserrat
            )
            Text(
                text = description,
                color = Color(0xBFFFFFFF),
                fontSize = 10.sp,
                lineHeight = 15.sp,
                fontFamily = Montserrat
            )
        }
        TextButton(onClick = onRefresh, enabled = !isLoading) {
            Text(stringResource(R.string.travel_live_refresh), color = HomeAccent)
        }
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun TimetableRow(document: TransitDocument, onOpen: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = document.title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Montserrat
            )
            Text(
                text = stringResource(R.string.travel_timetable_operator, document.operator),
                color = Color(0x99FFFFFF),
                fontSize = 9.sp,
                fontFamily = Montserrat
            )
        }
        TextButton(onClick = onOpen) {
            Text(stringResource(R.string.travel_timetable_open), color = HomeAccent)
        }
    }
}

@Composable
private fun EventRow(
    event: RhodesEvent,
    translation: TranslatedEventText?,
    onOpen: () -> Unit,
    onTranslationInfo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp)
    ) {
        Text(
            text = formatEventDate(event.startDateTime, event.endDateTime),
            color = HomeAccent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Montserrat
        )
        Text(
            text = translation?.title ?: event.title,
            color = Color.White,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Montserrat
        )
        translation?.let {
            Text(
                text = stringResource(R.string.travel_translation_original, event.title),
                color = Color(0x80FFFFFF),
                fontSize = 9.sp,
                lineHeight = 13.sp,
                fontFamily = Montserrat
            )
        }
        event.venue?.let { venue ->
            Text(
                text = stringResource(R.string.travel_event_venue, translation?.venue ?: venue),
                color = Color(0x99FFFFFF),
                fontSize = 9.sp,
                fontFamily = Montserrat
            )
        }
        TextButton(onClick = onOpen, contentPadding = PaddingValues(top = 2.dp)) {
            Text(stringResource(R.string.travel_event_open), color = HomeAccent)
        }
        if (translation != null) {
            TextButton(onClick = onTranslationInfo, contentPadding = PaddingValues(top = 0.dp)) {
                Text(
                    text = stringResource(R.string.travel_translation_attribution),
                    color = Color(0x99FFFFFF),
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
private fun LiveStatus(textRes: Int) {
    Text(
        text = stringResource(textRes),
        color = Color(0x99FFFFFF),
        fontSize = 10.sp,
        lineHeight = 15.sp,
        fontFamily = Montserrat,
        modifier = Modifier.padding(top = 6.dp)
    )
}

internal fun formatEventDate(start: String, end: String): String {
    val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    val output = SimpleDateFormat("EEE, dd.MM. · HH:mm", Locale.GERMANY)
    val startDate = runCatching { parser.parse(start) }.getOrNull() ?: return start
    val endDate = runCatching { parser.parse(end) }.getOrNull()
    return if (endDate != null && SimpleDateFormat("yyyyMMdd", Locale.US).format(startDate) !=
        SimpleDateFormat("yyyyMMdd", Locale.US).format(endDate)
    ) {
        "${output.format(startDate)} – ${SimpleDateFormat("dd.MM. · HH:mm", Locale.GERMANY).format(endDate)}"
    } else {
        output.format(startDate)
    }
}
