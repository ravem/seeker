package com.seeker.app.ui.controller

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.seeker.app.core.model.ControllerDevice
import com.seeker.app.core.model.ControllerDeviceStatus
import com.seeker.app.core.model.ControllerSource
import com.seeker.app.core.model.ControllerStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControllerDashboardScreen(
    onSettings: () -> Unit,
    viewModel: ControllerDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Polling ogni 10 secondi quando la scherma è visibile
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.startPolling(intervalMs = 10_000L)
        }
    }
    DisposableEffect(lifecycleOwner) {
        onDispose { viewModel.stopPolling() }
    }

    // Bottom sheet dettaglio
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    if (uiState.selectedDevice != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.clearSelection() },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            DeviceDetailSheet(
                device = uiState.selectedDevice!!,
                detail = uiState.deviceDetail
            )
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Menu a tendina per selezione organizzazione
        var showOrgMenu by remember { mutableStateOf(false) }

        TopAppBar(
            title = { Text(uiState.organizationName ?: "Controller", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
            actions = {
                // Pulsante cambio organizzazione (solo se ci sono org)
                if (uiState.merakiOrganizations.isNotEmpty() || uiState.organizationName != null) {
                    Box {
                        IconButton(onClick = { showOrgMenu = !showOrgMenu }) {
                            Icon(Icons.Default.AccountTree, contentDescription = "Cambia organizzazione")
                        }
                        DropdownMenu(
                            expanded = showOrgMenu,
                            onDismissRequest = { showOrgMenu = false }
                        ) {
                            if (uiState.merakiOrganizations.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Caricamento…") },
                                    onClick = {}
                                )
                            } else {
                                uiState.merakiOrganizations.forEach { org ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                org.name,
                                                fontWeight = if (org.name == uiState.organizationName) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            viewModel.selectOrganization(org)
                                            showOrgMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Aggiorna")
                }
                IconButton(onClick = onSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Configurazione")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
        // All'avvio, carica le organizzazioni se Meraki è configurato
        if (uiState.merakiOrganizations.isEmpty() && uiState.organizationName != null) {
            LaunchedEffect(Unit) {
                viewModel.loadMerakiOrganizations()
            }
        }

        if (uiState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item { ControllerStatusCards(statuses = uiState.controllerStatuses) }

            if (uiState.devices.isEmpty() && !uiState.isLoading) {
                item {
                    EmptyState(
                        hasAnyConfigured = uiState.controllerStatuses.any { it.isConfigured },
                        errorMessage = uiState.errorMessage,
                        statuses = uiState.controllerStatuses
                    )
                }
            }

            val grouped = uiState.devices.groupBy { it.controllerSource }
            for ((source, devices) in grouped) {
                item {
                    Text(
                        text = source.label,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                items(devices, key = { "${source.name}_${it.serial ?: it.mac ?: it.name}" }) { device ->
                    ControllerDeviceCard(
                        device = device,
                        onClick = { viewModel.selectDevice(device) }
                    )
                }
            }

            uiState.lastRefresh?.let { time ->
                item {
                    val ago = (System.currentTimeMillis() - time) / 1000
                    val text = when {
                        ago < 60 -> "Aggiornato ${ago}s fa"
                        ago < 3600 -> "Aggiornato ${ago / 60}m fa"
                        else -> "Aggiornato ${ago / 3600}h fa"
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ── Bottom Sheet Dettaglio (con LazyColumn per supportare molti client) ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceDetailSheet(
    device: ControllerDevice,
    detail: DeviceDetailState
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Intestazione
        item {
            Text(
                text = device.displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Modello
        if (device.model != null) {
            item {
                Text(
                    text = device.model,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
        }

        if (detail.isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Caricamento dettagli…", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        } else {
            // MAC
            val mac = detail.mac ?: device.mac
            if (!mac.isNullOrBlank() && mac != "00:00:00:00:00:00") {
                item { DetailRow(label = "MAC", value = mac) }
            }

            // Serial
            val serial = detail.serial ?: device.serial
            if (!serial.isNullOrBlank()) {
                item { DetailRow(label = "Seriale", value = serial) }
            }

            // Firmware
            val fw = detail.firmware ?: device.firmware
            if (!fw.isNullOrBlank()) {
                item { DetailRow(label = "Firmware", value = fw) }
            }

            // IP
            if (device.ipAddress != null && device.ipAddress != "0.0.0.0") {
                item { DetailRow(label = "IP", value = device.ipAddress) }
            }

            // Network
            if (!device.networkName.isNullOrBlank()) {
                item { DetailRow(label = "Rete", value = device.networkName) }
            }

            // Ping Latenza
            if (detail.pingLatencyMs != null) {
                item {
                    DetailRow(
                        label = "Latenza ping",
                        value = "${detail.pingLatencyMs} ms",
                        valueColor = when {
                            detail.pingLatencyMs < 10 -> MaterialTheme.colorScheme.primary
                            detail.pingLatencyMs < 50 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                }
            } else if (device.ipAddress != null && device.ipAddress != "0.0.0.0") {
                item { DetailRow(label = "Latenza ping", value = "Timeout") }
            }

            // SSIDs
            if (detail.ssids.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "SSID trasmessi (${detail.ssids.size}):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                detail.ssids.forEach { ssid ->
                    item { DetailRow(label = "SSID", value = ssid) }
                }
            }

            // Clients
            if (detail.clients != null) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Client connessi: ${detail.clients}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Mostra tutti i client (virtualizzati dalla LazyColumn)
                items(detail.clientList) { client ->
                    val rssiInfo = client.rssi?.let { "${it} dBm" }
                    val ssidInfo = client.ssid?.let { "[$it]" }
                    val rssiColor = if (client.rssi != null) {
                        when {
                            client.rssi!! >= -50 -> MaterialTheme.colorScheme.primary
                            client.rssi!! >= -70 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.error
                        }
                    } else null

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (ssidInfo != null) {
                                    Text(
                                        text = ssidInfo,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                if (rssiInfo != null) {
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = rssiInfo,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = rssiColor ?: MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (!client.hostname.isNullOrBlank()) {
                                Text(
                                    text = client.hostname,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (!client.ip.isNullOrBlank()) {
                                Text(
                                    text = client.ip,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (client.mac.isNotBlank()) {
                                Text(
                                    text = client.mac,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Errore
            if (detail.error != null) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "⚠ ${detail.error}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.35f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.65f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ── Card Riepilogo Controller ──

@Composable
private fun ControllerStatusCards(statuses: List<ControllerStatus>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        statuses.forEach { status ->
            val color = when {
                !status.isConfigured -> MaterialTheme.colorScheme.surfaceVariant
                status.isConnected -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.error
            }
            val icon = when {
                !status.isConfigured -> Icons.Default.LinkOff
                status.isConnected -> Icons.Default.CheckCircle
                else -> Icons.Default.Error
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.height(4.dp))
                    Text(status.source.label, style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold, color = color)
                    if (status.isConfigured) {
                        Text("${status.onlineCount}/${status.deviceCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// ── Card Dispositivo ──

@Composable
private fun ControllerDeviceCard(
    device: ControllerDevice,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val statusIcon = when (device.status) {
                ControllerDeviceStatus.ONLINE -> Icons.Default.Wifi
                ControllerDeviceStatus.OFFLINE -> Icons.Default.WifiOff
                ControllerDeviceStatus.ALERTING -> Icons.Default.Warning
                else -> Icons.Default.DevicesOther
            }
            val statusColor = when (device.status) {
                ControllerDeviceStatus.ONLINE -> MaterialTheme.colorScheme.primary
                ControllerDeviceStatus.OFFLINE -> MaterialTheme.colorScheme.error
                ControllerDeviceStatus.ALERTING -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Icon(statusIcon, contentDescription = device.status.label, tint = statusColor, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(device.displayName, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val detail = listOfNotNull(device.model, device.ipAddress).joinToString(" · ")
                if (detail.isNotBlank()) {
                    Text(detail, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                // Rete di appartenenza
                if (!device.networkName.isNullOrBlank()) {
                    Text(device.networkName, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                val extra = listOfNotNull(
                    device.clients?.let { "$it client" },
                    device.firmware?.let { "fw $it" }
                ).joinToString(" · ")
                if (extra.isNotBlank()) {
                    Text(extra, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            val statusLabel = when (device.status) {
                ControllerDeviceStatus.UNKNOWN -> "--"
                else -> device.status.label
            }
            Surface(shape = MaterialTheme.shapes.small, color = statusColor.copy(alpha = 0.12f)) {
                Text(statusLabel, style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold, color = statusColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }
    }
}

// ── Empty State ──

@Composable
private fun EmptyState(
    hasAnyConfigured: Boolean,
    errorMessage: String?,
    statuses: List<ControllerStatus>
) {
    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (!hasAnyConfigured) {
                Text("📡", style = MaterialTheme.typography.displayMedium)
                Spacer(Modifier.height(8.dp))
                Text("Nessun controller configurato", style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text("Apri le Impostazioni e configura Meraki, UniFi o Omada",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                statuses.filter { it.isConfigured }.forEach { status ->
                    Spacer(Modifier.height(8.dp))
                    val icon = if (status.isConnected) "✅" else "❌"
                    val connStatus = if (status.isConnected) "Connesso" else "Errore"
                    val errDetail = status.errorMessage?.let { " — $it" } ?: ""
                    Text("$icon ${status.source.label}: $connStatus · ${status.onlineCount}/${status.deviceCount}$errDetail",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (errorMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(errorMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                } else if (statuses.any { it.isConfigured && it.isConnected }) {
                    Spacer(Modifier.height(8.dp))
                    Text("Nessun dispositivo trovato sui controller configurati",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
