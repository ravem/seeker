package com.seeker.app.core.util

import java.net.InetAddress
import java.net.NetworkInterface
import java.net.UnknownHostException

/**
 * Utility per operazioni di rete a basso livello.
 */
object NetworkUtils {

    /**
     * Ottiene l'indirizzo IP locale e la subnet mask per determinare la subnet CIDR.
     */
    fun getLocalIpInfo(): Pair<String, Int>? {
        val interfaces = NetworkInterface.getNetworkInterfaces() ?: return null

        // Raccogli tutte le interfacce candidate con IPv4
        data class Candidate(val name: String, val ip: String, val prefix: Int, val priority: Int)
        val candidates = mutableListOf<Candidate>()

        for (intf in interfaces) {
            if (intf.isLoopback || !intf.isUp) continue
            val name = intf.name.lowercase()

            // Assegna una priorità: Ethernet > Wi-Fi > Altro
            val priority = when {
                name.contains("eth") -> 0
                name.contains("usb") || name.contains("rndis") || name.contains("enx") -> 1
                name.contains("wlan") || name.contains("wifi") -> 2
                else -> continue // ignora altre interfacce (rmnet, lo, etc.)
            }

            for (addr in intf.inetAddresses) {
                if (addr is java.net.Inet4Address && !addr.isLoopbackAddress) {
                    val ip = addr.hostAddress ?: continue
                    val subnet = intf.getSubnetMask(addr) ?: "255.255.255.0"
                    val prefix = subnetToPrefixLength(subnet)
                    candidates.add(Candidate(name, ip, prefix, priority))
                    break // primo IPv4 trovato per questa interfaccia
                }
            }
        }

        // Sceglie l'interfaccia con priorità più alta (Ethernet > USB > Wi-Fi)
        val best = candidates.minByOrNull { it.priority } ?: return null
        return Pair(best.ip, best.prefix)
    }

    /**
     * Calcola l'indirizzo di broadcast per una data IP e subnet mask.
     */
    fun getBroadcastAddress(ip: String, subnetMask: String): String? {
        try {
            val ipBytes = InetAddress.getByName(ip).address
            val maskBytes = InetAddress.getByName(subnetMask).address
            val broadcast = ByteArray(4)
            for (i in 0..3) {
                broadcast[i] = (ipBytes[i].toInt() or (maskBytes[i].toInt().inv())).toByte()
            }
            return InetAddress.getByAddress(broadcast).hostAddress
        } catch (_: Exception) {
            return null
        }
    }

    /**
     * Genera la lista degli IP della subnet.
     */
    fun getSubnetIps(baseIp: String, prefixLength: Int): List<String> {
        try {
            val ip = InetAddress.getByName(baseIp).address
            val networkPrefix = (prefixLength.toLong())
            val hostBits = 32 - prefixLength
            val hostCount = (1L shl hostBits) - 2 // .0 e .255 esclusi

            if (hostCount <= 0 || hostCount > 2048) return emptyList() // limit to /21 max

            val base = (ip[0].toLong() and 0xFF shl 24) or
                    (ip[1].toLong() and 0xFF shl 16) or
                    (ip[2].toLong() and 0xFF shl 8) or
                    (ip[3].toLong() and 0xFF)

            val networkBase = (base and (0xFFFFFFFF.toLong() shl (32 - prefixLength)))
            val ips = mutableListOf<String>()

            for (i in 1 until (1L shl hostBits) - 1) {
                val hostIp = networkBase or i
                ips.add(String.format(
                    "%d.%d.%d.%d",
                    (hostIp shr 24) and 0xFF,
                    (hostIp shr 16) and 0xFF,
                    (hostIp shr 8) and 0xFF,
                    hostIp and 0xFF
                ))
            }

            return ips
        } catch (_: Exception) {
            return emptyList()
        }
    }

    /**
     * Converte una subnet mask in notazione decimale (es. "255.255.255.0") in prefisso CIDR.
     */
    fun subnetToPrefixLength(subnetMask: String): Int {
        try {
            val parts = subnetMask.split(".").map { it.toInt() and 0xFF }
            var prefix = 0
            for (part in parts) {
                prefix += Integer.bitCount(part)
            }
            return prefix
        } catch (_: Exception) {
            return 24
        }
    }

    /**
     * Ottiene la subnet mask per un dato indirizzo su una NetworkInterface.
     */
    private fun NetworkInterface.getSubnetMask(address: InetAddress): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return null
            while (interfaces.hasMoreElements()) {
                val intf = interfaces.nextElement()
                if (intf.name != this.name) continue
                val addrs = intf.interfaceAddresses
                for (ifaceAddr in addrs) {
                    if (ifaceAddr.address.hostAddress == address.hostAddress) {
                        val prefix = ifaceAddr.networkPrefixLength.toInt()
                        return prefixLengthToSubnet(prefix)
                    }
                }
            }
        } catch (_: Exception) {
        }
        return null
    }

    private fun prefixLengthToSubnet(prefixLength: Int): String {
        if (prefixLength < 0 || prefixLength > 32) return "255.255.255.0"
        val mask = if (prefixLength == 0) 0L else (0xFFFFFFFFL shl (32 - prefixLength))
        return String.format(
            "%d.%d.%d.%d",
            (mask shr 24) and 0xFF,
            (mask shr 16) and 0xFF,
            (mask shr 8) and 0xFF,
            mask and 0xFF
        )
    }
}
