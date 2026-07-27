package com.seeker.app.data.wifi

import android.util.Log
import com.seeker.app.core.model.AccessPoint
import com.seeker.app.core.model.AccessPointGroup
import com.seeker.app.core.model.ConnectedNetwork
import com.seeker.app.core.model.toAccessPoint
import com.seeker.app.core.util.OuiDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context

private const val TAG = "SeekerWifiRepo"

/**
 * Repository che unifica l'accesso ai dati Wi-Fi.
 * Arricchisce i dati con vendor OUI (locale + hostname + API online + cache).
 */
@Singleton
class WifiRepository @Inject constructor(
    private val wifiScanner: WifiScanner,
    private val wifiConnectionInfo: WifiConnectionInfo,
    private val ouiDatabase: OuiDatabase,
    @ApplicationContext private val context: Context
) {
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── Connessione Attuale ──

    fun getCurrentConnection(): ConnectedNetwork? {
        val conn = wifiConnectionInfo.getCurrentConnection()
        return enrichWithVendor(conn)
    }

    fun observeCurrentConnection(): Flow<ConnectedNetwork?> =
        wifiConnectionInfo.observeConnection().map { conn -> enrichWithVendor(conn) }

    private fun enrichWithVendor(conn: ConnectedNetwork?): ConnectedNetwork? {
        if (conn == null) return null
        if (conn.apVendor != null) return conn
        val vendor = lookupVendor(conn.bssid, null)
        val enriched = if (vendor != null) conn.copy(apVendor = vendor) else conn
        // Se non trovato, avvia lookup online in background (il prossimo poll lo troverà in cache)
        if (vendor == null) {
            launchOnlineLookup(conn.bssid, null)
        }
        return enriched
    }

    // ── Scansione Access Point ──

    suspend fun startScan(): Boolean = wifiScanner.startScan()

    suspend fun getAccessPoints(): List<AccessPoint> {
        val results = wifiScanner.getScanResults()
        return results.map { result ->
            enrichAp(result.toAccessPoint(), result.BSSID)
        }
    }

    fun observeAccessPoints(): Flow<List<AccessPoint>> =
        wifiScanner.observeScanResults().map { results ->
            results.map { result ->
                enrichAp(result.toAccessPoint(), result.BSSID)
            }
        }

    suspend fun getAccessPointGroups(): List<AccessPointGroup> {
        val aps = getAccessPoints()
        return groupBySsid(aps)
    }

    fun observeAccessPointGroups(): Flow<List<AccessPointGroup>> =
        observeAccessPoints().map { aps -> groupBySsid(aps) }

    private fun groupBySsid(accessPoints: List<AccessPoint>): List<AccessPointGroup> {
        return accessPoints
            .groupBy { it.ssid }
            .map { (ssid, aps) ->
                AccessPointGroup(ssid = ssid, accessPoints = aps.sortedByDescending { it.signalStrengthDbm })
            }
            .sortedByDescending { it.bestSignal }
    }

    private fun enrichAp(ap: AccessPoint, bssid: String?): AccessPoint {
        if (ap.vendor != null) return ap
        val vendor = lookupVendor(bssid, null)
        val enriched = if (vendor != null) ap.copy(vendor = vendor) else ap
        if (vendor == null) {
            launchOnlineLookup(bssid, null)
        }
        return enriched
    }

    // ── Vendor Lookup ──

    /**
     * Lookup combinato: DB locale + hostname + cache online.
     * L'API online viene chiamata separatamente via [launchOnlineLookup].
     */
    /** BSSID redatto da Android 14+ per app senza NEARBY_WIFI_DEVICES. */
    private val REDACTED_BSSID = "02:00:00:00:00:00"

    /**
     * Lookup combinato: DB locale + hostname + cache online.
     * L'API online viene chiamata separatamente via [launchOnlineLookup].
     */
    private fun lookupVendor(bssid: String?, hostname: String?): String? {
        if (bssid.isNullOrBlank() || bssid == "00:00:00:00:00:00" || bssid == REDACTED_BSSID) {
            Log.d(TAG, "lookupVendor: BSSID non valido '$bssid'")
            return hostname?.let { ouiDatabase.lookupByHostname(it) }
        }
        val vendor = ouiDatabase.lookupCombined(bssid, hostname)
        Log.d(TAG, "lookupVendor: BSSID=$bssid hostname=$hostname -> $vendor")
        return vendor
    }

    /**
     * Avvia lookup online in background. Se trova un vendor, la cache
     * verrà usata al prossimo ciclo di polling o scansione.
     */
    private fun launchOnlineLookup(bssid: String?, hostname: String?) {
        if (bssid.isNullOrBlank() || bssid == "00:00:00:00:00:00" || bssid == REDACTED_BSSID) return
        ioScope.launch {
            val vendor = ouiDatabase.lookupOnline(bssid)
            if (vendor != null) {
                Log.d(TAG, "onlineLookup: $bssid -> $vendor (cached)")
            }
        }
    }

    fun isScanAvailable(): Boolean = wifiScanner.isScanAvailable()
}
