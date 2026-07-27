package com.seeker.app.data.network

import android.util.Log
import com.seeker.app.core.model.LanDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.net.InetAddress
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Esegue risoluzione DNS inversa (PTR record) per dispositivi senza hostname.
 * Serve come fallback quando mDNS non riesce a risolvere il nome.
 *
 * Ispirato da Ning (csicar/Ning) - HostnameScanner.
 */
@Singleton
class HostnameResolver @Inject constructor() {

    companion object {
        private val TAG = HostnameResolver::class.java.simpleName
        private const val CHUNK_SIZE = 10

        // Suffissi DNS da rimuovere dai nomi risolti
        private val DNS_SUFFIXES = listOf(
            ".local", ".local.", ".lan", ".lan.",
            ".home", ".home.", ".internal", ".internal."
        )

        // Nomi host non significativi da filtrare
        private val INVALID_NAMES = setOf(
            "local", "local.", "localhost", "localhost.",
            "lan", "home", "gateway", "router"
        )
    }

    data class ResolutionResult(
        val ipAddress: String,
        val dnsName: String
    )

    /**
     * Esegue reverse DNS lookup per una lista di dispositivi.
     * Solo per quelli che non hanno già un hostname.
     */
    suspend fun resolveMissing(devices: List<LanDevice>): List<ResolutionResult> =
        withContext(Dispatchers.IO) {
            val targets = devices.filter { it.hostname == null && it.dnsName == null }

            targets
                .chunked(CHUNK_SIZE)
                .map { chunk ->
                    async {
                        chunk.mapNotNull { device ->
                            resolveSingle(device.ipAddress)
                        }
                    }
                }.awaitAll()
                .flatten()
        }

    /**
     * Esegue reverse DNS per un singolo IP.
     */
    suspend fun resolveSingle(ipAddress: String): ResolutionResult? =
        withContext(Dispatchers.IO) {
            try {
                val addr = InetAddress.getByName(ipAddress)
                var hostname = addr.canonicalHostName

                // canonicalHostName restituisce l'IP se il lookup fallisce
                if (hostname == ipAddress || hostname.isBlank()) return@withContext null

                // Rimuovi suffissi .local, .lan, .home, ecc.
                for (suffix in DNS_SUFFIXES) {
                    if (hostname.endsWith(suffix, ignoreCase = true)) {
                        hostname = hostname.substring(0, hostname.length - suffix.length)
                        break
                    }
                }

                // Se dopo la pulizia rimane solo il suffisso (es. "local"), scarta
                hostname = hostname.trimEnd('.')
                if (hostname.isBlank() || hostname in INVALID_NAMES) {
                    Log.d(TAG, "Reverse DNS: $ipAddress -> scartato (nome non valido: '$hostname')")
                    return@withContext null
                }

                Log.d(TAG, "Reverse DNS: $ipAddress -> $hostname")
                ResolutionResult(ipAddress, hostname)
            } catch (e: Exception) {
                Log.d(TAG, "Reverse DNS failed for $ipAddress: ${e.message}")
                null
            }
        }

    /**
     * Versione batch: risolve tutti i dispositivi senza nome.
     * Restituisce la lista aggiornata.
     */
    suspend fun resolveAll(devices: List<LanDevice>): List<LanDevice> {
        val results = resolveMissing(devices)
        if (results.isEmpty()) return devices

        val map = results.associateBy { it.ipAddress }
        return devices.map { device ->
            val dnsName = map[device.ipAddress]?.dnsName
            if (dnsName != null && device.hostname == null) {
                device.copy(dnsName = dnsName)
            } else {
                device
            }
        }
    }
}
