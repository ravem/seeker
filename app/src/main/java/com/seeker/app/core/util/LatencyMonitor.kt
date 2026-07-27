package com.seeker.app.core.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import kotlin.system.measureTimeMillis
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Misura la latenza (ping) verso un host.
 */
@Singleton
class LatencyMonitor @Inject constructor() {

    /**
     * Esegue un ping verso un indirizzo IP e restituisce la latenza in ms.
     * @return latenza in ms, o null se l'host non è raggiungibile.
     */
    suspend fun ping(host: String, timeoutMs: Int = 3000): Long? = withContext(Dispatchers.IO) {
        try {
            val elapsed = measureTimeMillis {
                val inet = InetAddress.getByName(host)
                if (!inet.isReachable(timeoutMs)) {
                    return@withContext null
                }
            }
            elapsed.coerceAtLeast(1L)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Esegue un ping system (ICMP) via processo esterno.
     * Più accurato di [InetAddress.isReachable] che a volte fallisce.
     */
    suspend fun pingSystem(host: String, count: Int = 1): PingStats? = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec("ping -c $count -W 2 $host")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            reader.close()
            process.waitFor()

            // Parsa output: "rtt min/avg/max/mdev = 10.123/12.456/15.789/2.345 ms"
            val stats = parsePingOutput(output)
            stats
        } catch (_: Exception) {
            null
        }
    }

    private fun parsePingOutput(output: String): PingStats? {
        try {
            // Cerca la riga "rtt min/avg/max/mdev = ..."
            val rttLine = output.lineSequence().find { it.contains("rtt") && it.contains("=") } ?: return null
            val values = rttLine.substringAfter("=").substringBefore("ms").trim().split("/")
            if (values.size >= 3) {
                return PingStats(
                    minMs = values[0].toFloatOrNull() ?: 0f,
                    avgMs = values[1].toFloatOrNull() ?: 0f,
                    maxMs = values[2].toFloatOrNull() ?: 0f
                )
            }
        } catch (_: Exception) {}
        return null
    }
}

data class PingStats(
    val minMs: Float,
    val avgMs: Float,
    val maxMs: Float
)
