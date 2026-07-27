package com.seeker.app.data.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SeekerNetBIOS"

/**
 * Scanner NetBIOS Name Service (UDP 137).
 *
 * Invia query NBSTAT (Node Status) per ottenere il nome NetBIOS
 * di un dispositivo. Funziona tipicamente su host Windows con
 * NetBIOS su TCP/IP abilitato.
 */
@Singleton
class NetbiosScanner @Inject constructor() {

    companion object {
        private const val NETBIOS_PORT = 137
        private const val TIMEOUT_MS = 1500

        // Transaction ID fisso (potrebbe servire random)
        private const val TX_ID = 0x0010
    }

    data class NetbiosResult(
        val ipAddress: String,
        val netbiosName: String?,
        val isWindows: Boolean = false
    )

    /**
     * Tenta risoluzione NetBIOS via NBSTAT (Node Status) query.
     */
    suspend fun resolve(ipAddress: String): NetbiosResult = withContext(Dispatchers.IO) {
        Log.d(TAG, "resolve: probing $ipAddress ...")
        try {
            val targetAddr = InetAddress.getByName(ipAddress)
            val socket = DatagramSocket()
            socket.soTimeout = TIMEOUT_MS

            try {
                // Costruisce pacchetto NBSTAT query
                val requestPacket = buildNbstatRequest(targetAddr)
                socket.send(requestPacket)

                // Riceve risposta
                val buf = ByteArray(1024)
                val responsePacket = DatagramPacket(buf, buf.size)
                try {
                    socket.receive(responsePacket)
                    val data = responsePacket.data.copyOfRange(0, responsePacket.length)
                    Log.d(TAG, "resolve: $ipAddress -> risposta ${responsePacket.length} bytes")

                    val result = parseNbstatResponse(data, ipAddress)
                    Log.d(TAG, "resolve: $ipAddress -> ${result?.netbiosName ?: "nome non trovato"}")
                    result ?: NetbiosResult(ipAddress, null)
                } catch (e: SocketTimeoutException) {
                    Log.d(TAG, "resolve: $ipAddress -> timeout")
                    NetbiosResult(ipAddress, null)
                }
            } finally {
                try { socket.close() } catch (_: Exception) { }
            }
        } catch (e: Exception) {
            Log.d(TAG, "resolve: $ipAddress -> errore: ${e.message}")
            NetbiosResult(ipAddress, null)
        }
    }

    suspend fun resolveAll(ipAddresses: List<String>): List<NetbiosResult> = withContext(Dispatchers.IO) {
        if (ipAddresses.isEmpty()) return@withContext emptyList()
        coroutineScope {
            ipAddresses.map { ip -> async { resolve(ip) } }.awaitAll()
        }
    }

    /**
     * Costruisce una richiesta NBSTAT (Node Status) per un IP target.
     *
     * Formato:
     * [12 byte header]
     * [1 byte name_length = 0x20] [32 bytes encoded "CBAAAAAAAAAAAAAAA" + 0x00]
     * [2 byte type = 0x0021 (NBSTAT)]
     * [2 byte class = 0x0001 (IN)]
     */
    private fun buildNbstatRequest(targetAddr: InetAddress): DatagramPacket {
        // Nome NetBIOS per NBSTAT: "*" (wildcard, tutti i nomi)
        // Encoded: nome uppercase + padded a 15 + type (0x00) + encoded to 32 bytes
        val encodedName = encodeNetbiosWildcardName()

        // Header DNS/NetBIOS (12 bytes)
        val header = byteArrayOf(
            (TX_ID shr 8).toByte(), TX_ID.toByte(),  // Transaction ID
            0x00, 0x00,  // Flags: standard query (0x0000)
            0x00, 0x01,  // Questions: 1
            0x00, 0x00,  // Answer RRs: 0
            0x00, 0x00,  // Authority RRs: 0
            0x00, 0x00   // Additional RRs: 0
        )

        // Question section: encoded name + type NBSTAT (0x0021) + class IN (0x0001)
        val question = encodedName + byteArrayOf(
            0x00, 0x21,  // QTYPE: NBSTAT (Node Status)
            0x00, 0x01   // QCLASS: IN
        )

        val packetData = header + question
        return DatagramPacket(packetData, packetData.size, targetAddr, NETBIOS_PORT)
    }

    /**
     * Codifica il nome wildcard "*" per NBSTAT.
     *
     * Il nome "*" va uppercase, padded a 15 chars + type byte (0x00),
     * poi ogni byte viene diviso in due semi-byte + 0x41.
     * Risultato: 1 byte length (0x20) + 32 bytes encoded.
     */
    private fun encodeNetbiosWildcardName(): ByteArray {
        val raw = "*".padEnd(16, ' ')  // 15 chars + 1 type (0x00)
        val encoded = ByteArray(32)
        for (i in 0 until 16) {
            val c = raw[i].code
            encoded[i * 2] = ((c shr 4) + 0x41).toByte()
            encoded[i * 2 + 1] = ((c and 0x0F) + 0x41).toByte()
        }
        return byteArrayOf(0x20) + encoded
    }

    /**
     * Parsa la risposta NBSTAT.
     *
     * Struttura:
     * [12 byte header]
     * [encoded name + type + class same as query]
     * [2 byte answer count]
     * For each answer:
     *   [2 byte name pointer 0xC00C]
     *   [2 byte type]
     *   [2 byte class]
     *   [4 byte TTL]
     *   [2 byte data length]
     *   [1 byte number of names]
     *   For each name:
     *     [15 bytes name encoded]
     *     [1 byte type (0x00=workstation, 0x03=messenger, 0x20=server)]
     *     [2 byte flags]
     *   [6 byte adapter address (MAC)]
     */
    private fun parseNbstatResponse(data: ByteArray, ipAddress: String): NetbiosResult? {
        if (data.size < 12) {
            Log.d(TAG, "parse: risposta troppo corta (${data.size} bytes)")
            return null
        }

        val transactionId = data.rangeToInt(0, 1)
        val flags = data.rangeToInt(2, 3)
        val questions = data.rangeToInt(4, 5)
        val answers = data.rangeToInt(6, 7)

        Log.d(TAG, "parse: TX_ID=0x${transactionId.toString(16)} flags=0x${flags.toString(16)} answers=$answers")

        // Controlla se è un errore (name error = bit 4 nel flags)
        if (flags and 0x000F == 0x0003) {
            Log.d(TAG, "parse: Name Error (NXDOMAIN) - nome non trovato")
            return null
        }

        if (answers == 0) {
            Log.d(TAG, "parse: nessuna answer")
            return null
        }

        var offset = 12

        // Salta la question section (encoded name + 4 bytes type/class)
        // La question section inizia con 0x20 (length)
        if (offset < data.size && data[offset].toInt() == 0x20) {
            offset += 1 + 32 + 4  // length(1) + encoded_name(32) + qtype(2) + qclass(2)
        } else {
            // Prova a saltare fino a trovare la risposta
            // Alcuni server non replicano la question
        }

        // Parsa le answer sections
        for (a in 0 until answers) {
            if (offset + 12 > data.size) break

            // Name pointer (2 byte, tipicamente 0xC00C)
            val namePtr = data.rangeToInt(offset, offset + 1)

            // Log per debug
            Log.d(TAG, "parse: answer $a at offset $offset, namePtr=0x${namePtr.toString(16)}")

            if (namePtr and 0xC000 == 0xC000) {
                offset += 2 // name compressed pointer
            } else {
                // Nome inline (raro)
                val nameLen = data[offset].toInt() and 0xFF
                offset += 1 + nameLen
            }

            if (offset + 10 > data.size) break

            val type = data.rangeToInt(offset, offset + 1)
            val rrClass = data.rangeToInt(offset + 2, offset + 3)
            val ttl = data.rangeToInt(offset + 4, offset + 7)
            val rdLength = data.rangeToInt(offset + 8, offset + 9)
            offset += 10

            Log.d(TAG, "parse:   type=0x${type.toString(4)} class=0x${rrClass.toString(4)} ttl=$ttl rdLength=$rdLength")

            if (offset + rdLength > data.size) break

            // NBSTAT response (type 0x0021)
            if (type == 0x0021 && rdLength >= 1) {
                val numNames = data[offset].toInt() and 0xFF
                Log.d(TAG, "parse:   NBSTAT: $numNames names")

                var netbiosName: String? = null
                var isWindows = false

                for (j in 0 until numNames.coerceAtMost(50)) {
                    val nameOffset = offset + 1 + j * 18
                    if (nameOffset + 18 > data.size) break

                    val decodedName = decodeNetbiosName(data, nameOffset)
                    val nameType = data[nameOffset + 15].toInt() and 0xFF

                    Log.d(TAG, "parse:     name[$j]='$decodedName' type=0x${nameType.toString(16)}")

                    // 0x00 = Workstation (computer name)
                    if (nameType == 0x00 && netbiosName == null) {
                        netbiosName = decodedName
                    }
                    // 0x00, 0x03, 0x20 = Windows tipici
                    if (nameType in listOf(0x00, 0x03, 0x20)) {
                        isWindows = true
                    }
                }

                if (netbiosName != null) {
                    Log.d(TAG, "parse: nome trovato: $netbiosName, isWindows=$isWindows")
                    return NetbiosResult(ipAddress, netbiosName, isWindows)
                }
            }

            offset += rdLength
        }

        return null
    }

    /**
     * Decodifica un nome NetBIOS da 16 bytes (15 nome + 1 type).
     * Ogni byte è codificato come 2 semi-byte + 0x41.
     */
    private fun decodeNetbiosName(data: ByteArray, offset: Int): String {
        val sb = StringBuilder()
        for (i in 0 until 15) {
            val idx = offset + i * 2
            if (idx + 1 >= data.size) break
            val high = (data[idx].toInt() and 0xFF) - 0x41
            val low = (data[idx + 1].toInt() and 0xFF) - 0x41
            if (high < 0 || low < 0) break
            val c = (high shl 4) or low
            if (c == 0x20 || c == 0x00) break
            sb.append(c.toChar())
        }
        return sb.toString().trim()
    }

    private fun ByteArray.rangeToInt(start: Int, end: Int): Int {
        var v = 0
        for (i in start..end.coerceAtMost(size - 1)) {
            v = (v shl 8) or (this[i].toInt() and 0xFF)
        }
        return v
    }
}
