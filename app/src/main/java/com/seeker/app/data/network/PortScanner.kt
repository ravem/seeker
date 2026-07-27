package com.seeker.app.data.network

import android.util.Log
import com.seeker.app.core.model.PortInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import kotlin.system.measureTimeMillis
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scansione delle porte aperte su un dispositivo target.
 * Supporta TCP Connect scan e UDP probe scan (non necessita di root).
 *
 * Ispirato da Ning (csicar/Ning) - PortScanner.
 */
@Singleton
class PortScanner @Inject constructor() {

    companion object {
        private val TAG = PortScanner::class.java.simpleName

        /** Porte UDP comuni da sondare. */
        val UDP_PORTS = listOf(53, 67, 68, 123, 161, 162, 500, 514, 1900, 5353, 5355)

        /** Porte da 1 a 1024 (well-known). */
        val WELL_KNOWN_TCP = (1..1024).toList()
    }

    /**
     * Porte comuni scansionate di default (da [PortInfo.COMMON_PORTS]).
     */
    val defaultPorts: List<Int> = PortInfo.COMMON_PORTS.keys.sorted()

    /**
     * Porte da 1 a 1024 (well-known ports). Include [defaultPorts] più tutte le altre.
     */
    val wellKnownPorts: List<Int> = (1..1024).toList()

    /**
     * Scansiona una lista di porte TCP su un indirizzo IP target.
     *
     * @param ipAddress Indirizzo IP del target.
     * @param ports Lista delle porte da scansionare.
     * @param timeoutMs Timeout di connessione per ogni porta.
     * @param maxConcurrent Numero massimo di connessioni concorrenti.
     * @return Lista dei risultati della scansione.
     */
    suspend fun scanPorts(
        ipAddress: String,
        ports: List<Int> = defaultPorts,
        timeoutMs: Int = 500,
        maxConcurrent: Int = 20
    ): List<PortInfo> = withContext(Dispatchers.IO) {
        if (ports.isEmpty()) return@withContext emptyList()

        val results = mutableListOf<PortInfo>()

        // TCP scan
        coroutineScope {
            ports.chunked(maxConcurrent).flatMap { batch ->
                batch.map { port ->
                    async {
                        scanTcpPort(ipAddress, port, timeoutMs)
                    }
                }.map { it.await() }
            }
        }.let { results.addAll(it) }

        results
    }

    /**
     * Scansione TCP e UDP combo su porte ben note.
     *
     * Usa [wellKnownPorts] per TCP (1-1024) e [UDP_PORTS] per UDP.
     * Per performance, le porte TCP usano timeout breve (200ms) con alta concorrenza (50),
     * mentre le UDP usano timeout standard (500ms) con concorrenza ridotta (10).
     *
     * @param onProgress Callback opzionale per aggiornamenti di progresso (porta corrente, totale)
     */
    suspend fun scanPortsWithUdp(
        ipAddress: String,
        tcpPorts: List<Int> = wellKnownPorts,
        udpPorts: List<Int> = UDP_PORTS,
        timeoutMs: Int = 500,
        maxConcurrent: Int = 20,
        onProgress: ((current: Int, total: Int, port: Int, transport: String) -> Unit)? = null
    ): List<PortInfo> = withContext(Dispatchers.IO) {
        val results = mutableListOf<PortInfo>()
        val totalPorts = tcpPorts.size + udpPorts.size
        var scannedCount = 0

        val updateProgress = { port: Int, transport: String ->
            scannedCount++
            onProgress?.invoke(scannedCount, totalPorts, port, transport)
        }

        // TCP scan full (1-1024) con alta concorrenza
        val tcpBatchSize = if (tcpPorts.size > 100) 50 else maxConcurrent
        coroutineScope {
            tcpPorts.chunked(tcpBatchSize).flatMap { batch ->
                batch.map { port ->
                    async {
                        val timeout = if (tcpPorts.size > 100) 150 else timeoutMs
                        val result = scanTcpPort(ipAddress, port, timeout)
                        updateProgress(port, "TCP")
                        result
                    }
                }.map { it.await() }
            }
        }.let { results.addAll(it) }

        // UDP scan (solo porte selezionate, concorrenza ridotta)
        val udpBatchSize = (tcpBatchSize / 2).coerceIn(5, 10)
        coroutineScope {
            udpPorts.chunked(udpBatchSize).flatMap { batch ->
                batch.map { port ->
                    async {
                        val result = scanUdpPort(ipAddress, port, timeoutMs)
                        updateProgress(port, "UDP")
                        result
                    }
                }.map { it.await() }
            }
        }.let { results.addAll(it) }

        results.sortedBy { it.port }
    }

    /**
     * Scansione TCP di una singola porta.
     */
    private fun scanTcpPort(ipAddress: String, port: Int, timeoutMs: Int): PortInfo {
        var isOpen = false
        var responseTime: Long? = null

        val elapsed = measureTimeMillis {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(ipAddress, port), timeoutMs)
                socket.close()
                isOpen = true
            } catch (_: Exception) {
                // Porta chiusa o filtrata
            }
        }

        return PortInfo(
            port = port,
            service = PortInfo.serviceName(port),
            isOpen = isOpen,
            transport = "TCP",
            responseTimeMs = if (isOpen) elapsed else null
        )
    }

    /**
     * Scansione UDP di una singola porta.
     * Invia un pacchetto UDP vuoto e aspetta:
     * - ICMP Port Unreachable -> porta chiusa
     * - Nessuna risposta -> porta probabilmente aperta/filtrata
     * - Risposta -> porta aperta
     *
     * Nota: l'UDP scan è meno affidabile del TCP perché UDP è connectionless.
     */
    private fun scanUdpPort(ipAddress: String, port: Int, timeoutMs: Int): PortInfo {
        var isOpen = false
        var responseTime: Long? = null

        val elapsed = measureTimeMillis {
            try {
                val socket = DatagramSocket()
                socket.soTimeout = timeoutMs
                val sendData = ByteArray(0) // Pacchetto vuoto per sondare
                val sendPacket = DatagramPacket(sendData, sendData.size, InetSocketAddress(ipAddress, port))
                socket.send(sendPacket)

                // Se riceviamo una risposta, la porta è aperta
                val receiveData = ByteArray(128)
                val receivePacket = DatagramPacket(receiveData, receiveData.size)
                try {
                    socket.receive(receivePacket)
                    isOpen = true
                    Log.d(TAG, "UDP port $port open on $ipAddress (got response)")
                } catch (e: SocketTimeoutException) {
                    // Timeout: porta potrebbe essere aperta (nessuna risposta UDP)
                    // Non possiamo esserne certi, la segnaliamo come "possibile apertura"
                    isOpen = true
                    Log.d(TAG, "UDP port $port possibly open on $ipAddress (timeout)")
                }
                socket.close()
            } catch (e: Exception) {
                // Porta probabilmente chiusa
                Log.d(TAG, "UDP port $port closed on $ipAddress: ${e.message}")
            }
        }

        return PortInfo(
            port = port,
            service = PortInfo.serviceName(port) + " (UDP)",
            isOpen = isOpen,
            transport = "UDP",
            responseTimeMs = if (isOpen) elapsed else null
        )
    }
}
