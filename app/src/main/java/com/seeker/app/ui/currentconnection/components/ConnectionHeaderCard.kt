package com.seeker.app.ui.currentconnection.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.seeker.app.core.model.ConnectedNetwork
import com.seeker.app.core.model.TransportType
import com.seeker.app.ui.components.CardHeader
import com.seeker.app.ui.components.WifiCard

/**
 * Header card per la connessione attuale.
 * Mostra SSID, BSSID e stato connessione.
 */
@Composable
fun ConnectionHeaderCard(
    network: ConnectedNetwork?,
    modifier: Modifier = Modifier
) {
    WifiCard(modifier = modifier.fillMaxWidth()) {
        if (network != null) {
            val icon = if (network.isWifi) Icons.Default.Wifi else Icons.Default.SettingsEthernet
            val title = network.displayName

            CardHeader(
                icon = icon,
                title = title,
                iconTint = MaterialTheme.colorScheme.primary
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
            ) {
                if (network.isWifi) {
                    // BSSID
                    DetailRow(label = "BSSID", value = network.bssid.ifBlank { "N/D" })

                    // Vendor AP
                    if (network.apVendor != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        DetailRow(label = "Produttore", value = network.apVendor)
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Banda e Ampiezza canale
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Banda",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(80.dp)
                        )
                        BandChip(band = network.band)
                        Spacer(modifier = Modifier.width(6.dp))
                        ChannelWidthChip(channelWidthMhz = network.channelWidthMhz)
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    DetailRow(label = "Standard", value = network.wifiStandard?.displayName ?: "N/D")

                    if (network.txLinkSpeedMbps != null || network.rxLinkSpeedMbps != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        DetailRow(
                            label = "TX/RX",
                            value = "${network.txLinkSpeedMbps ?: "?"} / ${network.rxLinkSpeedMbps ?: "?"} Mbps"
                        )
                    }
                } else {
                    // Ethernet: mostra tipo connessione e interfaccia
                    DetailRow(label = "Tipo", value = "Ethernet (USB-C)")
                    if (network.interfaceName != null) {
                        DetailRow(label = "Interfaccia", value = network.interfaceName)
                    }
                }
            }
        } else {
            // Stato non connesso
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Non connesso a una rete Wi-Fi",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BandChip(
    band: com.seeker.app.core.model.WifiBand,
    modifier: Modifier = Modifier
) {
    val color = when (band) {
        com.seeker.app.core.model.WifiBand.GHZ_2_4 -> com.seeker.app.ui.theme.BandColors.ghz24
        com.seeker.app.core.model.WifiBand.GHZ_5 -> com.seeker.app.ui.theme.BandColors.ghz5
        com.seeker.app.core.model.WifiBand.GHZ_6 -> com.seeker.app.ui.theme.BandColors.ghz6
    }

    androidx.compose.material3.Surface(
        modifier = modifier,
        shape = com.seeker.app.ui.theme.ChipShape,
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = band.label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun ChannelWidthChip(
    channelWidthMhz: Int,
    modifier: Modifier = Modifier
) {
    val (label, color) = when (channelWidthMhz) {
        20 -> "Ch 20 MHz" to androidx.compose.ui.graphics.Color(0xFF90A4AE)
        40 -> "Ch 40 MHz" to androidx.compose.ui.graphics.Color(0xFF80CBC4)
        80 -> "Ch 80 MHz" to androidx.compose.ui.graphics.Color(0xFF81D4FA)
        160 -> "Ch 160 MHz" to androidx.compose.ui.graphics.Color(0xFFCE93D8)
        320 -> "Ch 320 MHz" to androidx.compose.ui.graphics.Color(0xFFEF9A9A)
        else -> "Ch ${channelWidthMhz} MHz" to androidx.compose.ui.graphics.Color(0xFFBDBDBD)
    }

    androidx.compose.material3.Surface(
        modifier = modifier,
        shape = com.seeker.app.ui.theme.ChipShape,
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
