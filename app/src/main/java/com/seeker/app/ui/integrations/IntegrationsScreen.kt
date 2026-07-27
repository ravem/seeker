package com.seeker.app.ui.integrations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntegrationsScreen(
    onBack: (() -> Unit)? = null,
    viewModel: IntegrationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Configurazione Controller") },
            navigationIcon = {
                if (onBack != null) {
                    IconButton(onClick = onBack!!) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Meraki
            IntegrationCard(
                title = "Meraki Dashboard API",
                icon = Icons.Default.WifiTethering,
                color = MaterialTheme.colorScheme.primary,
                isConfigured = uiState.merakiApiKey.isNotBlank(),
                expanded = uiState.expandedMeraki,
                onToggle = { viewModel.toggleMeraki() },
                content = {
                    var showMerakiKey by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = uiState.merakiApiKey,
                        onValueChange = { viewModel.setMerakiApiKey(it) },
                        label = { Text("API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (showMerakiKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showMerakiKey = !showMerakiKey }) {
                                Icon(
                                    if (showMerakiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showMerakiKey) "Nascondi" else "Mostra"
                                )
                            }
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.merakiOrgId,
                        onValueChange = { viewModel.setMerakiOrgId(it) },
                        label = { Text("Organization ID") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.saveMeraki() },
                            modifier = Modifier.weight(1f),
                            enabled = uiState.merakiApiKey.isNotBlank() && uiState.merakiOrgId.isNotBlank() && !uiState.merakiTesting
                        ) {
                            Text("Salva")
                        }
                        OutlinedButton(
                            onClick = { viewModel.testMeraki() },
                            modifier = Modifier.weight(1f),
                            enabled = uiState.merakiApiKey.isNotBlank() && !uiState.merakiTesting
                        ) {
                            if (uiState.merakiTesting) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Test")
                            }
                        }
                    }
                    if (uiState.merakiStatus != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(uiState.merakiStatus!!, style = MaterialTheme.typography.bodySmall,
                            color = when {
                                uiState.merakiStatus!!.startsWith("✅") -> MaterialTheme.colorScheme.primary
                                uiState.merakiStatus!!.startsWith("🗑️") -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.error
                            })
                    }
                    // Elenco organizzazioni disponibili (dopo test riuscito)
                    if (uiState.merakiOrganizations.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("Organizzazioni disponibili:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary)
                        uiState.merakiOrganizations.forEach { org ->
                            val isSelected = org.id == uiState.merakiOrgId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectMerakiOrganization(org) }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.selectMerakiOrganization(org) }
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(org.name, style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                    Text("ID: ${org.id}", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else if (uiState.merakiLoadingOrgs) {
                        Spacer(Modifier.height(4.dp))
                        Text("Caricamento organizzazioni…",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    // Pulsante elimina (solo se ci sono credenziali salvate)
                    if (uiState.merakiApiKey.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        TextButton(
                            onClick = { viewModel.deleteMeraki() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Elimina credenziali", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )

            // UniFi
            IntegrationCard(
                title = "Ubiquiti UniFi",
                icon = Icons.Default.Router,
                color = MaterialTheme.colorScheme.tertiary,
                isConfigured = uiState.unifiUrl.isNotBlank(),
                expanded = uiState.expandedUnifi,
                onToggle = { viewModel.toggleUnifi() },
                content = {
                    OutlinedTextField(
                        value = uiState.unifiUrl,
                        onValueChange = { viewModel.setUnifiUrl(it) },
                        label = { Text("Controller URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("https://192.168.1.1:8443") }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.unifiUsername,
                        onValueChange = { viewModel.setUnifiUsername(it) },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    var showUnifiPass by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = uiState.unifiPassword,
                        onValueChange = { viewModel.setUnifiPassword(it) },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (showUnifiPass) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showUnifiPass = !showUnifiPass }) {
                                Icon(
                                    if (showUnifiPass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showUnifiPass) "Nascondi" else "Mostra"
                                )
                            }
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.saveUnifi() },
                            modifier = Modifier.weight(1f),
                            enabled = uiState.unifiUrl.isNotBlank() && !uiState.unifiTesting
                        ) {
                            Text("Salva")
                        }
                        OutlinedButton(
                            onClick = { viewModel.testUnifi() },
                            modifier = Modifier.weight(1f),
                            enabled = uiState.unifiUrl.isNotBlank() && !uiState.unifiTesting
                        ) {
                            if (uiState.unifiTesting) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Test")
                            }
                        }
                    }
                    if (uiState.unifiStatus != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(uiState.unifiStatus!!, style = MaterialTheme.typography.bodySmall,
                            color = when {
                                uiState.unifiStatus!!.startsWith("✅") -> MaterialTheme.colorScheme.primary
                                uiState.unifiStatus!!.startsWith("🗑️") -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.error
                            })
                    }
                    if (uiState.unifiUrl.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        TextButton(
                            onClick = { viewModel.deleteUnifi() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Elimina credenziali", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )

            // Omada
            IntegrationCard(
                title = "TP-Link Omada",
                icon = Icons.Default.SettingsEthernet,
                color = MaterialTheme.colorScheme.secondary,
                isConfigured = uiState.omadaUrl.isNotBlank(),
                expanded = uiState.expandedOmada,
                onToggle = { viewModel.toggleOmada() },
                content = {
                    OutlinedTextField(
                        value = uiState.omadaUrl,
                        onValueChange = { viewModel.setOmadaUrl(it) },
                        label = { Text("Controller URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("https://192.168.1.1") }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.omadaUsername,
                        onValueChange = { viewModel.setOmadaUsername(it) },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    var showOmadaPass by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = uiState.omadaPassword,
                        onValueChange = { viewModel.setOmadaPassword(it) },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (showOmadaPass) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showOmadaPass = !showOmadaPass }) {
                                Icon(
                                    if (showOmadaPass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showOmadaPass) "Nascondi" else "Mostra"
                                )
                            }
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.saveOmada() },
                            modifier = Modifier.weight(1f),
                            enabled = uiState.omadaUrl.isNotBlank() && !uiState.omadaTesting
                        ) {
                            Text("Salva")
                        }
                        OutlinedButton(
                            onClick = { viewModel.testOmada() },
                            modifier = Modifier.weight(1f),
                            enabled = uiState.omadaUrl.isNotBlank() && !uiState.omadaTesting
                        ) {
                            if (uiState.omadaTesting) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Test")
                            }
                        }
                    }
                    if (uiState.omadaStatus != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(uiState.omadaStatus!!, style = MaterialTheme.typography.bodySmall,
                            color = when {
                                uiState.omadaStatus!!.startsWith("✅") -> MaterialTheme.colorScheme.primary
                                uiState.omadaStatus!!.startsWith("🗑️") -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.error
                            })
                    }
                    if (uiState.omadaUrl.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        TextButton(
                            onClick = { viewModel.deleteOmada() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Elimina credenziali", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun IntegrationCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    isConfigured: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = color)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        if (isConfigured) {
                            Text("Configurato", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                IconButton(onClick = onToggle) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Chiudi" else "Espandi"
                    )
                }
            }
            if (expanded) {
                HorizontalDivider()
                Column(
                    modifier = Modifier.padding(16.dp),
                    content = content
                )
            }
        }
    }
}
