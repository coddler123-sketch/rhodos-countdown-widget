package com.example.rhodoswidget

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
internal fun TransitPdfScreen(
    padding: PaddingValues,
    document: TransitDocument,
    onBack: () -> Unit,
    onOpenSource: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var file by remember(document.id) { mutableStateOf<File?>(null) }
    var pageCount by remember(document.id) { mutableIntStateOf(0) }
    var pageIndex by remember(document.id) { mutableIntStateOf(0) }
    var isLoading by remember(document.id) { mutableStateOf(false) }
    var failed by remember(document.id) { mutableStateOf(false) }
    val hasGermanTimetable = document.id in setOf("ktel_kolymbia", "ktel_lindos")
    var showGermanTimetable by remember(document.id) { mutableStateOf(hasGermanTimetable) }

    val load: () -> Unit = {
        if (!isLoading) {
            scope.launch {
                isLoading = true
                failed = false
                val loaded = withContext(Dispatchers.IO) {
                    LiveTravelRepository.downloadPdf(context.applicationContext, document)
                }
                file = loaded
                pageCount = loaded?.let(::pdfPageCount) ?: 0
                failed = loaded == null || pageCount == 0
                isLoading = false
            }
        }
    }
    BackHandler(onBack = onBack)
    LaunchedEffect(document.id) {
        if (!hasGermanTimetable) load()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF142E34), Color(0xFF0D1113))))
            .padding(padding)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.travel_back), color = HomeAccent)
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(R.string.travel_pdf_title),
                color = HomeAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = document.title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Montserrat
        )
        Text(
            text = stringResource(R.string.travel_timetable_operator, document.operator),
            color = Color(0x99FFFFFF),
            fontSize = 10.sp,
            fontFamily = Montserrat
        )
        if (hasGermanTimetable) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showGermanTimetable = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showGermanTimetable) HomeAccent else Color(0x1FFFFFFF),
                        contentColor = if (showGermanTimetable) Color(0xFF102126) else Color.White
                    )
                ) {
                    Text(stringResource(R.string.travel_german_timetable_tab), fontSize = 10.sp)
                }
                Button(
                    onClick = {
                        showGermanTimetable = false
                        if (file == null && !isLoading) load()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!showGermanTimetable) HomeAccent else Color(0x1FFFFFFF),
                        contentColor = if (!showGermanTimetable) Color(0xFF102126) else Color.White
                    )
                ) {
                    Text(stringResource(R.string.travel_original_pdf_tab), fontSize = 10.sp)
                }
            }
        }
        when {
            document.id == "ktel_kolymbia" && showGermanTimetable -> KolymbiaGermanTimetable(
                document = document,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 10.dp)
            )
            hasGermanTimetable && showGermanTimetable -> LindosGermanTimetable(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 10.dp)
            )
            isLoading -> PdfMessage(R.string.travel_pdf_loading)
            failed || file == null -> PdfError(onRetry = load, onOpenSource = onOpenSource)
            else -> PdfPageViewer(
                file = file ?: return@Column,
                operator = document.operator,
                pageIndex = pageIndex,
                pageCount = pageCount,
                onPrevious = { if (pageIndex > 0) pageIndex-- },
                onNext = { if (pageIndex + 1 < pageCount) pageIndex++ }
            )
        }
    }
}

@Composable
private fun ColumnScope.PdfPageViewer(
    file: File,
    operator: String,
    pageIndex: Int,
    pageCount: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    var zoomed by remember(file, pageIndex) { mutableStateOf(false) }
    var bitmap by remember(file, pageIndex, zoomed) { mutableStateOf<Bitmap?>(null) }
    GermanPdfHelp(operator = operator)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(vertical = 10.dp)
    ) {
        val pageWidth = maxWidth
        val widthPx = with(LocalDensity.current) { pageWidth.roundToPx() } * if (zoomed) 2 else 1
        LaunchedEffect(file, pageIndex, widthPx) {
            bitmap = withContext(Dispatchers.IO) { renderPdfPage(file, pageIndex, widthPx) }
        }
        val horizontal = rememberScrollState()
        val vertical = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(horizontal)
                .verticalScroll(vertical),
            contentAlignment = Alignment.TopCenter
        ) {
            bitmap?.let { page ->
                Image(
                    bitmap = page.asImageBitmap(),
                    contentDescription = stringResource(R.string.travel_pdf_page, pageIndex + 1, pageCount),
                    modifier = Modifier.width(pageWidth * if (zoomed) 2f else 1f)
                )
            }
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onPrevious, enabled = pageIndex > 0) {
            Text(stringResource(R.string.travel_pdf_previous), color = HomeAccent)
        }
        Text(
            text = stringResource(R.string.travel_pdf_page, pageIndex + 1, pageCount),
            color = Color.White,
            fontSize = 10.sp
        )
        TextButton(onClick = onNext, enabled = pageIndex + 1 < pageCount) {
            Text(stringResource(R.string.travel_pdf_next), color = HomeAccent)
        }
    }
    TextButton(onClick = { zoomed = !zoomed }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
        Text(
            stringResource(if (zoomed) R.string.travel_pdf_zoom_out else R.string.travel_pdf_zoom_in),
            color = HomeAccent
        )
    }
}

@Composable
private fun GermanPdfHelp(operator: String) {
    var expanded by remember(operator) { mutableStateOf(true) }
    TextButton(onClick = { expanded = !expanded }) {
        Text(
            text = stringResource(
                if (expanded) R.string.travel_pdf_german_help_hide
                else R.string.travel_pdf_german_help_show
            ),
            color = HomeAccent
        )
    }
    if (expanded) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text(
                text = stringResource(R.string.travel_pdf_german_help_title),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Montserrat
            )
            Text(
                text = stringResource(
                    if (operator == "RODA") R.string.travel_pdf_roda_help
                    else R.string.travel_pdf_ktel_help
                ),
                color = Color(0xDDFFFFFF),
                fontSize = 10.sp,
                fontFamily = Montserrat
            )
            Text(
                text = stringResource(R.string.travel_pdf_german_help_note),
                color = Color(0x99FFFFFF),
                fontSize = 9.sp,
                fontFamily = Montserrat
            )
        }
    }
}

@Composable
private fun PdfMessage(textRes: Int) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(textRes), color = Color.White, fontFamily = Montserrat)
    }
}

@Composable
private fun ColumnScope.PdfError(onRetry: () -> Unit, onOpenSource: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.travel_pdf_failed), color = Color.White, fontFamily = Montserrat)
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = HomeAccent, contentColor = Color(0xFF102126))
        ) {
            Text(stringResource(R.string.travel_pdf_retry), fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onOpenSource) {
            Text(stringResource(R.string.travel_open_source), color = HomeAccent)
        }
    }
}

private fun pdfPageCount(file: File): Int = runCatching {
    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
        PdfRenderer(descriptor).use { renderer -> renderer.pageCount }
    }
}.getOrDefault(0)

private fun renderPdfPage(file: File, pageIndex: Int, requestedWidth: Int): Bitmap? = runCatching {
    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
        PdfRenderer(descriptor).use { renderer ->
            renderer.openPage(pageIndex).use { page ->
                val width = requestedWidth.coerceIn(480, 3_200)
                val height = (width.toFloat() / page.width * page.height).toInt().coerceAtLeast(1)
                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                }
            }
        }
    }
}.getOrNull()
