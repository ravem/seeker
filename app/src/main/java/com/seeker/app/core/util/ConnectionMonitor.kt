package com.seeker.app.core.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import com.seeker.app.core.extension.connectivityManager
import com.seeker.app.core.extension.wifiManager
import com.seeker.app.core.model.ConnectedNetwork
import com.seeker.app.core.model.SignalLevel
import com.seeker.app.core.model.TransportType
import com.seeker.app.core.model.WifiBand
import com.seeker.app.core.model.WifiStandard
import java.net.Inet4Address

private const val TAG = "SeekerConnMon"

/**
 * Monitor della connessione di rete attuale.
 * Supporta sia Wi-Fi che Ethernet (USB-C).
 */
object ConnectionMonitor {

    /** Nomi di interfaccia che identificano connessioni dati mobili, non Ethernet reale */
    private val MOBILE_DATA_INTERFACES = listOf(
        "rmnet", "ccmni", "wwan", "pdp", "uwb", "wlan"
    ).map { it.lowercase() }

    /** Verifica se un nome di interfaccia è una connessione dati mobile */
    private fun isMobileDataInterface(iface: String?): Boolean {
        if (iface == null) return false
        val name = iface.lowercase().trim()
        return MOBILE_DATA_INTERFACES.any { name.startsWith(it) }
    }

    fun getActiveNetwork(context: Context): ConnectedNetwork? {
        val connectivityManager: ConnectivityManager = context.connectivityManager
        val network = connectivityManager.activeNetwork ?: return null
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return null

        // Verifica che la rete abbia connettività effettiva
        if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            Log.d(TAG, "Rete non validata (nessuna connettività effettiva)")
            return null
        }

        val lp = connectivityManager.getLinkProperties(network)
        val iface = lp?.interfaceName ?: ""

        // Se l'interfaccia è una connessione dati mobile (rmnet, ecc.) e non Wi-Fi, salta
        if (isMobileDataInterface(iface) && !caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            Log.d(TAG, "Interfaccia $iface è una connessione dati mobile — skip")
            return null
        }

        val transport = when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> TransportType.WIFI
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> TransportType.ETHERNET
            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> TransportType.VPN
            else -> TransportType.UNKNOWN
        }

        // Per Ethernet, verifica che NON sia un'interfaccia mobile (rmnet etc.)
        if (transport == TransportType.ETHERNET) {
            if (isMobileDataInterface(iface)) {
                Log.d(TAG, "Ethernet ma interfaccia $iface è dati mobile — skip")
                return null
            }
            val hasIpv4 = lp?.linkAddresses?.any { it.address is Inet4Address && !it.address.isLoopbackAddress } == true
            if (!hasIpv4) {
                Log.d(TAG, "Ethernet senza indirizzo IPv4 valido — skip")
                return null
            }
        }

        return when (transport) {
            TransportType.WIFI -> buildWifiNetwork(context, lp, caps)
            TransportType.ETHERNET -> buildEthernetNetwork(lp)
            else -> buildGenericNetwork(lp, transport)
        }
    }

    /**
     * Verifica se c'è almeno una rete attiva e validata.
     */
    fun isConnected(context: Context): Boolean {
        return getActiveNetwork(context) != null
    }

    /**
     * Verifica se la rete attuale è Wi-Fi.
     */
    fun isWifi(context: Context): Boolean {
        val connectivityManager: ConnectivityManager = context.connectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun buildWifiNetwork(context: Context, lp: LinkProperties?, caps: NetworkCapabilities? = null): ConnectedNetwork {
        val wifiManager: WifiManager = context.wifiManager
        val wifiInfo = wifiManager.connectionInfo

        val ssid = wifiInfo?.ssid?.removeSurrounding("\"") ?: ""
        val bssid = wifiInfo?.bssid ?: ""
        val rssi = wifiInfo?.rssi ?: 0
        val linkSpeed = wifiInfo?.linkSpeed ?: 0
        val frequency = wifiInfo?.frequency ?: 0
        val txSpeed = if (Build.VERSION.SDK_INT >= 31) wifiInfo?.txLinkSpeedMbps else null
        val rxSpeed = if (Build.VERSION.SDK_INT >= 31) wifiInfo?.rxLinkSpeedMbps else null
        val wifiStd = if (Build.VERSION.SDK_INT >= 31) {
            wifiInfo?.let { WifiStandard.fromWifiInfoStandard(it.wifiStandard) }
        } else null

        val (ip, subnet, gateway, dns, iface) = readNetworkConfig(lp)

        return ConnectedNetwork(
            transport = TransportType.WIFI,
            ssid = ssid,
            bssid = bssid,
            signalStrengthDbm = rssi,
            signalLevel = SignalLevel.fromDbm(rssi),
            ipAddress = ip,
            subnetMask = subnet,
            defaultGateway = gateway,
            dnsServers = dns,
            linkSpeedMbps = linkSpeed,
            frequencyMhz = frequency,
            band = WifiBand.fromFrequencyMhz(frequency),
            channelWidthMhz = 0, // Non facilmente ottenibile senza scan
            wifiStandard = wifiStd,
            isConnected = true,
            txLinkSpeedMbps = txSpeed,
            rxLinkSpeedMbps = rxSpeed,
            interfaceName = iface
        )
    }

    private fun buildEthernetNetwork(lp: LinkProperties?): ConnectedNetwork {
        val (ip, subnet, gateway, dns, iface) = readNetworkConfig(lp)

        return ConnectedNetwork(
            transport = TransportType.ETHERNET,
            ipAddress = ip,
            subnetMask = subnet,
            defaultGateway = gateway,
            dnsServers = dns,
            isConnected = true,
            interfaceName = iface
        )
    }

    private fun buildGenericNetwork(lp: LinkProperties?, transport: TransportType): ConnectedNetwork {
        val (ip, subnet, gateway, dns, iface) = readNetworkConfig(lp)

        return ConnectedNetwork(
            transport = transport,
            ipAddress = ip,
            subnetMask = subnet,
            defaultGateway = gateway,
            dnsServers = dns,
            isConnected = true,
            interfaceName = iface
        )
    }

    /**
     * Legge la configurazione di rete da LinkProperties.
     */
    private fun readNetworkConfig(lp: LinkProperties?): NetworkConfig {
        if (lp == null) return NetworkConfig()

        var ip = "0.0.0.0"
        var prefixLength = 24
        var gateway = "0.0.0.0"
        var iface = lp.interfaceName ?: ""

        // IP e subnet
        for (addr in lp.linkAddresses) {
            if (addr.address is Inet4Address && !addr.address.isLoopbackAddress) {
                ip = addr.address.hostAddress ?: ip
                prefixLength = addr.prefixLength
                break
            }
        }

        val subnetMask = prefixLengthToSubnetMask(prefixLength)

        // Gateway
        for (route in lp.routes) {
            if (route.isDefaultRoute && route.gateway is Inet4Address) {
                gateway = route.gateway?.hostAddress ?: gateway
                break
            }
        }

        // DNS
        val dns = lp.dnsServers
            .filterIsInstance<Inet4Address>()
            .mapNotNull { it.hostAddress }

        return NetworkConfig(ip, subnetMask, gateway, dns, iface)
    }

    private fun prefixLengthToSubnetMask(prefixLength: Int): String {
        if (prefixLength <= 0 || prefixLength > 32) return "255.255.255.0"
        val mask = if (prefixLength == 0) 0L else (0xFFFFFFFFL shl (32 - prefixLength))
        return String.format(
            "%d.%d.%d.%d",
            (mask shr 24) and 0xFF,
            (mask shr 16) and 0xFF,
            (mask shr 8) and 0xFF,
            mask and 0xFF
        )
    }

    private data class NetworkConfig(
        val ipAddress: String = "0.0.0.0",
        val subnetMask: String = "255.255.255.0",
        val defaultGateway: String = "0.0.0.0",
        val dnsServers: List<String> = emptyList(),
        val interfaceName: String = ""
    )
}
