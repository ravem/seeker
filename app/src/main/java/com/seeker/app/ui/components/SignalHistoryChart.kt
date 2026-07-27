package com.seeker.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seeker.app.ui.theme.SignalColors
import kotlin.math.roundToInt

/**
 * Storico del segnale Wi-Fi: coppie (tempo relativo in ms, dBm).
 */
data class SignalSample(
    val timestampMs: Long,
    val dbm: Int
)

/**
 * Grafico a linee che mostra l'andamento del segnale Wi-Fi negli ultimi secondi.
 * Disegna una curva cubica (bezier) con gradiente sotto la linea.
 */
@Composable
fun SignalHistoryChart(
    samples: List<SignalSample>,
    modifier: Modifier = Modifier,
    maxDurationMs: Long = 60_000L,  // 60 secondi di storico
    showLabels: Boolean = true
) {
    if (samples.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        val chartWidth = size.width
        val chartHeight = size.height
        val padding = 8.dp.toPx()
        val leftPadding = if (showLabels) 40.dp.toPx() else padding
        val bottomPadding = if (showLabels) 24.dp.toPx() else padding

        val plotLeft = leftPadding
        val plotTop = padding
        val plotRight = chartWidth - padding
        val plotBottom = chartHeight - bottomPadding
        val plotWidth = plotRight - plotLeft
        val plotHeight = plotBottom - plotTop

        if (plotWidth <= 0 || plotHeight <= 0) return@Canvas

        // Determina il range temporale
        val now = samples.last().timestampMs
        val startTime = now - maxDurationMs
        val visibleSamples = samples.filter { it.timestampMs >= startTime }

        if (visibleSamples.size < 2) {
            // Non abbastanza punti
            return@Canvas
        }

        // Range dBm (da -30 a -100)
        val minDbm = -100
        val maxDbm = -30
        val dbmRange = (maxDbm - minDbm).toFloat()

        // Mappa campioni a coordinate
        val points = visibleSamples.map { sample ->
            val x = plotLeft + ((sample.timestampMs - startTime).toFloat() / maxDurationMs) * plotWidth
            val y = plotBottom - ((sample.dbm - minDbm).toFloat() / dbmRange) * plotHeight
            Offset(x, y)
        }

        // Colore linea basato sull'ultimo valore
        val lastDbm = visibleSamples.last().dbm
        val lineColor = when {
            lastDbm >= -50 -> SignalColors.excellent
            lastDbm >= -60 -> SignalColors.good
            lastDbm >= -70 -> SignalColors.fair
            lastDbm >= -80 -> SignalColors.weak
            else -> SignalColors.veryWeak
        }

        // ── Linee di griglia orizzontali ──
        val gridColor = Color.Gray.copy(alpha = 0.12f)
        for (dbm in (-90..-30 step 20)) {
            val y = plotBottom - ((dbm - minDbm).toFloat() / dbmRange) * plotHeight
            drawLine(
                color = gridColor,
                start = Offset(plotLeft, y),
                end = Offset(plotRight, y),
                strokeWidth = 1.dp.toPx()
            )
            if (showLabels) {
                val textResult = textMeasurer.measure(
                    text = "${dbm}",
                    style = TextStyle(fontSize = 9.sp, color = Color.Gray.copy(alpha = 0.5f))
                )
                drawText(
                    textLayoutResult = textResult,
                    topLeft = Offset(
                        plotLeft - textResult.size.width - 4.dp.toPx(),
                        y - textResult.size.height / 2f
                    )
                )
            }
        }

        // ── Curva (Path con cubic Bezier) ──
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val curr = points[i]
                // Calcola punti di controllo per curva cubica liscia
                val controlX1 = (prev.x + curr.x) / 2f
                val controlX2 = controlX1
                val controlY1 = prev.y
                val controlY2 = curr.y
                cubicTo(controlX1, controlY1, controlX2, controlY2, curr.x, curr.y)
            }
        }

        // Ombra della curva
        drawPath(
            path = path,
            color = lineColor.copy(alpha = 0.6f),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // ── Gradiente sotto la curva ──
        val fillPath = Path().apply {
            addPath(path)
            lineTo(points.last().x, plotBottom)
            lineTo(points.first().x, plotBottom)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.25f),
                    lineColor.copy(alpha = 0.0f)
                ),
                endY = plotBottom
            )
        )

        // ── Etichetta ultimo valore ──
        if (showLabels) {
            val lastPoint = points.last()
            val labelText = "${lastDbm} dBm"
            val labelStyle = TextStyle(
                fontSize = 11.sp,
                color = lineColor
            )
            val labelResult = textMeasurer.measure(labelText, labelStyle)
            val labelX = (lastPoint.x - labelResult.size.width / 2f)
                .coerceIn(plotLeft, plotRight - labelResult.size.width)
            drawText(
                textLayoutResult = labelResult,
                topLeft = Offset(labelX, plotBottom + 6.dp.toPx())
            )
        }
    }
}
