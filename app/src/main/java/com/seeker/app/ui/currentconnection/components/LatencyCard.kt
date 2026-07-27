package com.seeker.app.ui.currentconnection.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seeker.app.ui.components.CardHeader
import com.seeker.app.ui.components.DetailRow
import com.seeker.app.ui.components.WifiCard

/**
 * Card che mostra la latenza verso il gateway e verso Internet (1.1.1.1).
 */
@Composable
fun LatencyCard(
    gatewayMs: Long?,
    internetMs: Long?,
    modifier: Modifier = Modifier
) {
    if (gatewayMs == null && internetMs == null) return

    WifiCard(modifier = modifier.fillMaxWidth()) {
        CardHeader(
            icon = Icons.Default.Public,
            title = "Latenza"
        )

        Column(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 16.dp)) {
            if (gatewayMs != null) {
                LatencyRow(
                    label = "Gateway",
                    value = gatewayMs,
                    icon = "🏠"
                )
            }
            if (internetMs != null) {
                Spacer(Modifier.height(4.dp))
                LatencyRow(
                    label = "Internet (1.1.1.1)",
                    value = internetMs,
                    icon = "🌐"
                )
            }
        }
    }
}

@Composable
private fun LatencyRow(
    label: String,
    value: Long,
    icon: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = when {
                value < 10 -> "${value} ms"
                value < 50 -> "${value} ms"
                value < 100 -> "${value} ms"
                value < 500 -> "${value} ms"
                else -> "${value} ms"
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = when {
                value < 20 -> MaterialTheme.colorScheme.primary
                value < 60 -> androidx.compose.ui.graphics.Color(0xFF8BC34A)
                value < 150 -> androidx.compose.ui.graphics.Color(0xFFFFC107)
                else -> MaterialTheme.colorScheme.error
            }
        )
    }
}
