package com.seeker.app.core.model

/**
 * Modello unificato per un dispositivo proveniente da un controller API
 * (Meraki, UniFi, Omada). Usato nella dashboard dei controller.
 */
data class ControllerDevice(
    val name: String,
    val model: String? = null,
    val serial: String? = null,
    val mac: String? = null,
    val ipAddress: String? = null,
    val firmware: String? = null,
    val status: ControllerDeviceStatus = ControllerDeviceStatus.UNKNOWN,
    val controllerSource: ControllerSource,
    val networkName: String? = null,    // Nome della rete/sito di appartenenza
    val tags: List<String> = emptyList(),
    val clients: Int? = null,           // Numero di client connessi
    val radioChannels: List<Int> = emptyList(),
    val uptime: Long? = null,           // secondi
    val lastSeen: Long? = null
) {
    val displayName: String get() = name.ifBlank { model ?: serial ?: mac ?: "Sconosciuto" }
}

enum class ControllerDeviceStatus(val label: String) {
    ONLINE("Online"),
    OFFLINE("Offline"),
    ALERTING("Alerting"),
    DORMANT("Dormant"),
    PENDING("In attesa"),
    UNKNOWN("Sconosciuto")
}

enum class ControllerSource(val label: String) {
    MERAKI("Meraki"),
    UNIFI("UniFi"),
    OMADA("Omada")
}

/**
 * Stato di un controller configurato.
 */
data class ControllerStatus(
    val source: ControllerSource,
    val isConfigured: Boolean,
    val isConnected: Boolean = false,
    val errorMessage: String? = null,
    val deviceCount: Int = 0,
    val onlineCount: Int = 0
)
