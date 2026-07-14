package com.example.rhodoswidget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
        Text("13 Tipps aus der Community", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = Montserrat)
        Spacer(Modifier.height(4.dp))
        Text("Strände, Essen, Unterkünfte und Mobilität entdecken  ›", color = Color(0xCCFFFFFF), fontSize = 11.sp, lineHeight = 16.sp, fontFamily = Montserrat)
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
            text = "HIGHLIGHT DES TAGES",
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
