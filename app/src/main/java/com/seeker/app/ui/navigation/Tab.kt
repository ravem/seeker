package com.seeker.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material.icons.filled.SignalWifiStatusbar4Bar
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Enum delle schede/tab principali dell'app.
 */
enum class Tab(
    val label: String,
    val icon: ImageVector,
    val route: String,
    val description: String
) {
    CurrentConnection(
        label = "Rete Attuale",
        icon = Icons.Default.SignalWifiStatusbar4Bar,
        route = "current_connection",
        description = "Stato della rete Wi-Fi connessa"
    ),
    WifiScanner(
        label = "Scanner Wi-Fi",
        icon = Icons.Default.WifiTethering,
        route = "wifi_scanner",
        description = "Scansione degli Access Point nelle vicinanze"
    ),
    LanDiscovery(
        label = "Dispositivi",
        icon = Icons.Default.DevicesOther,
        route = "lan_discovery",
        description = "Dispositivi sulla rete locale"
    ),
    Integrations(
        label = "Controller",
        icon = Icons.Default.Api,
        route = "integrations",
        description = "Configurazione API Meraki, UniFi e Omada"
    )
}
