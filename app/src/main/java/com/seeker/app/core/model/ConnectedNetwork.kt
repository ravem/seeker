package com.seeker.app.core.model

/** Tipo di connessione di rete. */
enum class TransportType {
    WIFI, ETHERNET, VPN, UNKNOWN
}

/**
 * Rappresenta la rete attualmente connessa (Wi-Fi o Ethernet).
 */
data class ConnectedNetwork(
    val transport: TransportType = TransportType.UNKNOWN,
    val ssid: String = "",
    val bssid: String = "",
    val signalStrengthDbm: Int = 0,
    val signalLevel: SignalLevel = SignalLevel.GOOD,
    val ipAddress: String = "0.0.0.0",
    val subnetMask: String = "0.0.0.0",
    val defaultGateway: String = "0.0.0.0",
    val dnsServers: List<String> = emptyList(),
    val linkSpeedMbps: Int = 0,
    val frequencyMhz: Int = 0,
    val band: WifiBand = WifiBand.GHZ_2_4,
    val channelWidthMhz: Int = 0,
    val wifiStandard: WifiStandard? = null,
    val isConnected: Boolean = true,
    val txLinkSpeedMbps: Int? = null,
    val rxLinkSpeedMbps: Int? = null,
    val latencyGatewayMs: Long? = null,  // Latenza verso il gateway (ms)
    val latencyInternetMs: Long? = null,  // Latenza verso 1.1.1.1 (ms)
    val apVendor: String? = null,          // Produttore AP (da OUI BSSID)
    val interfaceName: String? = null      // Nome interfaccia (es. wlan0, eth0)
) {
    val isWifi: Boolean get() = transport == TransportType.WIFI
    val isEthernet: Boolean get() = transport == TransportType.ETHERNET
    val displayName: String get() = when {
        isWifi && ssid.isNotBlank() -> ssid
        isEthernet -> "Ethernet"
        else -> "Connesso"
    }
    val hasWifiInfo: Boolean get() = isWifi && bssid.isNotBlank()
}

enum class SignalLevel {
    EXCELLENT, GOOD, FAIR, WEAK, VERY_WEAK;

    companion object {
        fun fromDbm(dbm: Int): SignalLevel = when {
            dbm >= -50 -> EXCELLENT
            dbm >= -60 -> GOOD
            dbm >= -70 -> FAIR
            dbm >= -80 -> WEAK
            else -> VERY_WEAK
        }
    }
}

enum class WifiBand(val label: String, val frequencyMhzRange: IntRange) {
    GHZ_2_4("2.4 GHz", 2400..2500),
    GHZ_5("5 GHz", 4900..5900),
    GHZ_6("6 GHz", 5900..7125);

    companion object {
        fun fromFrequencyMhz(frequencyMhz: Int): WifiBand = when {
            frequencyMhz in GHZ_2_4.frequencyMhzRange -> GHZ_2_4
            frequencyMhz in GHZ_5.frequencyMhzRange -> GHZ_5
            frequencyMhz in GHZ_6.frequencyMhzRange -> GHZ_6
            else -> GHZ_5
        }
    }
}

enum class WifiStandard(val displayName: String) {
    LEGACY("802.11 legacy"),
    N("802.11n"),
    AC("802.11ac"),
    AX("802.11ax (Wi-Fi 6)"),
    BE("802.11be (Wi-Fi 7)"),
    AD("802.11ad"),
    UNKNOWN("Sconosciuto");

    companion object {
        // Valori numerici dai costanti ScanResult (API 31+)
        private const val SR_LEGACY = 1
        private const val SR_11N = 4
        private const val SR_11AC = 5
        private const val SR_11AX = 6
        private const val SR_11BE = 7
        private const val SR_11AD = 3
        // Valori numerici dai costanti WifiInfo (API 31+)
        private const val WI_LEGACY = 1
        private const val WI_11N = 4
        private const val WI_11AC = 5
        private const val WI_11AX = 6
        private const val WI_11BE = 7
        private const val WI_11AD = 3

        fun fromScanResultStandard(standard: Int): WifiStandard = when (standard) {
            SR_LEGACY -> LEGACY
            SR_11N -> N
            SR_11AC -> AC
            SR_11AX -> AX
            SR_11BE -> BE
            SR_11AD -> AD
            else -> UNKNOWN
        }

        fun fromWifiInfoStandard(standard: Int): WifiStandard = when (standard) {
            WI_LEGACY -> LEGACY
            WI_11N -> N
            WI_11AC -> AC
            WI_11AX -> AX
            WI_11BE -> BE
            WI_11AD -> AD
            else -> UNKNOWN
        }
    }
}

/**
 * Canali Wi-Fi disponibili per banda.
 */
data class ChannelInfo(
    val band: WifiBand,
    val channels: List<Int>,
    val usedChannels: List<Int>
) {
    val usedCount: Int get() = usedChannels.size
    val totalCount: Int get() = channels.size
    val usagePercent: Float get() = if (totalCount > 0) usedCount.toFloat() / totalCount * 100f else 0f
}

/**
 * Info sulla rete mobile.
 */
data class MobileNetworkInfo(
    val simSlot: Int,               // 0 o 1
    val carrierName: String?,
    val networkType: String?,       // LTE, NR, HSPA, etc.
    val signalDbm: Int?,            // dBm, solo se disponibile
    val signalLevel: Int?,          // 0-4
    val cellId: String?,            // Identificativo cella
    val ipAddress: String? = null,  // Indirizzo IP mobile
    val isConnected: Boolean = false,
    val iccid: String? = null,      // ICCID della SIM (univoco per SIM)
    val phoneNumber: String? = null, // Numero di telefono
    val carrierId: Int? = null,      // ID operatore
    val cardId: Int? = null,         // Card ID univoco (API 30+)
    val subscriptionId: Int? = null, // Subscription ID univoco
    val isEmbedded: Boolean = false  // true = eSIM, false = SIM fisica
)

/**
 * Info generali sulle reti mobili.
 */
data class MobileNetworksState(
    val sim1: MobileNetworkInfo? = null,
    val sim2: MobileNetworkInfo? = null
)
