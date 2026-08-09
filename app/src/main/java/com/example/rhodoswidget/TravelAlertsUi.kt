package com.example.rhodoswidget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun TravelAlertsCard(enabled: Boolean, onEnabledChange: (Boolean) -> Unit) {
    TravelCardContainer {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEnabledChange(!enabled) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.travel_alerts_title),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Montserrat
                )
                Text(
                    text = stringResource(R.string.travel_alerts_description),
                    color = Color(0xBFFFFFFF),
                    fontSize = 10.sp,
                    lineHeight = 15.sp,
                    fontFamily = Montserrat
                )
            }
            Switch(checked = enabled, onCheckedChange = onEnabledChange)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(
                if (enabled) R.string.travel_alerts_enabled else R.string.travel_alerts_disabled
            ),
            color = if (enabled) HomeAccent else Color(0x80FFFFFF),
            fontSize = 9.sp,
            lineHeight = 14.sp,
            fontFamily = Montserrat
        )
    }
}
