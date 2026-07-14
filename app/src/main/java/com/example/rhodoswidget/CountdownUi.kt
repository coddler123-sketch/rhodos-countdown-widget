package com.example.rhodoswidget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun CountdownProgress(fraction: Float) {
    Column(modifier = Modifier.padding(top = 10.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "REISEFORTSCHRITT",
                color = Color(0xBFFFFFFF),
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Montserrat,
                letterSpacing = 0.6.sp
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${(fraction * 100).roundToInt()} %",
                color = Color(0xBFFFFFFF),
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Montserrat
            )
        }
        Spacer(Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0x33FFFFFF))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(HomeAccent)
            )
        }
    }
}

@Composable
fun CountdownSection(s: HomeState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        if (s.isOnVacation) {
            Text(
                text = "Ihr seid auf Rhodos. 🌊",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Montserrat
            )
            Text(
                text = "Genießt jeden Augenblick.",
                color = Color(0xE6FFFFFF),
                fontSize = 18.sp,
                fontFamily = Montserrat,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else if (s.isReached) {
            Text(
                text = "Es ist soweit!",
                color = Color.White,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Montserrat
            )
        } else {
            CappedFontScale(maxScale = 1.15f) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CountdownBlock(s.days.toString(), "TAGE")
                    CountdownDivider()
                    CountdownBlock(s.hours.toString().padStart(2, '0'), "STD.")
                    CountdownDivider()
                    CountdownBlock(s.minutes.toString().padStart(2, '0'), "MIN.")
                    CountdownDivider()
                    CountdownBlock(s.seconds.toString().padStart(2, '0'), "SEK.", secondary = true)
                }
            }
            countdownMilestone(s.days)?.let { milestone ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(top = 18.dp)
                            .clip(HomeCardShape)
                            .background(HomeAccent.copy(alpha = 0.18f))
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Text(
                            milestone.title,
                            color = HomeAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Montserrat,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            milestone.message,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = Montserrat
                        )
                    }
                }
            }
        }
        if (!s.isOnVacation) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "„${s.phrase}“",
                color = Color.White,
                fontSize = 17.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = Montserrat,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CountdownBlock(value: String, label: String, secondary: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = if (secondary) Color(0xD9FFFFFF) else Color.White,
            fontSize = if (secondary) 38.sp else 42.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Montserrat
        )
        Text(
            text = label,
            color = if (secondary) Color(0xA6FFFFFF) else Color(0xE6FFFFFF),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Montserrat,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun CappedFontScale(maxScale: Float, content: @Composable () -> Unit) {
    val current = LocalDensity.current
    val capped = remember(current.density, current.fontScale, maxScale) {
        Density(
            density = current.density,
            fontScale = min(current.fontScale, maxScale)
        )
    }
    CompositionLocalProvider(LocalDensity provides capped, content = content)
}

@Composable
private fun CountdownDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 11.dp)
            .width(1.dp)
            .height(42.dp)
            .background(Color(0x66FFFFFF))
    )
}
