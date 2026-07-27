package com.seeker.app.core.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.DhcpInfo
import android.net.LinkProperties
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import com.seeker.app.core.extension.connectivityManager
import com.seeker.app.core.extension.wifiManager
import com.seeker.app.core.model.ConnectedNetwork
import com.seeker.app.core.model.SignalLevel
import com.seeker.app.core.model.WifiBand
import java.net.Inet4Address
import java.net.InetAddress

/**
 * Utility per ottenere informazioni sulla connessione Wi-Fi attuale.
 * Centralizza tutta la logica di estrazione dati dai manager di sistema.
 */
object WifiUtils {

    private const val TAG = "SeekerWifiUtils"

    @Suppress("DEPRECATION")
    fun getConnectedNetwork(context: Context): ConnectedNetwork? {
        Log.d(TAG, "getConnectedNetwork() - INIZIO")

        val wifiManager: WifiManager = context.wifiManager
        val connectivityManager: ConnectivityManager = context.connectivityManager
        Log.d(TAG, "wifiManager=$wifiManager, connectivityManager=$connectivityManager")

        // Verifica se siamo connessi a una rete Wi-Fi
        val network = connectivityManager.activeNetwork
        Log.d(TAG, "activeNetwork=$network")
        if (network == null) {
            Log.d(TAG, "Nessuna rete attiva")
            return null
        }

        val caps = connectivityManager.getNetworkCapabilities(network)
        Log.d(TAG, "networkCapabilities=$caps")
        if (caps == null) {
            Log.d(TAG, "Impossibile ottenere NetworkCapabilities")
            return null
        }
        if (!caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            Log.d(TAG, "La rete attiva NON è Wi-Fi")
            return null
        }
        Log.d(TAG, "La rete attiva è Wi-Fi ✅")

        val wifiInfo: WifiInfo? = wifiManager.connectionInfo
        Log.d(TAG, "wifiInfo=$wifiInfo")
        if (wifiInfo == null) {
            Log.d(TAG, "wifiInfo è null")
            return null
        }

        val ssid = wifiInfo.ssid?.removeSurrounding("\"").orEmpty()
        val bssid = wifiInfo.bssid.orEmpty()
        Log.d(TAG, "ssid='$ssid', bssid='$bssid', rssi=${wifiInfo.rssi}, linkSpeed=${wifiInfo.linkSpeed}, freq=${wifiInfo.frequency}")

        // Se non c'è SSID, non siamo connessi
        if (ssid.isBlank() || ssid == "<unknown ssid>") {
            Log.d(TAG, "SSID non valido: '$ssid' — restituisco null")
            return null
        }

        val linkProperties = connectivityManager.getLinkProperties(network)
        Log.d(TAG, "linkProperties=$linkProperties")

        val ipAddress = getIpAddress(wifiInfo, linkProperties)
        val subnetMask = getSubnetMask(wifiInfo, linkProperties)
        val defaultGateway = getDefaultGateway(linkProperties, wifiManager.dhcpInfo)
        val dnsServers = getDnsServers(linkProperties)
        val frequencyMhz = getFrequency(wifiInfo)

        // Channel width: cerca il canale corrispondente nei risultati di scansione
        val channelWidth = getChannelWidthForBssid(context, bssid, frequencyMhz)

        // WiFi standard (API 31+)
        val wifiStandard = if (Build.VERSION.SDK_INT >= 31) {
            com.seeker.app.core.model.WifiStandard.fromWifiInfoStandard(wifiInfo.wifiStandard)
        } else null

        Log.d(TAG, "ip=$ipAddress, subnet=$subnetMask, gateway=$defaultGateway, dns=$dnsServers, freq=$frequencyMhz, chWidth=${channelWidth}MHz, std=$wifiStandard")

        // TX/RX link speed (API 31+)
        val txSpeed = if (Build.VERSION.SDK_INT >= 31) wifiInfo.txLinkSpeedMbps else null
        val rxSpeed = if (Build.VERSION.SDK_INT >= 31) wifiInfo.rxLinkSpeedMbps else null

        val result = ConnectedNetwork(
            ssid = ssid,
            bssid = bssid,
            signalStrengthDbm = wifiInfo.rssi,
            signalLevel = SignalLevel.fromDbm(wifiInfo.rssi),
            ipAddress = ipAddress,
            subnetMask = subnetMask,
            defaultGateway = defaultGateway,
            dnsServers = dnsServers,
            linkSpeedMbps = wifiInfo.linkSpeed,
            txLinkSpeedMbps = txSpeed,
            rxLinkSpeedMbps = rxSpeed,
            frequencyMhz = frequencyMhz,
            band = WifiBand.fromFrequencyMhz(frequencyMhz),
            channelWidthMhz = channelWidth,
            wifiStandard = wifiStandard,
            isConnected = true,
            apVendor = null  // Verrà popolato dal repository
        )
        Log.d(TAG, "ConnectedNetwork creato: $result")
        return result
    }

    private fun getIpAddress(wifiInfo: WifiInfo, linkProperties: LinkProperties?): String {
        Log.d(TAG, "getIpAddress() - linkProperties=$linkProperties")
        // Su Android 12+ usiamo linkProperties
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            linkProperties?.let { lp ->
                Log.d(TAG, "linkProperties.linkAddresses=${lp.linkAddresses}")
                lp.linkAddresses.forEach { la ->
                    Log.d(TAG, "  address=${la.address}, isInet4=${la.address is Inet4Address}")
                    if (la.address is Inet4Address) {
                        val hostAddr = la.address.hostAddress
                        Log.d(TAG, "  Trovato IPv4: $hostAddr")
                        return hostAddr ?: "0.0.0.0"
                    }
                }
            }
            Log.d(TAG, "Nessun IPv4 trovato in linkProperties")
        }

        // Fallback: dal WifiInfo (deprecato da API 31)
        @Suppress("DEPRECATION")
        val ipInt = wifiInfo.ipAddress
        Log.d(TAG, "Fallback wifiInfo.ipAddress=$ipInt")
        if (ipInt != 0) {
            return String.format(
                "%d.%d.%d.%d",
                ipInt and 0xFF,
                (ipInt shr 8) and 0xFF,
                (ipInt shr 16) and 0xFF,
                (ipInt shr 24) and 0xFF
            )
        }

        return "0.0.0.0"
    }

    private fun getSubnetMask(wifiInfo: WifiInfo, linkProperties: LinkProperties?): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            linkProperties?.let { lp ->
                lp.linkAddresses.forEach { la ->
                    if (la.address is Inet4Address) {
                        return prefixLengthToSubnetMask(la.prefixLength)
                    }
                }
            }
        }
        return "255.255.255.0"
    }

    private fun getDefaultGateway(linkProperties: LinkProperties?, dhcpInfo: DhcpInfo?): String {
        Log.d(TAG, "getDefaultGateway() - linkProperties=$linkProperties, dhcpInfo=$dhcpInfo")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            linkProperties?.let { lp ->
                Log.d(TAG, "linkProperties.routes=${lp.routes}")
                lp.routes.forEach { route ->
                    Log.d(TAG, "  route=$route, isDefault=${route.isDefaultRoute}, gateway=${route.gateway}")
                    if (route.isDefaultRoute && route.gateway is Inet4Address) {
                        val gw = route.gateway?.hostAddress ?: ""
                        Log.d(TAG, "  Trovato gateway: $gw")
                        return gw
                    }
                }
            }
        }

        @Suppress("DEPRECATION")
        val gatewayInt = dhcpInfo?.gateway ?: 0
        Log.d(TAG, "Fallback dhcpInfo.gateway=$gatewayInt")
        if (gatewayInt != 0) {
            return String.format(
                "%d.%d.%d.%d",
                gatewayInt and 0xFF,
                (gatewayInt shr 8) and 0xFF,
                (gatewayInt shr 16) and 0xFF,
                (gatewayInt shr 24) and 0xFF
            )
        }

        return "0.0.0.0"
    }

    private fun getDnsServers(linkProperties: LinkProperties?): List<String> {
        linkProperties?.let { lp ->
            Log.d(TAG, "linkProperties.dnsServers=${lp.dnsServers}")
            val dns = lp.dnsServers.filterIsInstance<Inet4Address>().map { it.hostAddress ?: "" }
            if (dns.isNotEmpty()) {
                Log.d(TAG, "DNS trovati: $dns")
                return dns
            }
        }
        Log.d(TAG, "Nessun DNS trovato, uso fallback")
        return listOf("8.8.8.8", "8.8.4.4")
    }

    private fun getFrequency(wifiInfo: WifiInfo): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            wifiInfo.frequency
        } else {
            2412
        }
    }

    private fun prefixLengthToSubnetMask(prefixLength: Int): String {
        val mask = if (prefixLength == 0) 0 else 0xFFFFFFFF.toInt() shl (32 - prefixLength)
        return String.format(
            "%d.%d.%d.%d",
            mask shr 24 and 0xFF,
            mask shr 16 and 0xFF,
            mask shr 8 and 0xFF,
            mask and 0xFF
        )
    }

    /**
     * Ottiene l'ampiezza del canale per il BSSID connesso.
     * Cerca tra gli ultimi risultati di scansione un AP con BSSID corrispondente.
     */
    private fun getChannelWidthForBssid(context: Context, bssid: String, defaultFreq: Int): Int {
        if (bssid.isBlank() || bssid == "02:00:00:00:00:00") return 20
        return try {
            val wifiManager = context.wifiManager
            val scanResults = wifiManager.scanResults ?: return 20
            val match = scanResults.find { it.BSSID == bssid }
            if (match != null) {
                when (match.channelWidth) {
                    android.net.wifi.ScanResult.CHANNEL_WIDTH_20MHZ -> 20
                    android.net.wifi.ScanResult.CHANNEL_WIDTH_40MHZ -> 40
                    android.net.wifi.ScanResult.CHANNEL_WIDTH_80MHZ -> 80
                    android.net.wifi.ScanResult.CHANNEL_WIDTH_160MHZ -> 160
                    android.net.wifi.ScanResult.CHANNEL_WIDTH_320MHZ -> 320
                    android.net.wifi.ScanResult.CHANNEL_WIDTH_80MHZ_PLUS_MHZ -> 160
                    else -> 20
                }
            } else {
                // Fallback: stima basata sulla frequenza
                when {
                    defaultFreq >= 5955 -> 80  // 6 GHz tipicamente 80 MHz
                    defaultFreq >= 5170 -> 80  // 5 GHz tipicamente 80 MHz
                    else -> 20                  // 2.4 GHz tipicamente 20 MHz
                }
            }
        } catch (_: Exception) { 20 }
    }
}
