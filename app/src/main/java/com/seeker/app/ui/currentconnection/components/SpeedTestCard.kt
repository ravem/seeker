package com.seeker.app.ui.currentconnection.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seeker.app.core.util.SpeedTestResult

@Composable
fun SpeedTestCard(
    result: SpeedTestResult?,
    isTesting: Boolean,
    showSpeedTest: Boolean,
    onToggle: () -> Unit,
    onRunTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Intestazione
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Speed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Speed Test",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                TextButton(onClick = onToggle) {
                    Text(if (!showSpeedTest) "Mostra" else "Nascondi")
                }
            }

            AnimatedVisibility(visible = showSpeedTest, enter = fadeIn(), exit = fadeOut()) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(12.dp))

                    if (isTesting) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(modifier = Modifier.size(32.dp))
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Test in corso…",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else if (result != null && result.error == null) {
                        // Risultati
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            SpeedMetric(
                                label = "Download",
                                value = result.downloadMbps?.let { formatSpeed(it) } ?: "--",
                                unit = "Mbps"
                            )
                            SpeedMetric(
                                label = "Upload",
                                value = result.uploadMbps?.let { formatSpeed(it) } ?: "--",
                                unit = "Mbps"
                            )
                            SpeedMetric(
                                label = "Latenza",
                                value = result.latencyMs?.let { "${it}ms" } ?: "--",
                                unit = ""
                            )
                        }

                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Server: ${result.server}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (result?.error != null) {
                        Text(
                            "⚠ ${result.error}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onRunTest,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isTesting
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Avvia Speed Test")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeedMetric(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (unit.isNotBlank()) {
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatSpeed(mbps: Double): String {
    return when {
        mbps >= 1000 -> String.format("%.0f", mbps)
        mbps >= 100 -> String.format("%.0f", mbps)
        mbps >= 10 -> String.format("%.1f", mbps)
        else -> String.format("%.2f", mbps)
    }
}
