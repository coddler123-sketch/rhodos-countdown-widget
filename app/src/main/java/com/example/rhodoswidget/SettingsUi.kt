package com.example.rhodoswidget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsSheet(
    isCheckingUpdate: Boolean,
    hasUpdate: Boolean,
    onCheckUpdate: () -> Unit,
    onShare: () -> Unit,
    onOpenGallery: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Optionen",
            color = Color(0x99FFFFFF),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Montserrat,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        SettingsRow(
            icon = if (hasUpdate) "🔴" else "✓",
            label = "Version v${BuildConfig.VERSION_NAME}",
            detail = if (hasUpdate) "Update verfügbar" else "Aktuellste Version",
            actionLabel = when {
                isCheckingUpdate -> "Lädt …"
                hasUpdate -> "Installieren"
                else -> "Prüfen"
            },
            onAction = onCheckUpdate,
            enabled = !isCheckingUpdate,
            actionTag = "settings-check-update"
        )
        SettingsRow(
            icon = "🖼",
            label = "Hintergrundbild",
            detail = "Bildergalerie & Favoriten",
            actionLabel = "Galerie",
            onAction = onOpenGallery,
            actionTag = "settings-open-gallery"
        )
        SettingsRow(
            icon = "↗",
            label = "Countdown teilen",
            detail = "Via WhatsApp, SMS, …",
            actionLabel = "Teilen",
            onAction = onShare,
            actionTag = "settings-share"
        )
    }
}

@Composable
private fun SettingsRow(
    icon: String,
    label: String,
    detail: String,
    actionLabel: String,
    onAction: () -> Unit,
    enabled: Boolean = true,
    actionTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 16.sp, modifier = Modifier.width(28.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, color = Color.White, fontSize = 14.sp, fontFamily = Montserrat, fontWeight = FontWeight.SemiBold)
            Text(text = detail, color = Color(0x99FFFFFF), fontSize = 11.sp, fontFamily = Montserrat)
        }
        SecondaryActionButton(
            text = actionLabel,
            enabled = enabled,
            onClick = onAction,
            modifier = Modifier.testTag(actionTag)
        )
    }
}

@Composable
private fun SecondaryActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0x24FFFFFF)
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(32.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White,
            disabledContainerColor = Color(0x18FFFFFF),
            disabledContentColor = Color(0x99FFFFFF)
        )
    ) {
        Text(
            text = text,
            fontFamily = Montserrat,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp
        )
    }
}
