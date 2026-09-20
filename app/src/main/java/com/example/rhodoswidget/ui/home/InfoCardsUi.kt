package com.example.rhodoswidget.ui.home
import com.example.rhodoswidget.*

import com.example.rhodoswidget.R
import com.example.rhodoswidget.MainActivity
import com.example.rhodoswidget.ui.compass.*
import com.example.rhodoswidget.ui.travel.*
import com.example.rhodoswidget.ui.news.*
import com.example.rhodoswidget.ui.weather.*
import com.example.rhodoswidget.ui.settings.*
import com.example.rhodoswidget.ui.theme.*
import com.example.rhodoswidget.widget.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

@Composable
fun CompassCard(onClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HomeCardShape)
            .background(HomeCardColor)
            .border(1.dp, HomeAccent.copy(alpha = 0.5f), HomeCardShape)
            .testTag("compass-link")
            .clickable {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        Text("RHODOS KOMPASS", color = HomeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
        Spacer(Modifier.height(5.dp))
        Text("${compassTips.size} ausgewählte Rhodos-Tipps", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = Montserrat)
        Spacer(Modifier.height(4.dp))
        Text("Strände, Essen, Unterkünfte und Mobilität entdecken  ›", color = Color(0xCCFFFFFF), fontSize = 11.sp, lineHeight = 16.sp, fontFamily = Montserrat)
    }
}

@Composable
fun TravelCard(onClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HomeCardShape)
            .background(HomeCardColor)
            .border(1.dp, HomeAccent.copy(alpha = 0.5f), HomeCardShape)
            .testTag("travel-link")
            .clickable {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        Text(
            text = stringResource(R.string.travel_card_label),
            color = HomeAccent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = stringResource(R.string.travel_card_title),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Montserrat
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.travel_card_description),
            color = Color(0xCCFFFFFF),
            fontSize = 11.sp,
            lineHeight = 16.sp,
            fontFamily = Montserrat
        )
    }
}

@Composable
fun HomeQuickActions(
    onOpenKolymbia: () -> Unit,
    onOpenTavernCalc: () -> Unit = {},
    onOpenPhrasebook: () -> Unit = {},
    onOpenEmergency: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "URLAUBS-QUICK-TOOLS 🛠️",
            color = HomeAccent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Montserrat,
            letterSpacing = 0.8.sp
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HomeQuickGridCard(
                title = "🚌 Bus Kolymbia",
                subtitle = "Fahrplan & Abfahrt",
                testTag = "kolymbia-bus-link",
                onClick = onOpenKolymbia,
                modifier = Modifier.weight(1f),
                emphasized = true
            )
            HomeQuickGridCard(
                title = "💶 Tavernen-Rechner",
                subtitle = "Trinkgeld & Splitting",
                testTag = "taverna-calc-link",
                onClick = onOpenTavernCalc,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HomeQuickGridCard(
                title = "🗣️ Sprachführer",
                subtitle = "Redewendungen",
                testTag = "phrasebook-link",
                onClick = onOpenPhrasebook,
                modifier = Modifier.weight(1f)
            )
            HomeQuickGridCard(
                title = "🏥 SOS Notfall",
                subtitle = "Arzt, Taxi & Hotel",
                testTag = "emergency-link",
                onClick = onOpenEmergency,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun HomeDayPlanSection(
    isOnVacation: Boolean,
    kind: DayPlanKind
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home-day-plan")
    ) {
        DayPlanCard(
            kind = kind,
            label = stringResource(
                if (isOnVacation) R.string.home_tip_today_label
                else R.string.home_tip_planning_label
            )
        )
    }
}

@Composable
private fun HomeQuickGridCard(
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false
) {
    val haptics = LocalHapticFeedback.current
    Column(
        modifier = modifier
            .heightIn(min = 72.dp)
            .clip(HomeCardShape)
            .background(if (emphasized) HomeAccent.copy(alpha = 0.20f) else HomeCardColor)
            .border(1.dp, HomeAccent.copy(alpha = if (emphasized) 0.85f else 0.4f), HomeCardShape)
            .testTag(testTag)
            .clickable {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Montserrat
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = subtitle,
            color = Color(0xCCFFFFFF),
            fontSize = 10.sp,
            fontFamily = Montserrat
        )
    }
}

@Composable
fun FactCard(fact: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HomeCardShape)
            .background(HomeCardColor)
            .border(1.dp, HomeCardBorder, HomeCardShape)
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        Text(
            text = "RHODOS-FAKT",
            color = HomeAccent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Montserrat,
            letterSpacing = 0.8.sp
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = fact,
            color = Color(0xF2FFFFFF),
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontFamily = Montserrat
        )
    }
}

@Composable
fun HighlightCard(highlight: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HomeCardShape)
            .background(HomeCardColor)
            .border(1.dp, HomeCardBorder, HomeCardShape)
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        Text(
            text = "TAGESHÖHEPUNKT",
            color = HomeAccent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Montserrat,
            letterSpacing = 0.8.sp
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = highlight,
            color = Color(0xF2FFFFFF),
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontFamily = Montserrat
        )
    }
}

@Composable
fun CommunityCard(onClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HomeCardShape)
            .background(HomeCardColor)
            .border(1.dp, HomeCardBorder, HomeCardShape)
            .testTag("community-link")
            .clickable {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        Text(
            text = stringResource(R.string.community_label),
            color = HomeAccent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Montserrat,
            letterSpacing = 0.8.sp
        )
        Spacer(Modifier.height(5.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.community_title),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Montserrat
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.community_description),
                    color = Color(0xCCFFFFFF),
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    fontFamily = Montserrat
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.community_action),
                    color = HomeAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Montserrat
                )
            }
            Text(text = "↗", color = HomeAccent, fontSize = 20.sp)
        }
    }
}

@Composable
fun FlightDayTimelineCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HomeCardShape)
            .background(Color(0xFF1B3B42))
            .border(1.dp, HomeAccent.copy(alpha = 0.8f), HomeCardShape)
            .padding(16.dp)
            .testTag("flight-day-timeline")
    ) {
        Text(
            text = "HEUTE IST ABFLUGTAG! ✈️",
            color = HomeAccent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Montserrat,
            letterSpacing = 0.8.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Reise-Timeline & Etappen",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Montserrat
        )
        Spacer(Modifier.height(10.dp))
        TimelineItem("14:00 Uhr", "Boarding & Abflugbereich", "Gepäck aufgegeben & Sicherheitskontrolle")
        TimelineItem("14:30 Uhr", "Abflug ab Hamburg (HAM) nach Rhodos 🛫", "Direktflug 20.09.2026")
        TimelineItem("19:00 Uhr", "Ankunft Flughafen Rhodos (RHO) 🛬", "Ortszeit Rhodos (+1 Std.)")
        TimelineItem("~20:00 Uhr", "Check-in Relax Hotel Kolymbia 🏨", "Doppelzimmer Superior Shared Pool (RHO071)")
    }
}

@Composable
private fun TimelineItem(time: String, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = time,
            color = HomeAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Montserrat,
            modifier = Modifier.width(75.dp)
        )
        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Montserrat
            )
            Text(
                text = subtitle,
                color = Color(0xAAFFFFFF),
                fontSize = 10.sp,
                fontFamily = Montserrat
            )
        }
    }
}
