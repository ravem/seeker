package com.seeker.app.core.model

import android.net.wifi.ScanResult

/**
 * Rappresenta un Access Point rilevato durante la scansione Wi-Fi.
 */
data class AccessPoint(
    val ssid: String,
    val bssid: String,
    val signalStrengthDbm: Int,
    val frequencyMhz: Int,
    val channel: Int,
    val channelWidthMhz: Int = 20,   // 20/40/80/160/320 MHz
    val band: WifiBand,
    val wifiStandard: WifiStandard? = null,
    val securityProtocols: List<SecurityProtocol>,
    val capabilities: String,
    val isHidden: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val vendor: String? = null          // Produttore AP (da OUI BSSID)
)

enum class SecurityProtocol(val displayName: String) {
    OPEN("Aperta"),
    WEP("WEP"),
    WPA_PSK("WPA-PSK"),
    WPA2_PSK("WPA2-PSK"),
    WPA3_SAE("WPA3-SAE"),
    WPA_EAP("WPA-EAP"),
    WPA2_EAP("WPA2-EAP"),
    WPA3_ENTERPRISE("WPA3-Enterprise"),
    OWE("OWE"),
    UNKNOWN("Sconosciuto");

    companion object {
        fun fromCapabilities(capabilities: String): List<SecurityProtocol> {
            if (capabilities.isBlank()) return listOf(OPEN)
            val protocols = mutableListOf<SecurityProtocol>()
            if (capabilities.contains("SAE") || capabilities.contains("WPA3")) protocols.add(WPA3_SAE)
            if (capabilities.contains("SUITE_B") || capabilities.contains("SUITE-B")) protocols.add(WPA3_ENTERPRISE)
            if (capabilities.contains("OWE") || capabilities.contains("OWE_TRANSITION")) protocols.add(OWE)
            if (capabilities.contains("WPA2-EAP") || (capabilities.contains("WPA2") && capabilities.contains("EAP"))) protocols.add(WPA2_EAP)
            if (capabilities.contains("WPA-EAP") || (capabilities.contains("WPA") && capabilities.contains("EAP") && !protocols.contains(WPA2_EAP))) protocols.add(WPA_EAP)
            if (capabilities.contains("WPA2-PSK") || capabilities.contains("WPA2-PSK") || capabilities.contains("CCMP")) protocols.add(WPA2_PSK)
            if (capabilities.contains("WPA-PSK") && !protocols.contains(WPA2_PSK)) protocols.add(WPA_PSK)
            if (capabilities.contains("WEP")) protocols.add(WEP)
            return if (protocols.isEmpty()) {
                if (capabilities.contains("ESS")) listOf(OPEN) else listOf(UNKNOWN)
            } else protocols
        }
    }
}

data class AccessPointGroup(
    val ssid: String,
    val accessPoints: List<AccessPoint>,
    val bestSignal: Int = accessPoints.maxOfOrNull { it.signalStrengthDbm } ?: Int.MIN_VALUE,
    val bandCount: Int = accessPoints.map { it.band }.distinct().size
)

fun ScanResult.toAccessPoint(): AccessPoint {
    val ssidStr = SSID?.ifBlank { null } ?: "<Rete Nascosta>"
    val bssidStr = BSSID?.ifBlank { null } ?: "00:00:00:00:00:00"
    val channel = frequencyToChannel(frequency)
    val band = WifiBand.fromFrequencyMhz(frequency)
    val security = SecurityProtocol.fromCapabilities(capabilities ?: "")
    val channelWidth = channelWidthFromScanResult()
    val wifiStd = if (android.os.Build.VERSION.SDK_INT >= 31) {
        WifiStandard.fromScanResultStandard(getWifiStandard())
    } else null

    return AccessPoint(
        ssid = ssidStr,
        bssid = bssidStr,
        signalStrengthDbm = level,
        frequencyMhz = frequency,
        channel = channel,
        channelWidthMhz = channelWidth,
        band = band,
        wifiStandard = wifiStd,
        securityProtocols = security,
        capabilities = capabilities ?: "",
        isHidden = SSID.isNullOrBlank()
    )
}

fun frequencyToChannel(frequencyMhz: Int): Int = when {
    frequencyMhz in 2412..2484 -> (frequencyMhz - 2412) / 5 + 1
    frequencyMhz in 5170..5825 -> (frequencyMhz - 5170) / 5 + 34
    frequencyMhz in 5955..7125 -> (frequencyMhz - 5955) / 5 + 1
    else -> 0
}

private fun ScanResult.channelWidthFromScanResult(): Int {
    return when (channelWidth) {
        ScanResult.CHANNEL_WIDTH_20MHZ -> 20
        ScanResult.CHANNEL_WIDTH_40MHZ -> 40
        ScanResult.CHANNEL_WIDTH_80MHZ -> 80
        ScanResult.CHANNEL_WIDTH_160MHZ -> 160
        ScanResult.CHANNEL_WIDTH_320MHZ -> 320
        ScanResult.CHANNEL_WIDTH_80MHZ_PLUS_MHZ -> 160
        else -> 20
    }
}
