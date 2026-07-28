package com.seeker.app.ui.heatmap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.seeker.app.core.model.CellSignal
import com.seeker.app.core.model.HeatmapScanPoint
import com.seeker.app.core.util.HeatmapEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeatmapScreen(
    onBack: () -> Unit,
    viewModel: HeatmapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showHelp by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Heatmap Wi-Fi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                }
            },
            actions = {
                IconButton(onClick = { showHelp = true }) {
                    Icon(Icons.Default.Info, contentDescription = "Aiuto")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )

        // Dialog aiuto
        if (showHelp) {
            AlertDialog(
                onDismissRequest = { showHelp = false },
                title = { Text("Come funziona la Heatmap") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("1. Imposta la griglia: scegli larghezza e altezza in celle. Ogni cella corrisponde a una posizione sul piano fisico (circa 1 metro). Ad esempio, una stanza di 10x10 m va impostata come griglia 10x10 celle.")
                        Text("2. Posizionati in un punto dell'area da mappare e tocca sulla griglia nella posizione corrispondente. L'app esegue una scansione Wi-Fi e registra i segnali di tutte le reti visibili.")
                        Text("3. Ripeti in punti diversi dell'area. Più punti inserisci, più precisa sarà la heatmap.")
                        Text("4. La heatmap viene generata automaticamente: verde = segnale forte (-30 dBm), giallo = medio, rosso = debole (-90 dBm).")
                        Text("5. Puoi selezionare quale rete visualizzare dal menu 'Rete visualizzata'.")
                        Text("6. Usa 'Nuovo' per ricominciare, 'Annulla' per rimuovere l'ultimo punto, 'Salva' per conservare la sessione.")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showHelp = false }) {
                        Text("OK")
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Controlli griglia
            GridControls(
                gridWidth = uiState.session.gridWidth,
                gridHeight = uiState.session.gridHeight,
                onGridWidthChange = { viewModel.setGridSize(it, uiState.session.gridHeight) },
                onGridHeightChange = { viewModel.setGridSize(uiState.session.gridWidth, it) },
                onNewSession = { viewModel.newSession() }
            )

            // Selezione BSSID
            BssidSelector(
                bssids = uiState.availableBssids,
                selectedBssid = uiState.selectedBssid,
                onSelectBssid = { viewModel.selectBssid(it) }
            )

            // Canvas heatmap
            HeatmapCanvas(
                session = uiState.session,
                cells = uiState.cells,
                gridWidth = uiState.session.gridWidth,
                gridHeight = uiState.session.gridHeight,
                onTap = { x, y -> viewModel.addScanPoint(x, y) }
            )

            // Informazioni
            if (uiState.isScanning) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text("Scansione Wi-Fi in corso...", style = MaterialTheme.typography.bodySmall)
            }

            uiState.message?.let { msg ->
                Text(msg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }

            uiState.lastScanRssi?.let { rssi ->
                Text(
                    "Ultimo segnale: $rssi dBm (${rssiToDescription(rssi)})",
                    style = MaterialTheme.typography.bodySmall,
                    color = rssiColor(rssi)
                )
            }

            // Pulsanti azione
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.removeLastPoint() },
                    enabled = uiState.session.scanPoints.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Annulla ultimo punto")
                }
                Button(
                    onClick = { viewModel.saveSession() },
                    enabled = uiState.session.scanPoints.size >= 2,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Salva sessione")
                }
            }

            // Legenda punti rilevati
            if (uiState.session.scanPoints.isNotEmpty()) {
                Text(
                    "Punti rilevati: ${uiState.session.scanPoints.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun GridControls(
    gridWidth: Int,
    gridHeight: Int,
    onGridWidthChange: (Int) -> Unit,
    onGridHeightChange: (Int) -> Unit,
    onNewSession: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Griglia", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f))
                TextButton(onClick = onNewSession) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Nuovo")
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("Larghezza: $gridWidth celle", style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = gridWidth.toFloat(),
                        onValueChange = { onGridWidthChange(it.toInt()) },
                        valueRange = 3f..30f,
                        steps = 26,
                        modifier = Modifier.width(120.dp)
                    )
                }
                Column {
                    Text("Altezza: $gridHeight celle", style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = gridHeight.toFloat(),
                        onValueChange = { onGridHeightChange(it.toInt()) },
                        valueRange = 3f..30f,
                        steps = 26,
                        modifier = Modifier.width(120.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BssidSelector(
    bssids: List<BssidInfo>,
    selectedBssid: String?,
    onSelectBssid: (String?) -> Unit
) {
    if (bssids.isEmpty()) return

    var expanded by remember { mutableStateOf(false) }
    val selected = bssids.find { it.bssid == selectedBssid }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Rete visualizzata", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Box {
                OutlinedButton(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = selected?.let { "${it.ssid} (${it.bssid.take(17)})" } ?: "Tutte le reti",
                        maxLines = 1
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Tutte le reti") },
                        onClick = { onSelectBssid(null); expanded = false }
                    )
                    bssids.forEach { info ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(info.ssid, style = MaterialTheme.typography.bodyMedium)
                                    Text("${info.bssid.take(17)}  (${info.samplesCount} punti)", style = MaterialTheme.typography.labelSmall)
                                }
                            },
                            onClick = { onSelectBssid(info.bssid); expanded = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapCanvas(
    session: com.seeker.app.core.model.HeatmapSession,
    cells: List<CellSignal>,
    gridWidth: Int,
    gridHeight: Int,
    onTap: (Float, Float) -> Unit
) {
    val cellSize = 32.dp // Dimensione di ogni cella nel canvas
    val canvasWidth = cellSize * gridWidth
    val canvasHeight = cellSize * gridHeight

    Card(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(canvasWidth, canvasHeight)
                    .background(Color(0xFFF5F5F5))
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val cellX = (offset.x / cellSize.toPx()).coerceIn(0f, gridWidth - 0.01f)
                            val cellY = (offset.y / cellSize.toPx()).coerceIn(0f, gridHeight - 0.01f)
                            onTap(cellX, cellY)
                        }
                    }
            ) {
                val cellSizePx = cellSize.toPx()
                drawGrid(gridWidth, gridHeight, cellSizePx)

                // Disegna heatmap interpolata
                if (cells.isNotEmpty()) {
                    drawHeatmap(cells, cellSizePx)
                }

                // Disegna punti di rilevamento
                drawScanPoints(session.scanPoints, cellSizePx, gridWidth, gridHeight)
            }
        }
    }
}

private fun DrawScope.drawGrid(width: Int, height: Int, cellSize: Float) {
    val gridColor = Color(0xFFDDDDDD)
    for (x in 0..width) {
        drawLine(gridColor, Offset(x * cellSize, 0f), Offset(x * cellSize, height * cellSize), strokeWidth = 1f)
    }
    for (y in 0..height) {
        drawLine(gridColor, Offset(0f, y * cellSize), Offset(width * cellSize, y * cellSize), strokeWidth = 1f)
    }
}

private fun DrawScope.drawHeatmap(cells: List<CellSignal>, cellSize: Float) {
    // Trova il range RSSI per normalizzare
    val minRssi = -90
    val maxRssi = -30

    for (cell in cells) {
        val colorInt = HeatmapEngine.rssiToColor(cell.rssi, minRssi, maxRssi)
        val alpha = 0.6f // trasparenza per vedere la griglia sotto
        val color = Color(colorInt).copy(alpha = alpha)

        drawRect(
            color = color,
            topLeft = Offset(cell.x * cellSize, cell.y * cellSize),
            size = Size(cellSize, cellSize)
        )
    }
}

private fun DrawScope.drawScanPoints(points: List<HeatmapScanPoint>, cellSize: Float, gridWidth: Int, gridHeight: Int) {
    val textPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = cellSize * 0.4f
        textAlign = android.graphics.Paint.Align.CENTER
        isFakeBoldText = true
    }
    for (point in points) {
        val cx = point.x * cellSize
        val cy = point.y * cellSize
        drawCircle(Color.White, radius = cellSize * 0.4f, center = Offset(cx, cy))
        drawCircle(Color(0xFF1976D2), radius = cellSize * 0.25f, center = Offset(cx, cy))
        val idx = points.indexOf(point) + 1
        drawContext.canvas.nativeCanvas.drawText("$idx", cx - 4f, cy + 4f, textPaint)
    }
}

private fun rssiToDescription(rssi: Int): String = when {
    rssi >= -50 -> "Eccellente"
    rssi >= -60 -> "Buono"
    rssi >= -70 -> "Discreto"
    rssi >= -80 -> "Debole"
    else -> "Molto debole"
}

private fun rssiColor(rssi: Int): Color = when {
    rssi >= -50 -> Color(0xFF4CAF50)
    rssi >= -60 -> Color(0xFF8BC34A)
    rssi >= -70 -> Color(0xFFFFC107)
    rssi >= -80 -> Color(0xFFFF9800)
    else -> Color(0xFFF44336)
}
