package com.seeker.app.ui.currentconnection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.seeker.app.ui.currentconnection.components.ConnectionHeaderCard
import com.seeker.app.ui.currentconnection.components.IpConfigCard
import com.seeker.app.ui.currentconnection.components.LatencyCard
import com.seeker.app.ui.currentconnection.components.MobileNetworkCard
import com.seeker.app.ui.currentconnection.components.SignalGaugeCard
import com.seeker.app.ui.currentconnection.components.SpeedTestCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentConnectionScreen(
    onSettings: () -> Unit = {},
    viewModel: CurrentConnectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text("Rete Attuale", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("Rilevamento connessione…", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                AnimatedVisibility(visible = uiState.connectedNetwork != null, enter = fadeIn(), exit = fadeOut()) {
                    uiState.connectedNetwork?.let { network ->
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            ConnectionHeaderCard(network = network)
                            if (network.isWifi) {
                                SignalGaugeCard(
                                    network = network,
                                    signalHistory = uiState.signalHistory
                                )
                            }
                            IpConfigCard(network = network)
                            LatencyCard(
                                gatewayMs = network.latencyGatewayMs,
                                internetMs = network.latencyInternetMs
                            )
                            SpeedTestCard(
                                result = uiState.speedTestResult,
                                isTesting = uiState.isSpeedTesting,
                                showSpeedTest = uiState.showSpeedTest,
                                onToggle = { viewModel.toggleSpeedTest() },
                                onRunTest = { viewModel.runSpeedTest() }
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = uiState.connectedNetwork == null && !uiState.isLoading, enter = fadeIn(), exit = fadeOut()) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⚡", style = MaterialTheme.typography.displayMedium)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = uiState.errorMessage ?: "Nessuna rete connessa",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Rete Mobile (sempre visibile, anche senza Wi-Fi)
                MobileNetworkCard(mobileState = uiState.mobileNetworks)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
