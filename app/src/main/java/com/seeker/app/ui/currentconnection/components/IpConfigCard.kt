package com.seeker.app.ui.currentconnection.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seeker.app.core.model.ConnectedNetwork
import com.seeker.app.ui.components.CardHeader
import com.seeker.app.ui.components.DetailDivider
import com.seeker.app.ui.components.DetailRow
import com.seeker.app.ui.components.WifiCard
import java.net.InetAddress

/**
 * Card con i dettagli della configurazione IP.
 * Mostra IP, rete, subnet, gateway, DNS.
 */
@Composable
fun IpConfigCard(
    network: ConnectedNetwork?,
    modifier: Modifier = Modifier
) {
    WifiCard(modifier = modifier.fillMaxWidth()) {
        CardHeader(
            icon = Icons.Default.Dns,
            title = "Configurazione IP"
        )

        if (network != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                DetailRow(
                    label = "Indirizzo IP",
                    value = network.ipAddress
                )
                DetailDivider()
                DetailRow(
                    label = "Rete",
                    value = calculateNetworkAddress(network.ipAddress, network.subnetMask)
                )
                DetailDivider()
                val cidr = subnetToCidr(network.subnetMask)
                DetailRow(
                    label = "Subnet Mask",
                    value = "${network.subnetMask} (/$cidr)"
                )
                DetailDivider()
                DetailRow(
                    label = "Gateway",
                    value = network.defaultGateway
                )
                DetailDivider()
                DetailRow(
                    label = "DNS Primario",
                    value = network.dnsServers.getOrElse(0) { "N/D" }
                )
                if (network.dnsServers.size > 1) {
                    DetailDivider()
                    DetailRow(
                        label = "DNS Secondario",
                        value = network.dnsServers.getOrElse(1) { "N/D" }
                    )
                }
            }
        } else {
            Text(
                text = "N/D",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Calcola l'indirizzo di rete (network address) a partire da IP e subnet mask.
 * Esempio: 172.24.240.97 & 255.255.248.0 = 172.24.240.0
 */
private fun calculateNetworkAddress(ipAddress: String, subnetMask: String): String {
    return try {
        val ipBytes = InetAddress.getByName(ipAddress).address
        val maskBytes = InetAddress.getByName(subnetMask).address
        val network = ByteArray(4)
        for (i in 0..3) {
            network[i] = (ipBytes[i].toInt() and maskBytes[i].toInt()).toByte()
        }
        InetAddress.getByAddress(network).hostAddress ?: "N/D"
    } catch (_: Exception) {
        "N/D"
    }
}

/**
 * Converte una subnet mask in notazione CIDR.
 * Esempio: 255.255.248.0 → 21
 */
private fun subnetToCidr(subnetMask: String): Int {
    return try {
        val parts = subnetMask.split(".").map { it.toInt() and 0xFF }
        parts.sumOf { Integer.bitCount(it) }
    } catch (_: Exception) { 24 }
}
