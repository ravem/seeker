package com.seeker.app.data.network

import android.util.Log
import com.seeker.app.core.model.LanDevice
import com.seeker.app.core.model.PortInfo
import com.seeker.app.core.util.NetworkUtils
import com.seeker.app.data.network.NetbiosScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val BATCH_SIZE = 50

private val INVALID_HOSTNAMES = setOf(
    "local", "local.", "localhost", "localhost.",
    "lan", "home", "gateway", "router",
    "_services", "_dns-sd", "_workstation", "_tcp"
)

/**
 * Repository per la scansione della rete locale.
 *
 * Fasi:
 * 1. Ping sweep + ARP (best-effort per MAC)
 * 2. mDNS discovery
 * 3. Reverse DNS
 * 4. Port scan su well-known ports
 * 5. SNMP scan (se la porta 161 è aperta)
 */
@Singleton
class NetworkRepository @Inject constructor(
    private val pingScanner: PingScanner,
    private val portScanner: PortScanner,
    private val mdnsScanner: MdnsScanner,
    private val hostnameResolver: HostnameResolver,
    private val snmpScanner: SnmpScanner,
    private val netbiosScanner: NetbiosScanner
) {
    private val _scanProgress = MutableStateFlow<ScanProgress>(ScanProgress.Idle)
    val scanProgress: Flow<ScanProgress> = _scanProgress.asStateFlow()

    private val _scanResults = MutableStateFlow<List<LanDevice>>(emptyList())
    val scanResults: Flow<List<LanDevice>> = _scanResults.asStateFlow()

    /**
     * Scansione rete locale con fasi concorrenti.
     */
    suspend fun scanLocalNetwork(): List<LanDevice> = withContext(Dispatchers.IO) {
        _scanProgress.value = ScanProgress.Scanning("Rilevamento subnet…")

        val ipInfo = NetworkUtils.getLocalIpInfo()
        if (ipInfo == null) {
            _scanProgress.value = ScanProgress.Error("Impossibile determinare la subnet")
            return@withContext emptyList()
        }
        val (localIp, prefixLength) = ipInfo
        val subnetIps = NetworkUtils.getSubnetIps(localIp, prefixLength)
        if (subnetIps.isEmpty()) {
            _scanProgress.value = ScanProgress.Error("Subnet troppo grande o non valida")
            return@withContext emptyList()
        }

        // Lancia mDNS in parallelo (indipendente dal ping)
        val mdnsDeferred = async { mdnsScanner.probeAll() }

        // Leggi ARP table
        _scanProgress.value = ScanProgress.Scanning("Lettura tabella ARP…")
        val arpEntries = pingScanner.readArpTable()
        Log.d("SeekerLAN", "ARP entries: ${arpEntries.size}")
        val arpCount = arpEntries.count { it.macAddress != "00:00:00:00:00:00" }
        _scanProgress.value = ScanProgress.Scanning("Lettura tabella ARP: $arpCount dispositivi trovati")

        val arpMap = arpEntries.associateBy { it.ipAddress }
        val allDevices = arpEntries.mapNotNull { arp ->
            val mac = arp.macAddress.takeIf { it != "00:00:00:00:00:00" && it.isNotBlank() }
            LanDevice(
                ipAddress = arp.ipAddress,
                macAddress = mac ?: "",
                hostname = null,
                isReachable = true
            )
        }.toMutableList()
        val foundIps = allDevices.map { it.ipAddress }.toMutableSet()
        val totalIps = subnetIps.size

        if (allDevices.isNotEmpty()) {
            _scanResults.value = allDevices.toList()
        }
        _scanProgress.value = ScanProgress.Scanning(
            "Scansione rete: $totalIps IP - ${allDevices.size} trovati"
        )

        // Ping sweep in batch
        var batchIndex = 0
        subnetIps.chunked(BATCH_SIZE).forEach { batch ->
            val results = pingScanner.scanSubnet(batch, timeoutMs = 300, maxConcurrent = 30)
            var added = false
            for (result in results) {
                if (result.ipAddress in foundIps) continue
                foundIps.add(result.ipAddress)
                val arpEntry = arpMap[result.ipAddress]
                val mac = arpEntry?.macAddress?.takeIf { it != "00:00:00:00:00:00" && it.isNotBlank() } ?: ""
                allDevices.add(
                    LanDevice(
                        ipAddress = result.ipAddress,
                        macAddress = mac,
                        hostname = result.hostname,
                        isReachable = true
                    )
                )
                added = true
            }
            batchIndex++
            if (added || batchIndex % 5 == 0) {
                val cnt = allDevices.size
                Log.d("SeekerLAN", "Emit batch: $cnt devices, batch=$batchIndex")
                _scanResults.value = allDevices.toList()
                val scanned = (batchIndex * BATCH_SIZE).coerceAtMost(totalIps)
                _scanProgress.value = ScanProgress.Scanning(
                    "Scansione rete: $scanned/$totalIps IP - ${allDevices.size} trovati"
                )
            }
        }

        Log.d("SeekerLAN", "Ping sweep done: ${allDevices.size} devices")

        // Unisci risultati mDNS
        _scanProgress.value = ScanProgress.Scanning("Unione risultati mDNS…")
        try {
            val mdnsResults = mdnsDeferred.await()
            if (mdnsResults.isNotEmpty()) {
                val mdnsMap = mdnsResults
                    .filter { it.ipAddress != null }
                    .groupBy { it.ipAddress!!.hostAddress }
                    .mapValues { (_, results) -> results.first().hostname }

                allDevices.forEachIndexed { i, device ->
                    val mdnsHostname = mdnsMap[device.ipAddress]
                    if (mdnsHostname != null && device.hostname == null
                        && mdnsHostname !in INVALID_HOSTNAMES && !mdnsHostname.startsWith("_")
                    ) {
                        var cleanName = mdnsHostname.trimEnd('.')
                        if (cleanName.endsWith(".local", ignoreCase = true)) {
                            cleanName = cleanName.removeSuffix(".local").removeSuffix(".local.")
                        }
                        if (cleanName !in INVALID_HOSTNAMES && cleanName.isNotBlank()) {
                            allDevices[i] = device.copy(hostname = cleanName)
                        }
                    }
                }
                _scanResults.value = allDevices.toList()
            }
        } catch (e: Exception) {
            Log.w("SeekerLAN", "mDNS probe failed", e)
        }

        // NetBIOS name resolution (per dispositivi Windows)
        _scanProgress.value = ScanProgress.Scanning("Risoluzione nomi NetBIOS…")
        try {
            val ipsToQuery = allDevices.filter { it.hostname == null && it.dnsName == null }
                .map { it.ipAddress }
            if (ipsToQuery.isNotEmpty()) {
                val netbiosResults = netbiosScanner.resolveAll(ipsToQuery)
                var netbiosUpdated = false
                allDevices.forEachIndexed { i, device ->
                    val nbResult = netbiosResults.find { it.ipAddress == device.ipAddress }
                    if (nbResult != null && nbResult.netbiosName != null && device.hostname == null && device.dnsName == null) {
                        allDevices[i] = device.copy(
                            hostname = nbResult.netbiosName,
                            vendor = if (nbResult.isWindows && device.vendor == null) "Microsoft Windows" else device.vendor
                        )
                        netbiosUpdated = true
                    }
                }
                if (netbiosUpdated) _scanResults.value = allDevices.toList()
            }
        } catch (e: Exception) {
            Log.w("SeekerLAN", "NetBIOS probe failed", e)
        }

        // Reverse DNS (in parallelo con eventuali altre operazioni)
        _scanProgress.value = ScanProgress.Scanning("Risoluzione DNS inversa…")
        try {
            val dnsResults = hostnameResolver.resolveMissing(allDevices)
            if (dnsResults.isNotEmpty()) {
                val dnsMap = dnsResults.associateBy { it.ipAddress }
                var dnsUpdated = false
                allDevices.forEachIndexed { i, device ->
                    val dns = dnsMap[device.ipAddress]
                    if (dns != null && device.hostname == null && device.dnsName == null) {
                        val cleanName = dns.dnsName.trimEnd('.')
                            .removeSuffix(".local").removeSuffix(".local.")
                        if (cleanName !in INVALID_HOSTNAMES && cleanName.isNotBlank()) {
                            allDevices[i] = device.copy(dnsName = cleanName)
                            dnsUpdated = true
                        }
                    }
                }
                if (dnsUpdated) _scanResults.value = allDevices.toList()
            }
        } catch (e: Exception) {
            Log.w("SeekerLAN", "Reverse DNS failed", e)
        }

        // Port scan rapido (well-known: 22, 80, 443) + SNMP
        _scanProgress.value = ScanProgress.Scanning("Scansione porte…")
        val devicesToScan = allDevices.filter { it.isReachable }
        if (devicesToScan.isNotEmpty()) {
            coroutineScope {
                devicesToScan.map { device ->
                    async {
                        val ip = device.ipAddress
                        try {
                            // Port scan rapido
                            val quickPorts = portScanner.scanPorts(
                                ip, listOf(22, 80, 443),
                                timeoutMs = 200, maxConcurrent = 3
                            )
                            val openPorts = quickPorts.filter { it.isOpen }

                                ip to PortScanResult(openPorts, null)
                        } catch (_: Exception) { null }
                    }
                }.awaitAll().forEach { result ->
                    if (result != null) {
                        val (ip, scanResult) = result
                        val idx = allDevices.indexOfFirst { it.ipAddress == ip }
                        if (idx >= 0) {
                            allDevices[idx] = allDevices[idx].copy(
                                ports = scanResult.openPorts
                            )
                        }
                    }
                }
            }
            _scanResults.value = allDevices.toList()
        }

        _scanResults.value = allDevices.toList()
        _scanProgress.value = ScanProgress.Complete(allDevices.toList())
        allDevices.toList()
    }

    /**
     * Scansione porte completa su un dispositivo (TCP 1-1024 + UDP) + SNMP.
     * Emette aggiornamenti di progresso via [scanProgress].
     */
    suspend fun scanDevicePorts(device: LanDevice, ports: List<Int> = portScanner.wellKnownPorts): List<PortInfo> = withContext(Dispatchers.IO) {
        val ip = device.ipAddress
        val totalPorts = ports.size + PortScanner.UDP_PORTS.size
        _scanProgress.value = ScanProgress.Scanning("Scansione porte su $ip ($totalPorts porte)...")

        // Port scan con progresso
        val results = portScanner.scanPortsWithUdp(
            ipAddress = ip,
            tcpPorts = ports,
            onProgress = { current, total, port, transport ->
                _scanProgress.value = ScanProgress.PortScanning(
                    ipAddress = ip,
                    port = port,
                    total = total,
                    transport = transport
                )
            }
        )
        val openPorts = results.filter { it.isOpen }

        // SNMP scan (timeout 1.5s, senza retry)
        _scanProgress.value = ScanProgress.Scanning("SNMP su $ip (1.5s timeout)...")
        val snmpInfo = snmpScanner.scan(ip)
        val vendor = snmpInfo?.detectedVendor ?: device.vendor

        val updatedDevices = _scanResults.value.map {
            if (it.ipAddress == ip) it.copy(
                ports = results,
                snmpInfo = snmpInfo,
                vendor = vendor
            ) else it
        }
        _scanResults.value = updatedDevices
        _scanProgress.value = ScanProgress.PortScanComplete(ip, openPorts.size)
        results
    }

    fun reset() {
        _scanProgress.value = ScanProgress.Idle
        _scanResults.value = emptyList()
    }
}

private data class PortScanResult(
    val openPorts: List<PortInfo>,
    val snmpInfo: com.seeker.app.core.model.SnmpInfo?
)

sealed class ScanProgress {
    data object Idle : ScanProgress()
    data class Scanning(val message: String) : ScanProgress()
    data class PortScanning(val ipAddress: String, val port: Int, val total: Int, val transport: String) : ScanProgress()
    data class Complete(val devices: List<LanDevice>) : ScanProgress()
    data class PortScanComplete(val ipAddress: String, val openPortCount: Int) : ScanProgress()
    data class Error(val message: String) : ScanProgress()
}
