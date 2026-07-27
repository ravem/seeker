package com.seeker.app.ui.currentconnection.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seeker.app.core.model.ConnectedNetwork
import com.seeker.app.core.model.SignalLevel
import com.seeker.app.ui.components.CardHeader
import com.seeker.app.ui.components.SignalHistoryChart
import com.seeker.app.ui.components.SignalSample
import com.seeker.app.ui.components.WifiCard
import com.seeker.app.ui.theme.SignalColors

/**
 * Card con grafico storico del segnale Wi-Fi (ultimi 60 secondi).
 * Mostra l'andamento in tempo reale con una curva liscia.
 */
@Composable
fun SignalGaugeCard(
    network: ConnectedNetwork?,
    modifier: Modifier = Modifier,
    signalHistory: List<SignalSample> = emptyList()
) {
    WifiCard(modifier = modifier.fillMaxWidth()) {
        CardHeader(
            icon = Icons.Default.Timeline,
            title = "Andamento Segnale"
        )

        if (network != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 6.dp)
            ) {
                // Valore corrente in modo compatto
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${network.signalStrengthDbm}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = when (network.signalLevel) {
                            SignalLevel.EXCELLENT -> SignalColors.excellent
                            SignalLevel.GOOD -> SignalColors.good
                            SignalLevel.FAIR -> SignalColors.fair
                            SignalLevel.WEAK -> SignalColors.weak
                            SignalLevel.VERY_WEAK -> SignalColors.veryWeak
                        }
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "dBm",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = when (network.signalLevel) {
                            SignalLevel.EXCELLENT -> "Eccellente"
                            SignalLevel.GOOD -> "Buono"
                            SignalLevel.FAIR -> "Discreto"
                            SignalLevel.WEAK -> "Debole"
                            SignalLevel.VERY_WEAK -> "Molto Debole"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = when (network.signalLevel) {
                            SignalLevel.EXCELLENT -> SignalColors.excellent
                            SignalLevel.GOOD -> SignalColors.good
                            SignalLevel.FAIR -> SignalColors.fair
                            SignalLevel.WEAK -> SignalColors.weak
                            SignalLevel.VERY_WEAK -> SignalColors.veryWeak
                        }
                    )
                }

                // Grafico storico (senza etichette, solo la linea)
                if (signalHistory.isNotEmpty()) {
                    SignalHistoryChart(
                        samples = signalHistory,
                        modifier = Modifier.fillMaxWidth(),
                        showLabels = false
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "N/D",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
