package com.example.rhodoswidget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GallerySheet(
    onDismissRequest: () -> Unit,
    onSelectImage: (String?) -> Unit,
    currentImageName: String,
    pinnedImageName: String?
) {
    val context = LocalContext.current
    val images = remember { Images.allImageNames }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        containerColor = Color(0xFF111116),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .background(Color(0x40FFFFFF), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Hintergrundbild Galerie",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Montserrat,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val isAuto = pinnedImageName == null || pinnedImageName == "auto"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isAuto) Color(0x33FFFFFF) else Color(0x12FFFFFF))
                    .testTag("gallery-auto-image")
                    .clickable { onSelectImage(null) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🔄", fontSize = 20.sp, modifier = Modifier.padding(end = 12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Täglicher automatischer Bildwechsel",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Montserrat
                    )
                    Text(
                        text = "Jeden Tag ein neues Rhodos-Foto als Vorfreude",
                        color = Color(0x99FFFFFF),
                        fontSize = 11.sp,
                        fontFamily = Montserrat
                    )
                }
                if (isAuto) {
                    Text(text = "✓", color = Color(0xFF66DD88), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f).padding(bottom = 24.dp)
            ) {
                items(images.size) { index ->
                    val imageName = images[index]
                    val isSelected = pinnedImageName == imageName
                    val isCurrentRotation = isAuto && currentImageName == imageName
                    val displayName = Images.displayNameOf(imageName)
                    val drawableId = Images.resourceOf(imageName)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x12FFFFFF))
                            .testTag("gallery-image-$imageName")
                            .clickable { onSelectImage(imageName) }
                    ) {
                        if (drawableId != 0) {
                            Image(
                                painter = painterResource(drawableId),
                                contentDescription = displayName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color(0xAA000000), Color(0xDD000000))
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF66DD88), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Gepinnt", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else if (isCurrentRotation) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF44AAFF), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Aktiv (Auto)", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Text(
                                text = displayName,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = Montserrat,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}
