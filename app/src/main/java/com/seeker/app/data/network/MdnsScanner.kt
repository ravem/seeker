package com.seeker.app.data.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.lang.IndexOutOfBoundsException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.net.MulticastSocket
import java.net.SocketException
import java.net.SocketTimeoutException
import java.nio.charset.Charset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scanner mDNS a basso livello che invia query multicast DNS (224.0.0.251:5353)
 * per scoprire nomi host di dispositivi sulla rete locale.
 *
 * Ispirato da Ning (csicar/Ning) - LowLevelMDnsScanner.
 */
@Singleton
class MdnsScanner @Inject constructor() {

    companion object {
        private val TAG = MdnsScanner::class.java.simpleName
        private val MDNS_IP: InetAddress =
            Inet4Address.getByAddress(byteArrayOf(224.toByte(), 0, 0, 251.toByte()))
        private const val MDNS_PORT = 5353
        private const val SERVICE_PORT = 0
        private const val TIMEOUT_MS = 1500

        // Nomi host non validi da filtrare
        private val INVALID_HOSTNAMES = setOf(
            "local", "local.", "localhost", "localhost.",
            "lan", "home", "gateway", "router",
            "_services", "_dns-sd", "_udp"
        )

        /**
         * Tipi di servizio mDNS comuni da sondare.
         */
        val COMMON_SERVICES = listOf(
            "_workstation._tcp.local",
            "_companion-link._tcp.local",
            "_ssh._tcp.local",
            "_adisk._tcp.local",
            "_afpovertcp._tcp.local",
            "_device-info._tcp.local",
            "_googlecast._tcp.local",
            "_printer._tcp.local",
            "_ipp._tcp.local",
            "_http._tcp.local",
            "_smb._tcp.local",
            "_hap._tcp.local",
            "_coap._udp.local",
            "_services._dns-sd._udp.local"
        )
    }

    data class MdnsResult(
        val hostname: String,
        val ipAddress: InetAddress?,
        val port: Int
    )

    /**
     * Sonda tutti i servizi comuni e restituisce i risultati.
     */
    suspend fun probeAll(): List<MdnsResult> = withContext(Dispatchers.IO) {
        Log.d(TAG, "mDNS: probing ${COMMON_SERVICES.size} services...")
        val results = mutableListOf<MdnsResult>()

        COMMON_SERVICES.map { service ->
            async {
                val serviceResults = mutableListOf<MdnsResult>()
                try {
                    probeService(service) { result ->
                        serviceResults.add(result)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "mDNS probe failed for $service", e)
                }
                serviceResults
            }
        }.awaitAll().forEach { results.addAll(it) }

        Log.d(TAG, "mDNS: found ${results.size} results")
        results
    }

    /**
     * Sonda un singolo servizio mDNS.
     */
    private fun probeService(
        serviceName: String,
        onResult: (MdnsResult) -> Unit
    ) {
        Log.d(TAG, "mDNS: probing $serviceName")
        val request = createMdnsRequest(serviceName)

        val socket = MulticastSocket(SERVICE_PORT)
        try {
            socket.joinGroup(MDNS_IP)
            val packet = DatagramPacket(request, request.size, MDNS_IP, MDNS_PORT)
            socket.timeToLive = 2
            socket.soTimeout = TIMEOUT_MS
            socket.send(packet)

            val receiveBuffer = ByteArray(4096)
            while (true) {
                val receivePacket = DatagramPacket(receiveBuffer, receiveBuffer.size)
                socket.receive(receivePacket)
                val data = receivePacket.data.copyOfRange(0, receivePacket.length)
                val answers = parseResponse(data)

                // Estrae il nome host più significativo dalle risposte
                val hostname = extractBestHostname(answers)

                if (hostname != null) {
                    Log.d(TAG, "mDNS: resolved '$hostname' from $serviceName")
                    onResult(MdnsResult(hostname, receivePacket.address, receivePacket.port))
                }
            }
        } catch (e: SocketTimeoutException) {
            // Normale: timeout in attesa di risposte
        } catch (e: IndexOutOfBoundsException) {
            Log.e(TAG, "mDNS parse error for $serviceName", e)
        } catch (e: SocketException) {
            Log.w(TAG, "mDNS socket error for $serviceName", e)
        } finally {
            try {
                socket.leaveGroup(MDNS_IP)
            } catch (_: Exception) {}
            socket.close()
        }
    }

    /**
     * Estrae il miglior nome host dalle risposte DNS.
     *
     * Cerca in ordine:
     * 1. Nomi da record SRV (contengono il vero hostname)
     * 2. Nomi da record PTR che puntano a hostname reali (non servizi)
     * 3. Record A/AAAA (solo IP, scartati)
     *
     * Filtra nomi non validi come "local", "localhost", ecc.
     */
    private fun extractBestHostname(answers: List<DnsAnswer>): String? {
        // 1. Cerca record SRV — contengono il vero hostname
        val srvNames = answers.mapNotNull { answer ->
            if (answer.domainName.size >= 2) {
                // SRV domainName = [hostname, "local", ...]
                val first = answer.domainName.first()
                if (!first.startsWith("_") && first !in INVALID_HOSTNAMES) first else null
            } else null
        }
        if (srvNames.isNotEmpty()) {
            Log.d(TAG, "mDNS: SRV hostname = ${srvNames.first()}")
            return srvNames.first()
        }

        // 2. Cerca nomi da PTR che NON puntano a servizi (es. _service._tcp.local)
        //    ma a veri hostname (es. "my-iphone.local")
        val ptrNames = answers.mapNotNull { answer ->
            if (answer.domainName.size >= 2) {
                val parts = answer.domainName
                // Se il primo segmento inizia con _ è un servizio, lo saltiamo
                if (parts.first().startsWith("_")) return@mapNotNull null
                // Il primo segmento non underscore è il nome host
                val hostname = parts.first()
                if (hostname !in INVALID_HOSTNAMES && !hostname.startsWith("_")) hostname else null
            } else if (answer.domainName.size == 1) {
                val name = answer.domainName.first()
                // Singolo segmento: può essere "local" (invalido) o un nome valido
                if (name !in INVALID_HOSTNAMES && !name.startsWith("_") && !name.first().isDigit()) name else null
            } else null
        }
        if (ptrNames.isNotEmpty()) {
            Log.d(TAG, "mDNS: PTR hostname = ${ptrNames.first()}")
            return ptrNames.first()
        }

        return null
    }

    /**
     * Crea una richiesta mDNS per un nome di servizio.
     */
    private fun createMdnsRequest(serviceName: String): ByteArray {
        val nameBytes = serviceName
            .split(".")
            .flatMap { listOf(it.length.toByte()) + it.encodeToByteArray().toList() }
            .toByteArray()

        // Header (12 byte) + name + null terminator + QTYPE (2 byte) + QCLASS (2 byte)
        val header = byteArrayOf(
            0x00, 0x00,       // ID
            0x00, 0x00,       // Flags: standard query
            0x00, 0x01,       // Question count
            0x00, 0x00,       // Answer count
            0x00, 0x00,       // Authority count
            0x00, 0x00        // Additional count
        )
        val qtype = byteArrayOf(0x00, 0x0C) // PTR
        val qclass = byteArrayOf(0x80.toByte(), 0x01) // IN + unicast flag

        return header + nameBytes + byteArrayOf(0x00) + qtype + qclass
    }

    /**
     * Parsing della risposta DNS/mDNS.
     */
    private fun parseResponse(data: ByteArray): List<DnsAnswer> {
        if (data.size < 12) return emptyList()

        val questionCount = data.rangeToInt(4, 5)
        val answerCount = data.rangeToInt(6, 7)

        var index = 12

        // Salta le question entries
        repeat(questionCount) {
            val (nameEnd, _) = parseName(index, data)
            index = nameEnd + 1 + 4 // null terminator + QTYPE(2) + QCLASS(2)
        }

        val answers = mutableListOf<DnsAnswer>()
        val totalRecords = answerCount

        repeat(totalRecords) {
            val (newIndex, answer) = parseAnswer(index, data)
            index = newIndex
            if (answer != null) answers += answer
        }

        return answers
    }

    private fun ByteArray.rangeToInt(start: Int, end: Int): Int {
        var value = 0
        for (i in start..end) {
            value = (value shl 8) or (this[i].toInt() and 0xFF)
        }
        return value
    }

    private fun parseName(
        startIndex: Int,
        data: ByteArray,
        references: MutableMap<Int, List<String>> = mutableMapOf(),
        depth: Int = 0
    ): Pair<Int, List<String>> {
        val name = mutableListOf<String>()
        var i = startIndex
        var hitReference = false

        while (i < data.size && data[i] != 0x00.toByte() && !hitReference) {
            val length = data[i].toUByte().toInt()
            val referenceMask = 0b11000000

            if (length and referenceMask != 0) {
                // Puntatore a nome compresso
                val refIndex = (length and referenceMask.inv() shl 8) or (data[i + 1].toInt() and 0xFF)
                val refValue = references.getOrPut(refIndex) {
                    if (depth > 10) listOf("")
                    else parseName(refIndex, data, references, depth + 1).second
                }
                name += refValue
                hitReference = true
                i += 1
            } else {
                // Segmento di nome diretto
                val start = i + 1
                val end = start + length
                if (end <= data.size) {
                    name += String(data.copyOfRange(start, end), Charset.forName("UTF-8"))
                }
                i += length + 1
            }
        }

        return i to name
    }

    private fun parseAnswer(
        startIndex: Int,
        data: ByteArray
    ): Pair<Int, DnsAnswer?> {
        if (startIndex + 10 > data.size) return startIndex to null

        val (i, nameParts) = parseName(startIndex, data)
        if (i + 10 > data.size) return i to null

        val type = data.rangeToInt(i + 1, i + 2)
        val dataLength = data.rangeToInt(i + 9, i + 10)
        val dataIndex = i + 11

        if (dataIndex + dataLength > data.size) return i to null

        val name = nameParts.joinToString(".")

        val dnsAnswer = when (type) {
            1 -> { // A record
                if (dataLength >= 4) {
                    val ip = Inet4Address.getByAddress(data.copyOfRange(dataIndex, dataIndex + 4))
                    DnsAnswer(name, listOf(ip.hostAddress ?: ""), "")
                } else null
            }
            12 -> { // PTR record
                val (_, targetName) = parseName(dataIndex, data)
                DnsAnswer(name, targetName, "")
            }
            16 -> { // TXT record
                val txt = String(data.copyOfRange(dataIndex, dataIndex + dataLength), Charset.forName("UTF-8"))
                DnsAnswer(name, emptyList(), txt)
            }
            33 -> { // SRV record
                if (dataLength >= 6) {
                    val (_, targetName) = parseName(dataIndex + 6, data)
                    DnsAnswer(name, targetName, "")
                } else null
            }
            28 -> { // AAAA record
                if (dataLength >= 16) {
                    val ip = Inet6Address.getByAddress(data.copyOfRange(dataIndex, dataIndex + 16))
                    DnsAnswer(name, listOf(ip.hostAddress ?: ""), "")
                } else null
            }
            else -> null
        }

        return (i + 11 + dataLength) to dnsAnswer
    }

    data class DnsAnswer(
        val name: String,
        val domainName: List<String>,
        val txt: String
    )
}
