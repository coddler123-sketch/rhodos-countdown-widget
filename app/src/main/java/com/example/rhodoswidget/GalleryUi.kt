package com.example.rhodoswidget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GallerySheet(
    onDismissRequest: () -> Unit,
    onApply: (String?, Float) -> Unit,
    currentImageName: String,
    pinnedImageName: String?,
    backgroundDim: Float
) {
    var pendingImage by rememberSaveable(pinnedImageName) { mutableStateOf(pinnedImageName) }
    var pendingDim by rememberSaveable(backgroundDim) { mutableStateOf(backgroundDim) }
    val previewName = pendingImage?.takeUnless { it == "auto" } ?: currentImageName
    val haptics = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF111116),
        dragHandle = {
            Box(
                Modifier.padding(vertical = 12.dp).width(36.dp).height(4.dp)
                    .background(Color(0x40FFFFFF), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f).padding(horizontal = 20.dp)
        ) {
            Text(
                "Hintergrund gestalten",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Montserrat,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Box(
                modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(16.dp)).background(Color.Black)
            ) {
                Image(
                    painter = painterResource(Images.resourceOf(previewName)),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = pendingDim)))
                Text(
                    Images.displayNameOf(previewName),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Montserrat,
                    modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Abdunklung", color = Color(0xCCFFFFFF), fontSize = 11.sp, fontFamily = Montserrat)
                Slider(
                    value = pendingDim,
                    onValueChange = { pendingDim = it },
                    valueRange = 0.4f..0.9f,
                    modifier = Modifier.weight(1f).testTag("gallery-dim-slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = HomeAccent,
                        activeTrackColor = HomeAccent,
                        inactiveTrackColor = Color(0x33FFFFFF)
                    )
                )
            }

            val isAuto = pendingImage == null || pendingImage == "auto"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isAuto) Color(0x33FFFFFF) else Color(0x12FFFFFF))
                    .testTag("gallery-auto-image")
                    .selectable(
                        selected = isAuto,
                        role = Role.RadioButton,
                        onClick = {
                            pendingImage = null
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    )
                    .clearAndSetSemantics {
                        contentDescription = "Automatischen Bildwechsel auswählen"
                        role = Role.RadioButton
                        selected = isAuto
                        onClick { pendingImage = null; true }
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("AUTOMATIK", color = HomeAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(72.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Täglicher Bildwechsel", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = Montserrat)
                    Text("Jeden Tag eine neue Rhodos-Impression", color = Color(0x99FFFFFF), fontSize = 10.sp, fontFamily = Montserrat)
                }
                if (isAuto) Text("✓", color = HomeAccent, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(10.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f).padding(bottom = 10.dp)
            ) {
                items(Images.allImageNames, key = { it }) { imageName ->
                    GalleryImage(
                        imageName = imageName,
                        selected = pendingImage == imageName,
                        currentInRotation = isAuto && currentImageName == imageName,
                        onClick = {
                            pendingImage = imageName
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    )
                }
            }

            Button(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onApply(pendingImage, pendingDim)
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("gallery-apply"),
                colors = ButtonDefaults.buttonColors(containerColor = HomeAccent, contentColor = Color(0xFF102126))
            ) {
                Text("Hintergrund übernehmen", fontWeight = FontWeight.Bold, fontFamily = Montserrat)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun GalleryImage(
    imageName: String,
    selected: Boolean,
    currentInRotation: Boolean,
    onClick: () -> Unit
) {
    val displayName = Images.displayNameOf(imageName)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x12FFFFFF))
            .testTag("gallery-image-$imageName")
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = "$displayName als Hintergrundbild auswählen"
                role = Role.RadioButton
                this.selected = selected
                onClick { onClick(); true }
            }
    ) {
        Image(
            painter = painterResource(Images.resourceOf(imageName)),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xDD000000)))))
        Text(
            displayName,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Montserrat,
            maxLines = 2,
            modifier = Modifier.align(Alignment.BottomStart).padding(9.dp)
        )
        if (selected || currentInRotation) {
            Text(
                if (selected) "✓" else "AUTOMATIK",
                color = Color(0xFF102126),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(HomeAccent, RoundedCornerShape(8.dp)).padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }
    }
}
