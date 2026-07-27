package com.seeker.app.data.wifi

import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import com.seeker.app.core.extension.wifiManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestisce la scansione delle reti Wi-Fi.
 * Incapsula [WifiManager] e fornisce un'API reactive basata su Flow.
 */
@Singleton
class WifiScanner @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val wifiManager: WifiManager = context.wifiManager

    /**
     * Esegue una scansione Wi-Fi on-demand.
     * @return true se la scansione è stata avviata con successo.
     */
    suspend fun startScan(): Boolean = withContext(Dispatchers.IO) {
        try {
            wifiManager.startScan()
        } catch (e: SecurityException) {
            false
        }
    }

    /**
     * Ottiene l'ultimo elenco di risultati di scansione disponibili.
     */
    suspend fun getScanResults(): List<ScanResult> = withContext(Dispatchers.IO) {
        try {
            wifiManager.scanResults
                ?: emptyList()
        } catch (e: SecurityException) {
            emptyList()
        }
    }

    /**
     * Flow che emette i risultati della scansione ogni volta che la scansione si completa.
     * Utilizza un BroadcastReceiver interno.
     */
    fun observeScanResults(): Flow<List<ScanResult>> = callbackFlow {
        val receiver = WifiScanReceiver {
            trySend(wifiManager.scanResults ?: emptyList())
        }

        context.registerReceiver(
            receiver,
            receiver.intentFilter,
            Context.RECEIVER_EXPORTED
        )

        // Emetti immediatamente i risultati correnti se disponibili
        try {
            val currentResults = wifiManager.scanResults
            if (currentResults != null && currentResults.isNotEmpty()) {
                trySend(currentResults)
            }
        } catch (_: SecurityException) {
            // Permessi non concessi
        }

        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Verifica se la scansione Wi-Fi è disponibile.
     */
    fun isScanAvailable(): Boolean {
        return try {
            wifiManager.isScanAlwaysAvailable
        } catch (_: Exception) {
            false
        }
    }
}
