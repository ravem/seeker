package com.seeker.app.core.util

import com.seeker.app.core.model.BssidReading
import com.seeker.app.core.model.CellSignal
import com.seeker.app.core.model.HeatmapScanPoint
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Motore di interpolazione per generare heatmap Wi-Fi.
 *
 * Usa Inverse Distance Weighting (IDW) per stimare il segnale
 * in ogni cella della griglia a partire dai punti di rilevamento.
 */
object HeatmapEngine {

    /**
     * Interpola i valori RSSI per ogni cella della griglia,
     * filtrando per un BSSID specifico.
     *
     * @param points Punti di rilevamento
     * @param bssid  BSSID da visualizzare (null = media di tutti)
     * @param gridWidth  Numero di celle in larghezza
     * @param gridHeight Numero di celle in altezza
     * @param power  Potenza dell'IDW (2 = default, >2 = più influenza ai vicini)
     * @return Lista di CellSignal per ogni cella della griglia
     */
    fun interpolate(
        points: List<HeatmapScanPoint>,
        bssid: String? = null,
        gridWidth: Int = 10,
        gridHeight: Int = 10,
        power: Double = 2.0
    ): List<CellSignal> {
        if (points.isEmpty()) return emptyList()

        val signals = mutableListOf<CellSignal>()

        for (cx in 0 until gridWidth) {
            for (cy in 0 until gridHeight) {
                // Centro della cella
                val cellX = cx + 0.5f
                val cellY = cy + 0.5f

                val rssi = interpolateCell(points, bssid, cellX, cellY, power)
                if (rssi != null) {
                    signals.add(CellSignal(x = cx, y = cy, rssi = rssi))
                }
            }
        }

        return signals
    }

    /**
     * Interpola il valore RSSI per una singola cella usando IDW.
     */
    private fun interpolateCell(
        points: List<HeatmapScanPoint>,
        bssid: String?,
        cellX: Float,
        cellY: Float,
        power: Double
    ): Double? {
        var weightedSum = 0.0
        var weightSum = 0.0
        val minDistance = 0.01f // Evita divisione per zero

        for (point in points) {
            // Distanza euclidea
            val dx = point.x - cellX
            val dy = point.y - cellY
            val distance = sqrt((dx * dx + dy * dy).toDouble()).coerceAtLeast(minDistance.toDouble())

            // RSSI medio per questo punto (per il BSSID selezionato o tutti)
            val rssi = getRssi(point, bssid) ?: continue

            // Peso = 1 / distanza^power
            val weight = 1.0 / distance.pow(power)

            weightedSum += rssi.toDouble() * weight
            weightSum += weight
        }

        return if (weightSum > 0) weightedSum / weightSum else null
    }

    /**
     * Ottiene l'RSSI medio per un punto, filtrando per BSSID.
     */
    private fun getRssi(point: HeatmapScanPoint, bssid: String?): Int? {
        val readings = if (bssid != null) {
            point.readings.filter { it.bssid == bssid }
        } else {
            point.readings
        }
        if (readings.isEmpty()) return null
        return readings.map { it.rssi }.average().toInt()
    }

    /**
     * Converte un valore RSSI in un colore ARGB.
     * Rosso (cattivo) -> Giallo -> Verde (buono)
     */
    fun rssiToColor(rssi: Double, minRssi: Int = -90, maxRssi: Int = -30): Int {
        val clamped = rssi.coerceIn(minRssi.toDouble(), maxRssi.toDouble())
        val t = ((clamped - minRssi) / (maxRssi - minRssi)).coerceIn(0.0, 1.0)

        // HSL: da rosso (0) a verde (120) passando per giallo (60)
        val hue = (t * 120).toFloat()
        val saturation = 0.8f
        val lightness = 0.5f

        return hslToArgb(hue, saturation, lightness)
    }

    /**
     * Converte HSL in ARGB.
     */
    private fun hslToArgb(hue: Float, saturation: Float, lightness: Float): Int {
        val c = (1.0 - kotlin.math.abs(2.0 * lightness - 1.0)) * saturation
        val x = c * (1.0 - kotlin.math.abs((hue / 60.0) % 2.0 - 1.0))
        val m = lightness - c / 2.0

        val (r, g, b) = when {
            hue < 60 -> Triple(c, x, 0.0)
            hue < 120 -> Triple(x, c, 0.0)
            hue < 180 -> Triple(0.0, c, x)
            hue < 240 -> Triple(0.0, x, c)
            hue < 300 -> Triple(x, 0.0, c)
            else -> Triple(c, 0.0, x)
        }

        return (255 shl 24) or
                (((r + m) * 255).toInt().coerceIn(0, 255) shl 16) or
                (((g + m) * 255).toInt().coerceIn(0, 255) shl 8) or
                ((b + m) * 255).toInt().coerceIn(0, 255)
    }
}
