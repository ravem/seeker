package com.seeker.app.ui.scanner

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seeker.app.core.model.AccessPoint
import com.seeker.app.core.model.SecurityProtocol
import com.seeker.app.ui.components.DetailRow
import com.seeker.app.ui.theme.BandColors
import com.seeker.app.ui.theme.SignalColors

/**
 * Bottom sheet con i dettagli di un Access Point.
 * Mostra tutte le informazioni disponibili dalle API Android.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessPointDetailSheet(
    accessPoint: AccessPoint,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // SSID
            Text(
                text = accessPoint.ssid,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (accessPoint.bssid.isNotBlank() && accessPoint.bssid != "00:00:00:00:00:00") {
                Text(
                    text = accessPoint.bssid,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            // Segnale
            DetailRow(
                label = "Segnale",
                value = "${accessPoint.signalStrengthDbm} dBm",
                valueColor = when {
                    accessPoint.signalStrengthDbm >= -50 -> SignalColors.excellent
                    accessPoint.signalStrengthDbm >= -60 -> SignalColors.good
                    accessPoint.signalStrengthDbm >= -70 -> SignalColors.fair
                    accessPoint.signalStrengthDbm >= -80 -> SignalColors.weak
                    else -> SignalColors.veryWeak
                }
            )

            // Sicurezza
            DetailRow(
                label = "Sicurezza",
                value = accessPoint.securityProtocols.joinToString(", ") { it.displayName }
            )

            // Canale
            DetailRow(
                label = "Canale",
                value = if (accessPoint.channel > 0) "${accessPoint.channel}" else "N/D"
            )

            // Ampiezza canale
            DetailRow(
                label = "Ampiezza",
                value = when (accessPoint.channelWidthMhz) {
                    20 -> "20 MHz"
                    40 -> "40 MHz"
                    80 -> "80 MHz"
                    160 -> "160 MHz"
                    320 -> "320 MHz"
                    else -> "${accessPoint.channelWidthMhz} MHz"
                }
            )

            // Frequenza
            DetailRow(
                label = "Frequenza",
                value = "${accessPoint.frequencyMhz} MHz"
            )

            // Banda
            DetailRow(
                label = "Banda",
                value = accessPoint.band.label,
                valueColor = when (accessPoint.band) {
                    com.seeker.app.core.model.WifiBand.GHZ_2_4 -> BandColors.ghz24
                    com.seeker.app.core.model.WifiBand.GHZ_5 -> BandColors.ghz5
                    com.seeker.app.core.model.WifiBand.GHZ_6 -> BandColors.ghz6
                }
            )

            // Standard WiFi
            DetailRow(
                label = "Standard",
                value = accessPoint.wifiStandard?.displayName ?: "N/D"
            )

            // Vendor (da OUI BSSID)
            if (accessPoint.vendor != null) {
                DetailRow(
                    label = "Produttore",
                    value = accessPoint.vendor,
                    valueColor = MaterialTheme.colorScheme.primary
                )
            }

            // Stima distanza (basata sul segnale, molto approssimativa)
            val estimatedDistance = estimateDistance(accessPoint.signalStrengthDbm, accessPoint.frequencyMhz)
            DetailRow(
                label = "Distanza (stimata)",
                value = estimatedDistance
            )

            // Timestamp ultimo rilevamento
            val timeAgo = (System.currentTimeMillis() - accessPoint.timestamp) / 1000
            DetailRow(
                label = "Rilevato",
                value = if (timeAgo < 60) "${timeAgo}s fa" else "${timeAgo / 60}min fa"
            )
        }
    }
}

/**
 * Stima molto approssimativa della distanza in metri basata sul segnale.
 * Formula: d = 10^((TxPower - RSSI) / (20 * n))
 * dove TxPower ≈ -30 dBm (tipico AP), n = path loss exponent ≈ 3 (ambiente indoor)
 */
private fun estimateDistance(dbm: Int, freqMhz: Int): String {
    if (dbm >= -30) return "< 1 m"
    if (dbm <= -90) return "> 50 m"

    // Formula semplificata: ogni -6dB ≈ doppia distanza
    val refDbm = -30  // potenza di riferimento a 1m
    val diff = (refDbm - dbm).coerceAtLeast(1)
    val distanceM = Math.pow(1.2, (diff / 6.0)).toInt()

    return when {
        distanceM < 1 -> "< 1 m"
        distanceM < 10 -> "~${distanceM} m"
        distanceM < 50 -> "~${distanceM} m"
        else -> "> 50 m"
    }
}
