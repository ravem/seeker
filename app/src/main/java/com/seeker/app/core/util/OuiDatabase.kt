package com.seeker.app.core.util

import android.content.Context
import android.util.Log
import com.seeker.app.data.oui.OuiEntry
import com.seeker.app.data.oui.OuiParser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SeekerOuiDB"

/** Pattern hostname → vendor per identificazione via mDNS/DNS. */
private val HOSTNAME_VENDORS = mapOf(
    "tplink" to "TP-Link", "tp-link" to "TP-Link",
    "netgear" to "Netgear",
    "cisco" to "Cisco", "meraki" to "Cisco Meraki",
    "ubiquiti" to "Ubiquiti", "ubnt" to "Ubiquiti",
    "mikrotik" to "MikroTik",
    "asus" to "ASUS",
    "dlink" to "D-Link", "d-link" to "D-Link",
    "huawei" to "Huawei",
    "zyxel" to "Zyxel",
    "aruba" to "Aruba", "ruckus" to "Ruckus",
    "engenius" to "EnGenius", "grandstream" to "Grandstream",
    "totolink" to "TotoLink", "tenda" to "Tenda",
    "linksys" to "Linksys", "belkin" to "Belkin",
    "apple" to "Apple", "macbook" to "Apple", "imac" to "Apple",
    "macmini" to "Apple", "iphone" to "Apple", "ipad" to "Apple",
    "dell" to "Dell", "lenovo" to "Lenovo", "thinkpad" to "Lenovo",
    "hp-" to "HP", "hewlett" to "HP",
    "msi" to "MSI", "acer" to "Acer", "razer" to "Razer",
    "surface" to "Microsoft",
    "samsung" to "Samsung", "galaxy" to "Samsung",
    "xiaomi" to "Xiaomi", "oneplus" to "OnePlus",
    "pixel" to "Google", "nexus" to "Google",
    "honor" to "Honor", "oppo" to "OPPO", "vivo" to "vivo",
    "fairphone" to "Fairphone",
    "google" to "Google", "nest" to "Google", "chromecast" to "Google",
    "amazon" to "Amazon", "echo" to "Amazon", "alexa" to "Amazon",
    "roborock" to "Roborock",
    "philips" to "Philips", "hue" to "Philips Hue",
    "sonos" to "Sonos", "bose" to "Bose", "harman" to "Harman", "jbl" to "JBL",
    "brother" to "Brother", "epson" to "Epson", "canon" to "Canon",
    "xerox" to "Xerox", "kyocera" to "Kyocera",
    "synology" to "Synology", "qnap" to "QNAP",
    "raspberry" to "Raspberry Pi", "espressif" to "Espressif",
    "nintendo" to "Nintendo",
    "playstation" to "Sony", "ps4" to "Sony", "ps5" to "Sony",
    "xbox" to "Microsoft"
)

/**
 * Gestisce il database OUI per la risoluzione MAC → Vendor.
 *
 * Combina:
 * - Database locale IEEE OUI (~40K entry)
 * - API online (macvendors.com) come fallback, con cache in memoria
 * - Pattern matching su hostname mDNS/DNS
 *
 * Il caricamento è lazy: il database viene caricato in memoria al primo accesso.
 */
@Singleton
class OuiDatabase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val cacheFile: File
        get() = File(context.filesDir, OUI_CACHE_FILENAME)

    private var entries: Map<String, OuiEntry> = emptyMap()
    private var isLoaded = false
    private var loadAttempted = false

    // Cache per risultati API online (solo valori non-null)
    private val onlineCache = ConcurrentHashMap<String, String>()

    // ── Caricamento ──

    suspend fun load(): Result<Unit> = withContext(Dispatchers.IO) { doLoad() }

    private fun doLoad(): Result<Unit> {
        if (isLoaded) return Result.success(Unit)
        if (loadAttempted) {
            Log.w(TAG, "doLoad: già tentato senza successo")
            return Result.failure(Exception("Già tentato senza successo"))
        }
        loadAttempted = true
        Log.d(TAG, "doLoad: caricamento database OUI...")
        return try {
            val input: InputStream = if (cacheFile.exists()) {
                Log.d(TAG, "doLoad: da cache file"); cacheFile.inputStream()
            } else {
                Log.d(TAG, "doLoad: da asset bundled"); context.assets.open(OUI_ASSET_FILENAME)
            }
            val parsed = OuiParser.parse(input); input.close()
            entries = parsed.associateBy { it.macPrefix }; isLoaded = true
            Log.i(TAG, "doLoad: caricato ${entries.size} entry"); Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "doLoad: primo tentativo fallito", e)
            try {
                Log.d(TAG, "doLoad: fallback su asset")
                val input = context.assets.open(OUI_ASSET_FILENAME)
                val parsed = OuiParser.parse(input); input.close()
                entries = parsed.associateBy { it.macPrefix }; isLoaded = true
                Log.i(TAG, "doLoad: caricato ${entries.size} entry (fallback)"); Result.success(Unit)
            } catch (e2: Exception) {
                Log.e(TAG, "doLoad: fallito definitivamente", e2); Result.failure(e2)
            }
        }
    }

    fun isDatabaseLoaded(): Boolean = isLoaded

    // ── Lookup locale (MAC → vendor da database IEEE) ──

    fun lookup(macAddress: String): String? {
        if (!isLoaded) { try { doLoad() } catch (_: Exception) { return null } }
        if (!isLoaded) return null

        val cleanMac = macAddress.replace(":", "").replace("-", "").replace(".", "").uppercase()
        if (cleanMac.length < 6) return null
        val prefix = cleanMac.substring(0, 6)
        if (prefix == "000000" || prefix == "FFFFFF") return null

        val entry = entries[prefix]
        if (entry != null) {
            if (entry.vendor.equals("Private", ignoreCase = true)) return null
            return entry.vendor
        }
        for (len in 5 downTo 3) {
            val shortPrefix = prefix.substring(0, len)
            val match = entries.values.find { it.macPrefix == shortPrefix }
            if (match != null) {
                if (match.vendor.equals("Private", ignoreCase = true)) return null
                return match.vendor
            }
        }
        return null
    }

    // ── Lookup online (API macvendors.com) ──

    /**
     * Cerca il vendor online per un MAC non trovato localmente.
     * Ignora MAC localmente amministrati (bit 1 del primo byte = 1).
     * I risultati positivi sono cached in memoria.
     */
    /** Prefisso BSSID redatto da Android 14+ */
    private val REDACTED_PREFIX = "020000"

    suspend fun lookupOnline(macAddress: String): String? = withContext(Dispatchers.IO) {
        val cleanMac = macAddress.replace(":", "").replace("-", "").replace(".", "").uppercase()
        if (cleanMac.length < 6) return@withContext null
        val prefix = cleanMac.substring(0, 6)
        if (prefix == "000000" || prefix == "FFFFFF" || prefix == REDACTED_PREFIX) return@withContext null

        // MAC localmente amministrato — salta
        val firstByte = prefix.substring(0, 2).toIntOrNull(16) ?: return@withContext null
        if (firstByte and 0x02 != 0) {
            Log.d(TAG, "lookupOnline: '$prefix' local-admin, salto API"); return@withContext null
        }

        // Cache check
        onlineCache[prefix]?.let {
            Log.d(TAG, "lookupOnline: '$prefix' -> $it (cache)"); return@withContext it
        }

        Log.d(TAG, "lookupOnline: '$prefix' -> query API...")
        try {
            val request = Request.Builder()
                .url("https://api.macvendors.com/${prefix}")
                .header("User-Agent", "Seeker/1.0 (Android)")
                .build()
            val response = client.newCall(request).execute()
            val vendor = if (response.isSuccessful)
                response.body?.string()?.trim()?.takeIf { it.isNotBlank() } else null
            if (vendor != null) {
                onlineCache[prefix] = vendor
                Log.d(TAG, "lookupOnline: '$prefix' -> $vendor")
            } else {
                Log.d(TAG, "lookupOnline: '$prefix' non trovato")
            }
            vendor
        } catch (e: Exception) {
            Log.w(TAG, "lookupOnline: errore '$prefix'", e)
            null
        }
    }

    /**
     * Controlla la cache online senza fare chiamate API.
     * Thread-safe, può essere chiamato dal thread principale.
     */
    fun lookupOnlineCache(macAddress: String): String? {
        val cleanMac = macAddress.replace(":", "").replace("-", "").replace(".", "").uppercase()
        if (cleanMac.length < 6) return null
        return onlineCache[cleanMac.substring(0, 6)]
    }

    // ── Lookup da hostname (mDNS/DNS) ──

    fun lookupByHostname(hostname: String?): String? {
        if (hostname.isNullOrBlank()) return null
        val clean = hostname.lowercase().trimEnd('.').removeSuffix(".local").removeSuffix(".local.")
        for ((keyword, vendor) in HOSTNAME_VENDORS) {
            if (clean.contains(keyword)) {
                Log.d(TAG, "lookupByHostname: '$clean' match '$keyword' -> $vendor")
                return vendor
            }
        }
        return null
    }

    // ── Lookup combinato ──

    /**
     * Lookup combinato sincrono: DB locale + hostname + cache online.
     * Non fa chiamate API (usa [lookupOnline] per quello).
     */
    fun lookupCombined(macAddress: String?, hostname: String?): String? {
        if (macAddress != null) {
            val v = lookup(macAddress)
            if (v != null) return v
            // Controlla cache online (risultati da chiamate API precedenti)
            val cached = lookupOnlineCache(macAddress)
            if (cached != null) return cached
        }
        if (hostname != null) {
            val v = lookupByHostname(hostname)
            if (v != null) return v
        }
        return null
    }

    // ── Aggiornamento remoto DB ──

    suspend fun updateFromRemote(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(OUI_DOWNLOAD_URL)
                .header("User-Agent", "Seeker/1.0 (Android OUI lookup)")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext Result.failure(
                Exception("HTTP ${response.code}: ${response.message}")
            )
            val body = response.body ?: return@withContext Result.failure(Exception("Risposta vuota"))
            val csvString = body.string()
            val parsed = parseIeeeCsv(csvString)
            if (parsed.isEmpty()) return@withContext Result.failure(Exception("DB OUI scaricato ma vuoto"))
            val jsonString = buildJsonFromEntries(parsed)
            cacheFile.parentFile?.mkdirs(); cacheFile.writeText(jsonString)
            entries = parsed.associateBy { it.macPrefix }; isLoaded = true
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    fun lastUpdateTimestamp(): Long = cacheFile.takeIf { it.exists() }?.lastModified() ?: 0L
    fun entryCount(): Int = entries.size

    // ── Parsing CSV IEEE ──

    private fun parseIeeeCsv(csv: String): List<OuiEntry> {
        val reader = BufferedReader(InputStreamReader(csv.byteInputStream()))
        val headerLine = reader.readLine()
        if (headerLine == null) { reader.close(); return emptyList() }
        if (!headerLine.startsWith("Registry")) { reader.close(); return OuiParser.parse(csv.byteInputStream()) }

        val entries = mutableListOf<OuiEntry>()
        val seen = mutableSetOf<String>()
        var line = reader.readLine()
        while (line != null) {
            val parts = try { parseCsvLine(line) } catch (_: Exception) { null }
            if (parts == null || parts.size < 3) { line = reader.readLine(); continue }
            try {
                val assignment = parts[1].trim()
                val vendor = parts[2].trim().removeSurrounding("\"")
                val address = if (parts.size >= 4) parts[3].trim().removeSurrounding("\"") else ""
                if (assignment.length < 6) { line = reader.readLine(); continue }
                val prefix = assignment.replace(":", "").replace("-", "").uppercase().take(6)
                if (prefix.length < 6 || vendor.isBlank()) { line = reader.readLine(); continue }
                if (seen.add(prefix)) {
                    entries.add(OuiEntry(macPrefix = prefix, vendor = vendor, address = address.ifBlank { null }))
                }
            } catch (_: Exception) { }
            line = reader.readLine()
        }
        reader.close()
        return entries
    }

    private fun parseCsvLine(line: String): List<String>? {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false; var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && !inQuotes -> inQuotes = true
                c == '"' && inQuotes -> {
                    if (i + 1 < line.length && line[i + 1] == '"') { current.append('"'); i++ } else inQuotes = false
                }
                c == ',' && !inQuotes -> { result.add(current.toString()); current.clear() }
                else -> current.append(c)
            }
            i++
        }
        result.add(current.toString())
        return if (result.size >= 3) result else null
    }

    private fun buildJsonFromEntries(entries: List<OuiEntry>): String {
        val sb = StringBuilder("{")
        for ((i, entry) in entries.withIndex()) {
            if (i > 0) sb.append(',')
            sb.append('"').append(entry.macPrefix)
            sb.append("\":{\"vendor\":\"").append(entry.vendor.replace("\"", "\\\""))
            if (entry.address != null) sb.append("\",\"address\":\"").append(entry.address.replace("\"", "\\\""))
            sb.append("\"}")
        }
        sb.append('}')
        return sb.toString()
    }

    companion object {
        private const val OUI_CACHE_FILENAME = "oui_database.json"
        private const val OUI_ASSET_FILENAME = "oui.json"
        private const val OUI_DOWNLOAD_URL = "https://standards-oui.ieee.org/oui/oui.csv"
    }
}
