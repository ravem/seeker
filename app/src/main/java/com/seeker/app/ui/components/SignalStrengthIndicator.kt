package com.seeker.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seeker.app.core.model.SignalLevel
import com.seeker.app.ui.theme.SignalColors

/**
 * Indicatore visivo della potenza del segnale Wi-Fi.
 * Disegna le classiche "barre" del segnale Wi-Fi con colori dinamici.
 */
@Composable
fun SignalStrengthIndicator(
    signalLevel: SignalLevel,
    signalDbm: Int,
    modifier: Modifier = Modifier,
    showDbm: Boolean = true,
    showLabel: Boolean = true,
    animated: Boolean = true
) {
    val targetProgress = when (signalLevel) {
        SignalLevel.EXCELLENT -> 1f
        SignalLevel.GOOD -> 0.8f
        SignalLevel.FAIR -> 0.6f
        SignalLevel.WEAK -> 0.4f
        SignalLevel.VERY_WEAK -> 0.2f
    }

    val targetColor = when (signalLevel) {
        SignalLevel.EXCELLENT -> SignalColors.excellent
        SignalLevel.GOOD -> SignalColors.good
        SignalLevel.FAIR -> SignalColors.fair
        SignalLevel.WEAK -> SignalColors.weak
        SignalLevel.VERY_WEAK -> SignalColors.veryWeak
    }

    val color by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(500),
        label = "signalColor"
    )

    val progress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(300),
        label = "signalProgress"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Icona Wi-Fi a barre personalizzata
        Canvas(modifier = Modifier.size(32.dp)) {
            val barWidth = size.width / 5f
            val barGap = barWidth * 0.3f
            val startX = 0f

            for (i in 0..3) {
                val barActive = (i + 1) <= (progress * 4)
                val barHeight = size.height * (0.25f + i * 0.2f)
                val barY = size.height - barHeight

                drawRoundRect(
                    color = if (barActive) color else Color.Gray.copy(alpha = 0.3f),
                    topLeft = Offset(startX + i * (barWidth + barGap), barY),
                    size = Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f)
                )
            }
        }

        if (showDbm || showLabel) {
            Spacer(modifier = Modifier.width(8.dp))

            Column {
                if (showDbm) {
                    Text(
                        text = "${signalDbm} dBm",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
                if (showLabel) {
                    Text(
                        text = when (signalLevel) {
                            SignalLevel.EXCELLENT -> "Eccellente"
                            SignalLevel.GOOD -> "Buono"
                            SignalLevel.FAIR -> "Discreto"
                            SignalLevel.WEAK -> "Debole"
                            SignalLevel.VERY_WEAK -> "Molto Debole"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
