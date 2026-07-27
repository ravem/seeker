package com.seeker.app.ui.discovery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.seeker.app.core.model.LanDevice
import com.seeker.app.ui.components.DetailRow
import com.seeker.app.ui.theme.ChipShape
import com.seeker.app.ui.theme.SignalColors

private val KNOWN_AP_VENDORS = listOf(
    "Cisco", "Meraki", "Ubiquiti", "TP-Link", "TP-LINK",
    "Netgear", "ASUSTek", "ASUS", "D-Link", "Huawei",
    "Zyxel", "MikroTik", "Aruba", "Ruckus", "EnGenius", "Grandstream"
)

/**
 * Verifica se il vendor è un produttore noto di AP/router,
 * usando matching parziale per gestire i nomi completi del database IEEE.
 */
private fun isApVendor(vendor: String?): Boolean {
    if (vendor == null) return false
    val v = vendor.lowercase()
    return KNOWN_AP_VENDORS.any { known ->
        v.contains(known.lowercase())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanDiscoveryScreen(
    onSettings: () -> Unit = {},
    viewModel: LanDiscoveryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDevice = remember { mutableStateOf<LanDevice?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Dispositivi Rete", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            actions = {
                IconButton(onClick = { viewModel.startScan() }) {
                    Icon(if (uiState.isScanning) Icons.Default.Refresh else Icons.Default.PlayArrow,
                        contentDescription = "Scansiona")
                }
                IconButton(onClick = onSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Impostazioni")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )

        // Rete corrente (Wi-Fi o Ethernet)
        uiState.currentNetwork?.let { net ->
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = if (net.isWifi) "📶" else "🔌"
                Text("$icon  ", style = MaterialTheme.typography.labelSmall)
                Text(net.displayName, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                if (net.isWifi) {
                    Spacer(Modifier.width(6.dp))
                    val bandColor = when (net.band) {
                        com.seeker.app.core.model.WifiBand.GHZ_2_4 -> com.seeker.app.ui.theme.BandColors.ghz24
                        com.seeker.app.core.model.WifiBand.GHZ_5 -> com.seeker.app.ui.theme.BandColors.ghz5
                        com.seeker.app.core.model.WifiBand.GHZ_6 -> com.seeker.app.ui.theme.BandColors.ghz6
                    }
                    Text("(${net.band.label})", style = MaterialTheme.typography.labelSmall, color = bandColor)
                }
            }
        }

        if (uiState.isScanning) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            uiState.scanMessage?.let { msg ->
                Text(msg, style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Port scan progress
        AnimatedVisibility(
            visible = uiState.portScanProgress != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            uiState.portScanProgress?.let { progress ->
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            progress.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    LinearProgressIndicator(
                        progress = { progress.progressPercent },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        val errorMsg = uiState.error
        if (errorMsg != null) {
            Text(errorMsg, style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
        }

        if (uiState.devices.isEmpty() && !uiState.isScanning) {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔍", style = MaterialTheme.typography.displayMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Nessun dispositivo trovato", style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(uiState.devices, key = { it.ipAddress }) { device ->
                    DeviceCompactRow(
                        device = device,
                        gatewayIp = uiState.gatewayIp,
                        onClick = { selectedDevice.value = device }
                    )
                }
            }
        }
    }

    val sel = selectedDevice.value
    if (sel != null) {
        DeviceDetailSheet(
            device = sel,
            onPortScan = { viewModel.scanPorts(sel) },
            onDismiss = { selectedDevice.value = null }
        )
    }
}

@Composable
private fun DeviceCompactRow(
    device: LanDevice,
    gatewayIp: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mac = device.macAddress
    val macDisplay = if (mac.isNotBlank() && mac != "00:00:00:00:00:00") mac else null
    val isGateway = gatewayIp != null && device.ipAddress == gatewayIp
    val isAp = isApVendor(device.vendor)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp)
    ) {
        // Riga 1: nome hostname o IP + tags
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = device.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (isGateway) {
                Spacer(Modifier.width(4.dp))
                TagChip("GW")
            } else if (isAp) {
                Spacer(Modifier.width(4.dp))
                TagChip("AP")
            }
        }

        // Riga 2: MAC + vendor (se disponibili)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (macDisplay != null) {
                Text(macDisplay, style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (device.vendor != null && !isGateway) {
                if (macDisplay != null) Spacer(Modifier.width(8.dp))
                Text(device.vendor, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun TagChip(label: String) {
    val color = when (label) {
        "GW" -> MaterialTheme.colorScheme.primary
        "AP" -> SignalColors.fair
        else -> MaterialTheme.colorScheme.secondary
    }
    androidx.compose.material3.Surface(
        shape = ChipShape,
        color = color.copy(alpha = 0.12f)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold,
            color = color, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceDetailSheet(
    device: LanDevice,
    onPortScan: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
            // Intestazione: hostname/IP
            Text(device.displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface)
            if (device.hostname != null && device.dnsName != null && device.hostname != device.dnsName) {
                Text("DNS: ${device.dnsName}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(2.dp))
            // IP piccolo sotto il nome
            Text(device.ipAddress, style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            // MAC
            val mac = device.macAddress
            if (mac.isNotBlank() && mac != "00:00:00:00:00:00") {
                DetailRow(label = "MAC", value = mac)
            }

            // Vendor
            if (device.vendor != null) {
                DetailRow(label = "Produttore", value = device.vendor)
            }

            // Hostname
            if (device.hostname != null && device.hostname != device.ipAddress) {
                DetailRow(label = "Hostname", value = device.hostname)
            }

            // DNS Name
            if (device.dnsName != null && device.dnsName != device.hostname && device.dnsName != device.ipAddress) {
                DetailRow(label = "DNS", value = device.dnsName)
            }

            // SNMP Info
            if (device.snmpInfo != null) {
                val info = device.snmpInfo
                if (info.systemName != null) {
                    DetailRow(label = "Nome SNMP", value = info.systemName)
                }
                if (info.systemDescription != null) {
                    DetailRow(label = "Descrizione", value = info.systemDescription)
                }
                if (info.systemLocation != null) {
                    DetailRow(label = "Ubicazione", value = info.systemLocation)
                }
                if (info.systemContact != null) {
                    DetailRow(label = "Contatto", value = info.systemContact)
                }
                if (info.uptime != null) {
                    val days = info.uptime / (100 * 86400)
                    val hours = (info.uptime % (100 * 86400)) / (100 * 3600)
                    val uptimeStr = if (days > 0) "${days}g ${hours}h" else "${hours}h"
                    DetailRow(label = "Uptime", value = uptimeStr)
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            // Porte ben note
            val openPorts = device.ports.filter { it.isOpen }
            Text("Porte aperte:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            if (openPorts.isNotEmpty()) {
                openPorts.sortedBy { it.port }.forEach { port ->
                    DetailRow(
                        label = "${port.port}/${port.transport}",
                        value = port.service,
                        valueColor = SignalColors.excellent
                    )
                }
            } else {
                Text("Nessuna porta aperta rilevata", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(16.dp))
            TextButton(
                onClick = onPortScan,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Scansione porte completa (TCP + UDP)", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
