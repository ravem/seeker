package com.seeker.app.data.meraki

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Risposta dell'endpoint /organizations/{orgId}/devices/availabilities
 * Documentazione: https://developer.cisco.com/meraki/api-v1/get-organization-devices-availabilities/
 */
@Serializable
data class MerakiAvailabilitiesResponse(
    val items: List<MerakiDeviceAvailability> = emptyList(),
    val meta: MerakiPageMeta? = null
)

@Serializable
data class MerakiDeviceAvailability(
    val serial: String = "",
    val name: String = "",
    val productType: String? = null,  // "wireless", "switch", "appliance", "camera", "sensor", etc.
    val status: String = "",         // "online", "offline", "alerting", "dormant"
    val network: MerakiAvailabilityNetwork? = null
)

@Serializable
data class MerakiAvailabilityNetwork(
    val id: String = "",
    val name: String = ""
)

@Serializable
data class MerakiPageMeta(
    val total: Int = 0,
    val count: Int = 0,
    val pageSize: Int = 0
)

/**
 * Stato bulk di un dispositivo Meraki.
 * Usato dall'endpoint /organizations/{organizationId}/deviceStatuses
 * che restituisce lo stato di tutti i dispositivi in una chiamata.
 */
@Serializable
data class MerakiDeviceStatus(
    val serial: String = "",
    val name: String = "",
    val status: String = "" // "online", "offline", "alerting", "dormant"
)

/**
 * Modelli di risposta delle API Meraki (v1).
 * Documentazione: https://developer.cisco.com/meraki/api/
 */

@Serializable
data class MerakiOrganization(
    val id: String = "",
    val name: String = "",
    val url: String? = null
)

@Serializable
data class MerakiNetwork(
    val id: String = "",
    @SerialName("organizationId") val organizationId: String = "",
    val name: String = "",
    val type: String = "", // "wireless", "appliance", "switch", "systemsManager", "camera"
    val tags: List<String> = emptyList(),
    val timeZone: String? = null,
    val notes: String? = null
)

@Serializable
data class MerakiDevice(
    val serial: String = "",
    val name: String = "",
    val model: String? = null,
    val networkId: String? = null,
    val status: String? = null,           // "online", "offline", "alerting", "dormant"
    val lanIp: String? = null,
    val publicIp: String? = null,
    val firmware: String? = null,
    val tags: List<String> = emptyList(),
    val mac: String? = null,
    val url: String? = null,
    val notes: String? = null
)

@Serializable
data class MerakiWirelessStatus(
    val serial: String = "",
    val basicServiceSets: List<MerakiBss> = emptyList(),
    val interference: MerakiInterference? = null
)

@Serializable
data class MerakiBss(
    @SerialName("ssidName") val ssid: String = "",
    val band: String = "", // "2.4", "5", "6"
    val channel: Int = 0,
    val noiseLevel: Int = 0,
    val utilization: Double = 0.0,
    val clients: Int = 0,
    val enabled: Boolean = true
)

@Serializable
data class MerakiInterference(
    val score: Int = 0,         // 0-100
    val channelUtilization: Double = 0.0,
    val noiseFloor: Int = 0
)

@Serializable
data class MerakiClient(
    val id: String = "",
    val description: String? = null,
    val mac: String? = null,
    val ip: String? = null,
    val connectedAt: String? = null,
    val ssid: String? = null,
    val band: String? = null,       // "2.4", "5", "6"
    val rssi: Int? = null,
    val snr: Int? = null
)

/**
 * Risultato di una chiamata API Meraki.
 */
sealed class MerakiResult<out T> {
    data class Success<T>(val data: T) : MerakiResult<T>()
    data class Error(val message: String, val httpCode: Int = 0) : MerakiResult<Nothing>()
}

/**
 * Client per l'API Meraki Dashboard v1.
 *
 * Endpoint base: https://api.meraki.com/api/v1
 * Autenticazione: header X-Cisco-Meraki-API-Key
 *
 * Implementa gli endpoint principali per il monitoraggio degli AP:
 * - Organizzazioni
 * - Reti (wireless)
 * - Dispositivi (AP)
 * - Stato wireless (canale, interferenze, client)
 * - Client connessi
 */
class MerakiApiClient(
    apiKey: String = ""
) {
    private val apiKey: String = apiKey.trim()
    private val baseUrl = "https://api.meraki.com/api/v1"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("X-Cisco-Meraki-API-Key", apiKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()



    fun isConfigured(): Boolean = apiKey.isNotBlank()

    // ──────────────────────────────────────────────
    // Organizzazioni
    // ──────────────────────────────────────────────

    /**
     * GET /organizations
     * Restituisce l'elenco delle organizzazioni accessibili con l'API key.
     */
    suspend fun getOrganizations(): MerakiResult<List<MerakiOrganization>> {
        return get("/organizations")
    }

    // ──────────────────────────────────────────────
    // Reti
    // ──────────────────────────────────────────────

    /**
     * GET /organizations/{organizationId}/networks
     * Restituisce le reti di un'organizzazione.
     */
    suspend fun getNetworks(organizationId: String): MerakiResult<List<MerakiNetwork>> {
        return get("/organizations/$organizationId/networks")
    }

    // ──────────────────────────────────────────────
    // Dispositivi (AP, switch, appliance)
    // ──────────────────────────────────────────────

    /**
     * GET /networks/{networkId}/devices
     * Restituisce i dispositivi di una rete.
     */
    suspend fun getDevices(networkId: String): MerakiResult<List<MerakiDevice>> {
        return get("/networks/$networkId/devices")
    }

    /**
     * GET /devices/{serial}
     * Restituisce i dettagli di un singolo dispositivo.
     */
    suspend fun getDevice(serial: String): MerakiResult<MerakiDevice> {
        return get("/devices/$serial")
    }

    // ──────────────────────────────────────────────
    // Stato Wireless
    // ──────────────────────────────────────────────

    /**
     * GET /devices/{serial}/wireless/status
     * Restituisce lo stato wireless di un AP (canale, interferenze, SSID).
     */
    suspend fun getWirelessStatus(serial: String): MerakiResult<MerakiWirelessStatus> {
        return get("/devices/$serial/wireless/status")
    }

    /**
     * GET /devices/{serial}/wireless/connectionStats
     * Restituisce statistiche di connessione per un AP.
     */
    suspend fun getWirelessConnectionStats(serial: String): MerakiResult<Map<String, Map<String, Int>>> {
        return get("/devices/$serial/wireless/connectionStats")
    }

    // ──────────────────────────────────────────────
    // Stato dispositivi (bulk)
    // ──────────────────────────────────────────────

    /**
     * GET /organizations/{organizationId}/deviceStatuses (v1)
     * Restituisce lo stato di tutti i dispositivi di un'organizzazione in una chiamata.
     */
    suspend fun getDeviceStatuses(organizationId: String): MerakiResult<List<MerakiDeviceStatus>> {
        return get("/organizations/$organizationId/deviceStatuses")
    }

    /**
     * GET /organizations/{organizationId}/devices/availabilities
     * Restituisce la disponibilità (stato online/offline/alerting/dormant) di tutti i dispositivi.
     * Documentazione: https://developer.cisco.com/meraki/api-v1/get-organization-devices-availabilities/
     * Nota: la risposta è un array JSON diretto, non un oggetto con campo "items".
     */
    suspend fun getDevicesAvailabilities(organizationId: String): MerakiResult<List<MerakiDeviceAvailability>> {
        return get("/organizations/$organizationId/devices/availabilities")
    }

    // ──────────────────────────────────────────────
    // Client
    // ──────────────────────────────────────────────

    /**
     * GET /devices/{serial}/clients
     * Restituisce i client connessi a un AP (ultimi 5 minuti).
     */
    suspend fun getDeviceClients(serial: String): MerakiResult<List<MerakiClient>> {
        return get("/devices/$serial/clients?perPage=50")
    }

    /**
     * GET /networks/{networkId}/clients
     * Restituisce tutti i client connessi alla rete (ultimi 5 minuti).
     */
    suspend fun getNetworkClients(networkId: String): MerakiResult<List<MerakiClient>> {
        return get("/networks/$networkId/clients?perPage=100")
    }

    // ──────────────────────────────────────────────
    // Endpoint helper
    // ──────────────────────────────────────────────

    /**
     * Test di connessione: recupera la prima organizzazione.
     * Usato dalla UI per verificare che l'API key sia valida.
     */
    suspend fun testConnection(): MerakiResult<String> {
        return when (val result = getOrganizations()) {
            is MerakiResult.Success -> {
                if (result.data.isNotEmpty()) {
                    MerakiResult.Success("✅ Connesso: ${result.data.first().name}")
                } else {
                    MerakiResult.Success("✅ Connesso (nessuna organizzazione)")
                }
            }
            is MerakiResult.Error -> result
        }
    }

    // ──────────────────────────────────────────────
    // Metodo GET generico
    // ──────────────────────────────────────────────

    @Suppress("UNCHECKED_CAST")
    private suspend inline fun <reified T> get(path: String): MerakiResult<T> {
        if (!isConfigured()) return MerakiResult.Error("API key non configurata")

        return try {
            val request = Request.Builder()
                .url("$baseUrl$path")
                .get()
                .build()

            val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                client.newCall(request).execute()
            }

            val bodyString = response.body?.string()
            if (!response.isSuccessful) {
                val detail = bodyString?.let { parseError(it) }?.let { " — $it" } ?: ""
                val errorMsg = when (response.code) {
                    401 -> "API Key non valida$detail"
                    403 -> "Permessi insufficienti$detail"
                    404 -> "Risorsa non trovata: $path$detail"
                    429 -> "Troppe richieste (rate limit)$detail"
                    else -> "Errore HTTP ${response.code}$detail"
                }
                return MerakiResult.Error(errorMsg, response.code)
            }

            if (bodyString == null || bodyString.isBlank()) {
                return MerakiResult.Error("Risposta vuota dal server")
            }

            // Debug: stampa il primo dispositivo per vedere la struttura JSON
            if (path.contains("/devices") && bodyString.length > 100) {
                val sample = bodyString.substring(0, minOf(bodyString.length, 800))
                android.util.Log.d("SeekerMeraki", "Risposta devices (truncata): $sample")
            }

            val parsed = json.decodeFromString<T>(bodyString)
            MerakiResult.Success(parsed)
        } catch (e: kotlinx.serialization.SerializationException) {
            MerakiResult.Error("Errore parsing risposta: ${e.message}")
        } catch (e: java.net.UnknownHostException) {
            MerakiResult.Error("Host non raggiungibile: verifica la connessione internet")
        } catch (e: java.net.SocketTimeoutException) {
            MerakiResult.Error("Timeout connessione (15s)")
        } catch (e: Exception) {
            MerakiResult.Error(e.message ?: "Errore sconosciuto")
        }
    }

    /**
     * Tenta di estrarre un messaggio di errore dal corpo JSON di una risposta di errore Meraki.
     */
    private fun parseError(body: String): String {
        return try {
            val errorJson = json.decodeFromString<Map<String, String>>(body)
            errorJson["message"] ?: errorJson["error"] ?: body.take(200)
        } catch (_: Exception) {
            body.take(200)
        }
    }
}

/**
 * Repository per i dati Meraki.
 * Fornisce dati aggregati (organizzazioni → reti → dispositivi → stato wireless).
 */
class MerakiRepository(
    private val apiClient: MerakiApiClient
) {
    /**
     * Recupera tutte le organizzazioni.
     */
    suspend fun getOrganizations(): MerakiResult<List<MerakiOrganization>> =
        apiClient.getOrganizations()

    /**
     * Recupera tutte le reti di un'organizzazione.
     */
    suspend fun getNetworks(orgId: String): MerakiResult<List<MerakiNetwork>> =
        apiClient.getNetworks(orgId)

    /**
     * Recupera tutti i dispositivi di una rete.
     */
    suspend fun getDevices(networkId: String): MerakiResult<List<MerakiDevice>> =
        apiClient.getDevices(networkId)

    /**
     * Recupera lo stato wireless di un dispositivo.
     */
    suspend fun getWirelessStatus(serial: String): MerakiResult<MerakiWirelessStatus> =
        apiClient.getWirelessStatus(serial)

    /**
     * Recupera tutti gli AP (dispositivi wireless) di una rete con stato wireless.
     * Filtra solo i dispositivi che sono AP (modelli MR).
     */
    suspend fun getAccessPointsWithStatus(networkId: String): MerakiResult<List<Pair<MerakiDevice, MerakiWirelessStatus?>>> {
        return when (val devicesResult = getDevices(networkId)) {
            is MerakiResult.Success -> {
                val apDevices = devicesResult.data.filter { it.model?.startsWith("MR") == true }
                val withStatus = apDevices.map { device ->
                    val status = when (val s = apiClient.getWirelessStatus(device.serial)) {
                        is MerakiResult.Success -> s.data
                        else -> null
                    }
                    device to status
                }
                MerakiResult.Success(withStatus)
            }
            is MerakiResult.Error -> devicesResult
        }
    }

    /**
     * Recupera lo stato di tutti i dispositivi di un'organizzazione (v1).
     * Endpoint: /organizations/{orgId}/deviceStatuses
     */
    suspend fun getDeviceStatuses(orgId: String): MerakiResult<List<MerakiDeviceStatus>> =
        apiClient.getDeviceStatuses(orgId)

    /**
     * Recupera la disponibilità (stato) di tutti i dispositivi di un'organizzazione.
     * Endpoint: /organizations/{orgId}/devices/availabilities
     * Documentazione: https://developer.cisco.com/meraki/api-v1/get-organization-devices-availabilities/
     */
    suspend fun getDevicesAvailabilities(orgId: String): MerakiResult<List<MerakiDeviceAvailability>> =
        apiClient.getDevicesAvailabilities(orgId)

    /**
     * Test di connessione.
     */
    suspend fun testConnection(): MerakiResult<String> = apiClient.testConnection()
}
