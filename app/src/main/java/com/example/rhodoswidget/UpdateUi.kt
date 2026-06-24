package com.example.rhodoswidget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
fun UpdateBanner(
    update: AppUpdate,
    isDownloading: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x33FF6B35))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isDownloading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = Color(0xFFFF6B35),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "💡",
                    fontSize = 16.sp
                )
            }
            Text(
                text = if (isDownloading) "Lade Update herunter..." else "Update v${update.versionName} verfügbar · Tippe zum Installieren",
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = Montserrat,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            if (!isDownloading) {
                Text(
                    text = "➔",
                    color = Color(0x99FFFFFF),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun StartupUpdateDialog(
    update: AppUpdate,
    isDownloading: Boolean,
    onDismiss: () -> Unit,
    onInstall: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!isDownloading) onDismiss()
        },
        title = {
            Text(
                text = "Update verfügbar 🚀",
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Eine neue Version (v${update.versionName}) von deinem Rhodos-Countdown ist verfügbar. Möchtest du sie jetzt installieren?",
                    fontFamily = Montserrat,
                    fontSize = 14.sp,
                    color = Color(0xE6FFFFFF)
                )
                if (isDownloading) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFFFF6B35),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Wird heruntergeladen...",
                            fontFamily = Montserrat,
                            fontSize = 13.sp,
                            color = Color(0xCCFFFFFF)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!isDownloading) onInstall()
                },
                enabled = !isDownloading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35))
            ) {
                Text("Installieren", fontFamily = Montserrat, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        },
        dismissButton = {
            if (!isDownloading) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x24FFFFFF))
                ) {
                    Text("Später", fontFamily = Montserrat, color = Color.White)
                }
            }
        },
        containerColor = Color(0xFF111116)
    )
}
