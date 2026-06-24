package com.example.rhodoswidget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FactCard(fact: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x1AFFFFFF))
            .padding(horizontal = 15.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "🏛",
            fontSize = 16.sp,
            modifier = Modifier.padding(end = 10.dp, top = 1.dp)
        )
        Column {
            Text(
                text = "Rhodos-Fakt des Tages",
                color = Color(0xBFFFFFFF),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Montserrat
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = fact,
                color = Color.White,
                fontSize = 13.sp,
                fontFamily = Montserrat
            )
        }
    }
}

@Composable
fun HighlightCard(highlight: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x1AFFFFFF))
            .padding(horizontal = 15.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = "📍", fontSize = 16.sp, modifier = Modifier.padding(end = 10.dp, top = 1.dp))
        Column {
            Text(
                text = "Highlight des Tages",
                color = Color(0xBFFFFFFF),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Montserrat
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = highlight,
                color = Color.White,
                fontSize = 13.sp,
                fontFamily = Montserrat
            )
        }
    }
}
