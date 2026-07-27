package com.seeker.app.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.system.measureTimeMillis
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scansione della rete locale tramite system ping (ICMP).
 * Usa il comando system ping che risolve il MAC via ARP,
 * popolando /proc/net/arp con gli indirizzi MAC reali.
 */
@Singleton
class PingScanner @Inject constructor() {

    /**
     * Esegue un ping sweep sulla subnet.
     * Usa il comando system ping per risolvere correttamente i MAC via ARP.
     */
    suspend fun scanSubnet(
        subnetIps: List<String>,
        timeoutMs: Int = 500,
        maxConcurrent: Int = 30
    ): List<PingResult> = withContext(Dispatchers.IO) {
        if (subnetIps.isEmpty()) return@withContext emptyList()

        coroutineScope {
            subnetIps.chunked(maxConcurrent).flatMap { batch ->
                batch.map { ip ->
                    async {
                        pingSystem(ip, timeoutMs)
                    }
                }.map { it.await() }
            }.filter { it.isReachable }
        }
    }

    /**
     * Ping tramite comando system /system/bin/ping.
     * Questo comando risolve il MAC via ARP, aggiornando /proc/net/arp.
     */
    private fun pingSystem(ipAddress: String, timeoutSec: Int): PingResult {
        var hostname: String? = null
        var reachable = false

        val elapsed = measureTimeMillis {
            try {
                val timeoutParam = (timeoutSec / 1000).coerceAtLeast(1)
                val cmd = arrayOf("/system/bin/ping", "-q", "-n", "-W", "$timeoutParam", "-c", "1", ipAddress)
                android.util.Log.d("SeekerPing", "Pinging $ipAddress...")
                val process = Runtime.getRuntime().exec(cmd)
                val exitCode = process.waitFor()
                reachable = (exitCode == 0)
                android.util.Log.d("SeekerPing", "$ipAddress -> exitCode=$exitCode reachable=$reachable")

                if (reachable) {
                    hostname = try {
                        val inet = java.net.InetAddress.getByName(ipAddress)
                        inet.hostName?.takeIf { it != ipAddress }
                    } catch (_: Exception) { null }
                }
            } catch (e: Exception) {
                android.util.Log.e("SeekerPing", "System ping failed for $ipAddress", e)
                // Fallback a InetAddress.isReachable
                try {
                    val inet = java.net.InetAddress.getByName(ipAddress)
                    reachable = inet.isReachable(timeoutSec * 1000)
                    android.util.Log.d("SeekerPing", "$ipAddress fallback reachable=$reachable")
                } catch (e2: Exception) {
                    android.util.Log.e("SeekerPing", "Fallback ping failed for $ipAddress", e2)
                }
            }
        }

        return PingResult(
            ipAddress = ipAddress,
            isReachable = reachable,
            responseTimeMs = elapsed,
            hostname = hostname
        )
    }

    /**
     * Legge la tabella ARP e restituisce solo le entry complete (Flags = 0x2).
     */
    /**
     * Legge la tabella ARP/neigh usando più metodi:
     * 1. `ip neigh` (funziona su Android 14+)
     * 2. `/proc/net/arp` (fallback per versioni precedenti)
     * Restituisce solo entry con MAC non nullo.
     */
    suspend fun readArpTable(): List<ArpEntry> = withContext(Dispatchers.IO) {
        try {
            // Metodo 1: ip neigh (funziona su Android 14+)
            val entries = try {
                readArpFromIpNeigh()
            } catch (e: Exception) {
                android.util.Log.w("SeekerARP", "ip neigh failed", e)
                emptyList()
            }

            if (entries.isNotEmpty()) {
                android.util.Log.d("SeekerARP", "ip neigh: ${entries.size} entries")
                return@withContext entries
            }

            // Metodo 2: /proc/net/arp (fallback)
            try {
                val fileEntries = readArpFromProcFs()
                android.util.Log.d("SeekerARP", "proc/net/arp: ${fileEntries.size} entries")
                fileEntries
            } catch (e: Exception) {
                android.util.Log.w("SeekerARP", "proc/net/arp failed", e)
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("SeekerARP", "Error reading ARP", e)
            emptyList()
        }
    }

    /** Legge ARP usando il comando `ip neigh`. */
    private fun readArpFromIpNeigh(): List<ArpEntry> {
        val entries = mutableListOf<ArpEntry>()
        val process = Runtime.getRuntime().exec(arrayOf("ip", "neigh"))
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        process.waitFor()

        var line = reader.readLine()
        while (line != null) {
            val parts = line.trim().split("\\s+".toRegex())
            // Formato: IP dev IFACE lladdr MAC STATE
            // parts[0] = IP
            // parts[3] = "lladdr"
            // parts[4] = MAC
            if (parts.size >= 5 && parts[3] == "lladdr" && parts[4] != "00:00:00:00:00:00") {
                val mac = parts[4]
                if (mac != "FAILED") {
                    entries.add(ArpEntry(ipAddress = parts[0], macAddress = mac, interfaceName = parts[2]))
                }
            }
            line = reader.readLine()
        }
        reader.close()
        return entries
    }

    /** Legge ARP da /proc/net/arp. */
    private fun readArpFromProcFs(): List<ArpEntry> {
        val entries = mutableListOf<ArpEntry>()
        val file = java.io.File("/proc/net/arp")
        if (!file.exists() || !file.canRead()) return entries

        val reader = BufferedReader(InputStreamReader(java.io.FileInputStream(file)))
        reader.readLine() // header

        var line = reader.readLine()
        while (line != null) {
            val parts = line.trim().split("\\s+".toRegex())
            if (parts.size >= 4 && parts[2] == "0x2" && parts[3] != "00:00:00:00:00:00") {
                entries.add(ArpEntry(ipAddress = parts[0], macAddress = parts[3], interfaceName = parts.getOrElse(5) { "" }))
            }
            line = reader.readLine()
        }
        reader.close()
        return entries
    }
}

data class PingResult(
    val ipAddress: String,
    val isReachable: Boolean,
    val responseTimeMs: Long,
    val hostname: String?
)

data class ArpEntry(
    val ipAddress: String,
    val macAddress: String,
    val interfaceName: String
)
