package com.seeker.app.ui.scanner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.seeker.app.core.model.AccessPoint
import com.seeker.app.core.model.AccessPointGroup
import com.seeker.app.core.model.SecurityProtocol
import com.seeker.app.ui.components.WifiCard
import com.seeker.app.ui.theme.BandColors
import com.seeker.app.ui.theme.CardShape
import com.seeker.app.ui.theme.ChipShape
import com.seeker.app.ui.theme.SignalColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiScannerScreen(
    onSettings: () -> Unit = {},
    viewModel: WifiScannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedAp = remember { mutableStateOf<AccessPoint?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text("Scanner Wi-Fi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            },
            actions = {
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Aggiorna")
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

        // Barra di stato
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = uiState.sortMode == SortMode.BY_SIGNAL,
                onClick = { viewModel.toggleSort() },
                label = { Text(if (uiState.sortMode == SortMode.BY_SIGNAL) "Per segnale" else "Per nome") },
                leadingIcon = { Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )

            Spacer(Modifier.weight(1f))

            if (uiState.isScanning) {
                Text("Scansione…", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            } else {
                Text("Reti: ${uiState.accessPoints.size}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Barra di progresso durante scansione
        AnimatedVisibility(visible = uiState.isScanning, enter = fadeIn(), exit = fadeOut()) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        // Messaggio Ethernet
        if (!uiState.isOnWifi && uiState.accessPoints.isEmpty() && !uiState.isScanning) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.SettingsEthernet, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Connesso via Ethernet — la scansione Wi-Fi non è disponibile",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Errore
        val errorMsg = uiState.error
        if (errorMsg != null) {
            Text(
                text = errorMsg,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        // Lista degli Access Point
        if (uiState.groups.isEmpty() && !uiState.isScanning) {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📡", style = MaterialTheme.typography.displayMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Nessuna rete trovata", style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
            ) {
                items(uiState.groups, key = { it.ssid }) { group ->
                    AccessPointGroupCard(
                        group = group,
                        onApClick = { ap -> selectedAp.value = ap }
                    )
                }
            }
        }
    }

    // Bottom sheet dettaglio AP
    val ap = selectedAp.value
    if (ap != null) {
        AccessPointDetailSheet(
            accessPoint = ap,
            onDismiss = { selectedAp.value = null }
        )
    }
}

/**
 * Card per un gruppo di Access Point con lo stesso SSID (reti mesh/multi-AP).
 */
@Composable
private fun AccessPointGroupCard(
    group: AccessPointGroup,
    onApClick: (AccessPoint) -> Unit = {},
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header del gruppo
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WifiTethering, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = group.ssid,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                // Livello segnale migliore
                val bestColor = signalColorForLevel(group.bestSignal)
                Text(
                    text = "${group.bestSignal} dBm",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = bestColor
                )
            }

            if (group.bandCount > 1) {
                Text(
                    text = "${group.accessPoints.size} AP · ${group.bandCount} bande",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 28.dp, top = 2.dp)
                )
            }

            Spacer(Modifier.height(6.dp))

            // Lista degli AP in questo gruppo
            group.accessPoints.forEach { ap ->
                AccessPointItem(ap = ap, onClick = { onApClick(ap) })
                if (ap != group.accessPoints.last()) {
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

/**
 * Singolo Access Point nella lista.
 */
@Composable
private fun AccessPointItem(
    ap: AccessPoint,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Indicatore segnale (semplificato)
        val signalColor = signalColorForLevel(ap.signalStrengthDbm)
        Box(
            modifier = Modifier
                .size(8.dp)
                .padding(end = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(8.dp)) {
                drawCircle(color = signalColor.copy(alpha = 0.6f))
            }
        }

        Spacer(Modifier.width(8.dp))

        // BSSID e dettagli
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = ap.bssid,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
            // Vendor (se disponibile)
            if (ap.vendor != null) {
                Text(
                    text = ap.vendor,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row {
                // Canale
                Text(
                    text = "CH ${ap.channel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(6.dp))
                // Frequenza
                Text(
                    text = "${ap.frequencyMhz} MHz",
                    style = MaterialTheme.typography.labelSmall,
                    color = ap.bandColor()
                )
                Spacer(Modifier.width(6.dp))
                // Sicurezza
                if (ap.securityProtocols.isNotEmpty() && ap.securityProtocols.first() != SecurityProtocol.OPEN) {
                    Text(
                        text = ap.securityProtocols.first().displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // dBm
        Text(
            text = "${ap.signalStrengthDbm}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = signalColor
        )
    }
}

private fun signalColorForLevel(dbm: Int): androidx.compose.ui.graphics.Color = when {
    dbm >= -50 -> SignalColors.excellent
    dbm >= -60 -> SignalColors.good
    dbm >= -70 -> SignalColors.fair
    dbm >= -80 -> SignalColors.weak
    else -> SignalColors.veryWeak
}

private fun AccessPoint.bandColor(): androidx.compose.ui.graphics.Color = when (band) {
    com.seeker.app.core.model.WifiBand.GHZ_2_4 -> BandColors.ghz24
    com.seeker.app.core.model.WifiBand.GHZ_5 -> BandColors.ghz5
    com.seeker.app.core.model.WifiBand.GHZ_6 -> BandColors.ghz6
}
