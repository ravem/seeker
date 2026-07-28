package com.seeker.app.core.model

/**
 * Un punto di rilevamento sulla mappa con i risultati della scansione Wi-Fi.
 */
data class HeatmapScanPoint(
    val x: Float,                       // Posizione X sulla griglia (0..gridWidth)
    val y: Float,                       // Posizione Y sulla griglia (0..gridHeight)
    val readings: List<BssidReading>,   // Letture BSSID in questo punto
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Lettura di un BSSID in un punto.
 */
data class BssidReading(
    val bssid: String,
    val ssid: String,
    val rssi: Int,          // dBm (es. -65)
    val frequencyMhz: Int
)

/**
 * Una sessione di rilevamento heatmap.
 */
data class HeatmapSession(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val gridWidth: Int = 10,            // Larghezza griglia in celle
    val gridHeight: Int = 10,           // Altezza griglia in celle
    val cellSizeMeters: Float = 1f,     // Dimensione cella in metri (approssimativa)
    val scanPoints: List<HeatmapScanPoint> = emptyList(),
    val selectedBssid: String? = null,  // BSSID attualmente selezionato per la visualizzazione
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Dati interpolati per una cella della griglia.
 */
data class CellSignal(
    val x: Int,     // Coordinata cella X
    val y: Int,     // Coordinata cella Y
    val rssi: Double // Valore RSSI stimato
)

/**
 * Configurazione visualizzazione heatmap.
 */
data class HeatmapViewConfig(
    val minRssi: Int = -90,   // Rosso (peggiore)
    val maxRssi: Int = -30    // Verde (migliore)
)
