package com.seeker.app.ui.currentconnection.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seeker.app.core.model.MobileNetworkInfo
import com.seeker.app.core.model.MobileNetworksState
import com.seeker.app.ui.components.CardHeader
import com.seeker.app.ui.components.DetailDivider
import com.seeker.app.ui.components.WifiCard

@Composable
fun MobileNetworkCard(
    mobileState: MobileNetworksState,
    modifier: Modifier = Modifier
) {
    val sim1 = mobileState.sim1
    val sim2 = mobileState.sim2

    if (sim1 == null && sim2 == null) return

    WifiCard(modifier = modifier.fillMaxWidth()) {
        CardHeader(icon = Icons.Default.SignalCellularAlt, title = "Rete Mobile")

        Column(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 12.dp)) {
            if (sim1 != null) SimInfo(sim = sim1)
        }
    }
}

@Composable
private fun SimInfo(sim: MobileNetworkInfo) {
    val tipoSim = if (sim.isEmbedded) " (eSIM)" else ""
    Text("SIM$tipoSim", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 4.dp))

    MobileDetailRow("Operatore", sim.carrierName ?: "N/D")
    MobileDetailDivider()
    if (sim.signalDbm != null) {
        MobileDetailRow("Segnale", "${sim.signalDbm} dBm")
        MobileDetailDivider()
    }
    if (sim.ipAddress != null) {
        MobileDetailRow("IP", sim.ipAddress)
        MobileDetailDivider()
    }
    MobileDetailRow("Cella", sim.cellId ?: "N/D")
    if (sim.iccid != null) {
        MobileDetailDivider()
        MobileDetailRow("ICCID", "···${sim.iccid.takeLast(4)}")
    } else if (sim.cardId != null && sim.cardId >= 0) {
        MobileDetailDivider()
        MobileDetailRow("Card ID", "#${sim.cardId}")
    }
    if (sim.phoneNumber != null) {
        MobileDetailDivider()
        MobileDetailRow("Numero", sim.phoneNumber)
    }
}

/** Versione più compatta di DetailRow, con colonne più vicine. */
@Composable
private fun MobileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(0.6f))
    }
}

@Composable
private fun MobileDetailDivider() {
    Spacer(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(0.5.dp))
}
