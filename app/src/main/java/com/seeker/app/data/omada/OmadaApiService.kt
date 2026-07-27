package com.seeker.app.data.omada

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * ── TP-Link Omada Controller API Client ──
 *
 * Supporta due modalità:
 * 1. **Locale**: connessione diretta al controller Omada (OC200, OC300, Software Controller)
 *    Endpoint: POST /api/v2/login
 * 2. **Cloud**: connessione via TP-Link Cloud (omada.tplinkcloud.com)
 *    Endpoint: POST /api/v2/cloud/login
 *
 * Il rilevamento è automatico: se l'URL contiene "omada.tplinkcloud.com" usa cloud, altrimenti locale.
 *
 * Documentazione: https://www.tp-link.com/omada-sdn/
 */

// ─── Modelli Dati ───

/** Risposta login Omada (locale e cloud) */
@Serializable
data class OmadaLoginResponse(
    val errorCode: Int = 0,     // 0 = success
    val msg: String? = null,
    val result: OmadaLoginResult? = null
)

@Serializable
data class OmadaLoginResult(
    val token: String? = null
)

/** Risposta cloud login — restituisce anche la lista dei controller */
@Serializable
data class OmadaCloudLoginResponse(
    val errorCode: Int = 0,
    val msg: String? = null,
    val result: OmadaCloudLoginResult? = null
)

@Serializable
data class OmadaCloudLoginResult(
    val token: String? = null,
    val cloudControllerToken: String? = null,
    val controllerUrl: String? = null,
    val sites: List<OmadaCloudSite>? = null
)

@Serializable
data class OmadaCloudSite(
    val id: String? = null,
    val name: String? = null,
    val siteId: String? = null,
    val region: String? = null,
    val controllerUrl: String? = null
)

/** Sito Omada (locale) */
@Serializable
data class OmadaSite(
    val id: String? = null,
    val name: String? = null,
    val desc: String? = null
)

/** Dispositivo Omada (AP, switch, gateway) */
@Serializable
data class OmadaDevice(
    val name: String? = null,
    val type: Int? = null,               // 1=AP, 2=Gateway, 3=Switch, 4=Router
    val model: String? = null,
    val mac: String? = null,
    val ip: String? = null,
    val serial: String? = null,
    val version: String? = null,
    val needUpgrade: Boolean? = null,
    val status: Int? = null,             // 1=online, 0=offline, -1=isolated, 2=pending
    val uptime: Long? = null,
    val cpuUtilization: Double? = null,
    val memUtilization: Double? = null,
    val clients: Int? = null,
    val radioInfos: List<OmadaRadioInfo>? = null,
    val gatewayInfo: OmadaGatewayInfo? = null,
    val switchInfo: OmadaSwitchInfo? = null,
    val locateStatus: Boolean? = null
) {
    val displayName: String get() = name ?: model ?: mac ?: "Sconosciuto"
    val statusLabel: String get() = when (status) {
        1 -> "online"
        0 -> "offline"
        -1 -> "isolato"
        2 -> "in attesa"
        else -> "sconosciuto"
    }
}

@Serializable
data class OmadaRadioInfo(
    val radioId: Int? = null,
    val band: String? = null,        // "2.4GHz", "5GHz", "6GHz"
    val channel: Int? = null,
    val channelWidth: Int? = null,
    val txPower: Int? = null,
    val utilization: Double? = null,  // 0-100
    val noiseFloor: Int? = null,
    val clients: Int? = null
)

@Serializable
data class OmadaGatewayInfo(
    val ip: String? = null,
    val mac: String? = null,
    val wanIp: String? = null,
    val wanStatus: String? = null,
    val wanType: String? = null,       // "pppoe", "dhcp", "static"
    val downlinkRate: Long? = null,
    val uplinkRate: Long? = null
)

@Serializable
data class OmadaSwitchInfo(
    val poeBudget: Double? = null,
    val poeConsumption: Double? = null
)

/** Client Omada connesso */
@Serializable
data class OmadaClient(
    val id: String? = null,
    val name: String? = null,
    val mac: String? = null,
    val ip: String? = null,
    val hostname: String? = null,
    val vendor: String? = null,
    val deviceId: String? = null,       // ID del dispositivo a cui è connesso
    val signal: Int? = null,            // dBm
    val snr: Int? = null,
    val channel: Int? = null,
    val band: String? = null,
    val ssid: String? = null,
    val isGuest: Boolean? = null,
    val isWired: Boolean? = null,
    val txRate: Long? = null,
    val rxRate: Long? = null,
    val txBytes: Long? = null,
    val rxBytes: Long? = null,
    val uptime: Long? = null,
    val onlineTime: Long? = null
)

/** Risposta API Omada standard */
@Serializable
data class OmadaResponse<T>(
    val errorCode: Int = 0,
    val msg: String? = null,
    val result: T? = null
)

/** Risposta paginata Omada */
@Serializable
data class OmadaPageResult<T>(
    val totalRows: Int = 0,
    val data: List<T> = emptyList()
)

// ─── Risultato ───

sealed class OmadaResult<out T> {
    data class Success<T>(val data: T) : OmadaResult<T>()
    data class Error(val message: String, val httpCode: Int = 0) : OmadaResult<Nothing>()
}

// ─── Client API ───

/**
 * Client per l'API Omada Controller v2.
 *
 * Supporta due modalità:
 * - **Locale** (default): POST /api/v2/login su controller diretto
 * - **Cloud**: POST /api/v2/cloud/login su omada.tplinkcloud.com
 *
 * Il rilevamento è automatico in base all'URL.
 */
class OmadaApiClient(
    private val controllerUrl: String = "",
    private val username: String = "",
    private val password: String = ""
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val tokenRef = AtomicReference<String?>(null)
    private val currentSiteId = AtomicReference<String?>(null)
    private var cloudControllerBase: String? = null  // URL del controller dopo login cloud

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val baseUrl: String get() = controllerUrl.trimEnd('/')

    /** True se l'URL è un indirizzo cloud Omada */
    private val isCloud: Boolean get() = controllerUrl.contains("omada.tplinkcloud.com", ignoreCase = true)

    /** Base URL per le richieste API (cambia in modalità cloud dopo login) */
    private val apiBaseUrl: String get() = cloudControllerBase ?: baseUrl

    fun isConfigured(): Boolean = controllerUrl.isNotBlank()

    // ──────────────────────────────────────────────
    // Autenticazione
    // ──────────────────────────────────────────────

    /**
     * Login automatico: sceglie la modalità in base all'URL.
     */
    suspend fun login(): OmadaResult<String> {
        return if (isCloud) cloudLogin() else localLogin()
    }

    /**
     * Login locale (controller diretto).
     * POST /api/v2/login
     */
    private suspend fun localLogin(): OmadaResult<String> {
        if (!isConfigured()) return OmadaResult.Error("URL controller non configurato")

        return try {
            val loginPayload = JsonObject(
                mapOf(
                    "username" to JsonPrimitive(username),
                    "password" to JsonPrimitive(password)
                )
            ).toString()

            val request = Request.Builder()
                .url("$baseUrl/api/v2/login")
                .post(loginPayload.toRequestBody("application/json".toMediaType()))
                .header("Content-Type", "application/json")
                .build()

            val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                client.newCall(request).execute()
            }

            val bodyString = response.body?.string()
            if (!response.isSuccessful) {
                val msg = when (response.code) {
                    401 -> "Credenziali non valide"
                    403 -> "Account bloccato o permessi insufficienti"
                    404 -> "Endpoint login non trovato (verifica URL controller)"
                    429 -> "Troppi tentativi di login"
                    else -> bodyString?.let { parseError(it) } ?: "Errore HTTP ${response.code}"
                }
                return OmadaResult.Error(msg, response.code)
            }

            if (bodyString == null || bodyString.isBlank()) {
                return OmadaResult.Error("Risposta vuota dal server")
            }

            val loginResponse = json.decodeFromString<OmadaLoginResponse>(bodyString)
            if (loginResponse.errorCode != 0) {
                return OmadaResult.Error(loginResponse.msg ?: "Login fallito (errore ${loginResponse.errorCode})")
            }

            val token = loginResponse.result?.token
            if (token.isNullOrBlank()) {
                return OmadaResult.Error("Token non ricevuto")
            }

            tokenRef.set(token)

            // Recupera il sito predefinito
            val siteResult = getDefaultSite()
            when (siteResult) {
                is OmadaResult.Success -> {}
                is OmadaResult.Error -> {
                    currentSiteId.set("default")
                }
            }

            OmadaResult.Success("✅ Connesso a $controllerUrl")
        } catch (e: java.net.UnknownHostException) {
            OmadaResult.Error("Host non raggiungibile: $controllerUrl")
        } catch (e: java.net.SocketTimeoutException) {
            OmadaResult.Error("Timeout connessione (10s)")
        } catch (e: javax.net.ssl.SSLException) {
            OmadaResult.Error("Errore SSL: verifica che il controller usi HTTPS")
        } catch (e: Exception) {
            OmadaResult.Error(e.message ?: "Errore sconosciuto")
        }
    }

    /**
     * Login cloud (TP-Link Cloud).
     * POST /api/v2/cloud/login
     * Dopo il login, scopre il controller e lo usa come base per le richieste successive.
     */
    private suspend fun cloudLogin(): OmadaResult<String> {
        if (!isConfigured()) return OmadaResult.Error("URL cloud non configurato")

        return try {
            val loginPayload = JsonObject(
                mapOf(
                    "email" to JsonPrimitive(username),
                    "password" to JsonPrimitive(password)
                )
            ).toString()

            val request = Request.Builder()
                .url("$baseUrl/api/v2/cloud/login")
                .post(loginPayload.toRequestBody("application/json".toMediaType()))
                .header("Content-Type", "application/json")
                .build()

            val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                client.newCall(request).execute()
            }

            val bodyString = response.body?.string()
            if (!response.isSuccessful) {
                val msg = when (response.code) {
                    401 -> "Credenziali cloud non valide"
                    403 -> "Account bloccato"
                    429 -> "Troppi tentativi di login"
                    else -> bodyString?.let { parseError(it) } ?: "Errore HTTP ${response.code}"
                }
                return OmadaResult.Error(msg, response.code)
            }

            if (bodyString == null || bodyString.isBlank()) {
                return OmadaResult.Error("Risposta vuota dal server")
            }

            // Prova prima come OmadaCloudLoginResponse
            val cloudResponse = try {
                json.decodeFromString<OmadaCloudLoginResponse>(bodyString)
            } catch (_: Exception) {
                null
            }

            if (cloudResponse != null && cloudResponse.errorCode == 0) {
                val token = cloudResponse.result?.token
                if (token.isNullOrBlank()) {
                    return OmadaResult.Error("Token cloud non ricevuto")
                }
                tokenRef.set(token)

                // Scopri il controller dalla risposta cloud
                val ctrlUrl = cloudResponse.result?.controllerUrl
                    ?: cloudResponse.result?.sites?.firstOrNull()?.controllerUrl

                if (ctrlUrl != null) {
                    cloudControllerBase = ctrlUrl.trimEnd('/')
                    android.util.Log.d("SeekerOmada", "Controller cloud: $cloudControllerBase")
                }

                // Recupera il sito
                val siteResult = getDefaultSite()
                when (siteResult) {
                    is OmadaResult.Success -> {}
                    is OmadaResult.Error -> {
                        currentSiteId.set("default")
                    }
                }

                OmadaResult.Success("✅ Connesso via Cloud Omada")
            } else {
                // Fallback: prova come login locale standard
                val localResponse = try {
                    json.decodeFromString<OmadaLoginResponse>(bodyString)
                } catch (_: Exception) {
                    return OmadaResult.Error("Risposta non riconosciuta dal server")
                }

                if (localResponse.errorCode != 0) {
                    return OmadaResult.Error(localResponse.msg ?: "Login cloud fallito (errore ${localResponse.errorCode})")
                }

                val token = localResponse.result?.token
                if (token.isNullOrBlank()) {
                    return OmadaResult.Error("Token non ricevuto")
                }
                tokenRef.set(token)

                val siteResult = getDefaultSite()
                when (siteResult) {
                    is OmadaResult.Success -> {}
                    is OmadaResult.Error -> currentSiteId.set("default")
                }

                OmadaResult.Success("✅ Connesso a $controllerUrl")
            }
        } catch (e: java.net.UnknownHostException) {
            OmadaResult.Error("Host cloud non raggiungibile: $controllerUrl")
        } catch (e: java.net.SocketTimeoutException) {
            OmadaResult.Error("Timeout connessione cloud (10s)")
        } catch (e: javax.net.ssl.SSLException) {
            OmadaResult.Error("Errore SSL cloud")
        } catch (e: Exception) {
            OmadaResult.Error(e.message ?: "Errore sconosciuto")
        }
    }

    /**
     * Recupera il sito predefinito (primo disponibile).
     * GET /api/v2/sites
     */
    private suspend fun getDefaultSite(): OmadaResult<String> {
        return try {
            val result = getList<OmadaSite>("/api/v2/sites")
            when (result) {
                is OmadaResult.Success -> {
                    val defaultSite = result.data.firstOrNull()
                    if (defaultSite?.id != null) {
                        currentSiteId.set(defaultSite.id)
                        OmadaResult.Success(defaultSite.id)
                    } else {
                        OmadaResult.Error("Nessun sito trovato")
                    }
                }
                is OmadaResult.Error -> result
            }
        } catch (_: Exception) {
            OmadaResult.Error("Impossibile ottenere i siti")
        }
    }

    // ──────────────────────────────────────────────
    // Dispositivi
    // ──────────────────────────────────────────────

    /**
     * Recupera tutti i dispositivi del sito corrente.
     * GET /api/v2/sites/{siteId}/devices
     */
    suspend fun getDevices(): OmadaResult<List<OmadaDevice>> {
        val siteId = currentSiteId.get() ?: return OmadaResult.Error("Nessun sito selezionato")
        return getList("/api/v2/sites/$siteId/devices")
    }

    /**
     * Recupera solo gli AP (Access Point).
     */
    suspend fun getAccessPoints(): OmadaResult<List<OmadaDevice>> {
        return when (val result = getDevices()) {
            is OmadaResult.Success -> {
                val aps = result.data.filter { it.type == 1 || it.model?.startsWith("EAP") == true }
                OmadaResult.Success(aps)
            }
            is OmadaResult.Error -> result
        }
    }

    /**
     * Recupera i dettagli di un dispositivo specifico.
     * GET /api/v2/sites/{siteId}/devices/{mac}
     */
    suspend fun getDevice(mac: String): OmadaResult<OmadaDevice> {
        val siteId = currentSiteId.get() ?: return OmadaResult.Error("Nessun sito selezionato")
        return getSingle("/api/v2/sites/$siteId/devices/$mac")
    }

    // ──────────────────────────────────────────────
    // Client
    // ──────────────────────────────────────────────

    /**
     * Recupera i client connessi al sito corrente.
     * GET /api/v2/sites/{siteId}/clients
     */
    suspend fun getClients(): OmadaResult<List<OmadaClient>> {
        val siteId = currentSiteId.get() ?: return OmadaResult.Error("Nessun sito selezionato")
        return getList("/api/v2/sites/$siteId/clients")
    }

    /**
     * Recupera i client connessi a un dispositivo specifico.
     * GET /api/v2/sites/{siteId}/devices/{deviceId}/clients
     */
    suspend fun getClientsByDevice(deviceId: String): OmadaResult<List<OmadaClient>> {
        val siteId = currentSiteId.get() ?: return OmadaResult.Error("Nessun sito selezionato")
        return getList("/api/v2/sites/$siteId/devices/$deviceId/clients")
    }

    // ──────────────────────────────────────────────
    // Helper
    // ──────────────────────────────────────────────

    /**
     * Test di connessione completo: login + lista dispositivi.
     */
    suspend fun testConnection(): OmadaResult<String> {
        return when (val loginResult = login()) {
            is OmadaResult.Error -> loginResult
            is OmadaResult.Success -> {
                when (val devicesResult = getDevices()) {
                    is OmadaResult.Success -> {
                        val count = devicesResult.data.size
                        OmadaResult.Success("${loginResult.data} · $count dispositivi trovati")
                    }
                    is OmadaResult.Error -> loginResult
                }
            }
        }
    }

    // ──────────────────────────────────────────────
    // Chiamate HTTP
    // ──────────────────────────────────────────────

    private suspend fun <T> getList(path: String): OmadaResult<List<T>> {
        return request(path) { body ->
            val response = json.decodeFromString<OmadaResponse<OmadaPageResult<T>>>(body)
            if (response.errorCode == 0 && response.result != null) {
                response.result.data
            } else {
                throw Exception(response.msg ?: "Errore API ${response.errorCode}")
            }
        }
    }

    private suspend fun <T> getSingle(path: String): OmadaResult<T> {
        return request(path) { body ->
            val response = json.decodeFromString<OmadaResponse<T>>(body)
            if (response.errorCode == 0 && response.result != null) {
                response.result
            } else {
                throw Exception(response.msg ?: "Errore API ${response.errorCode}")
            }
        }
    }

    private suspend inline fun <T> request(path: String, parser: (String) -> T): OmadaResult<T> {
        if (!isConfigured()) return OmadaResult.Error("URL controller non configurato")

        val token = tokenRef.get()
        if (token == null) {
            return OmadaResult.Error("Non autenticato, effettua il login")
        }

        return try {
            val request = Request.Builder()
                .url("$baseUrl$path")
                .get()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer $token")
                .build()

            val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                client.newCall(request).execute()
            }

            val bodyString = response.body?.string()
            if (!response.isSuccessful) {
                val msg = when (response.code) {
                    401 -> "Token scaduto, effettua il login"
                    403 -> "Permessi insufficienti"
                    404 -> "Endpoint non trovato: $path"
                    else -> bodyString?.let { parseError(it) } ?: "Errore HTTP ${response.code}"
                }
                return OmadaResult.Error(msg, response.code)
            }

            if (bodyString == null || bodyString.isBlank()) {
                return OmadaResult.Error("Risposta vuota dal server")
            }

            OmadaResult.Success(parser(bodyString))
        } catch (e: java.net.UnknownHostException) {
            OmadaResult.Error("Host non raggiungibile")
        } catch (e: java.net.SocketTimeoutException) {
            OmadaResult.Error("Timeout connessione (10s)")
        } catch (e: javax.net.ssl.SSLException) {
            OmadaResult.Error("Errore SSL")
        } catch (e: kotlinx.serialization.SerializationException) {
            OmadaResult.Error("Errore parsing risposta: ${e.message}")
        } catch (e: Exception) {
            OmadaResult.Error(e.message ?: "Errore sconosciuto")
        }
    }

    private fun parseError(body: String): String {
        return try {
            val errorObj = json.decodeFromString<JsonObject>(body)
            errorObj["msg"]?.jsonPrimitive?.content
                ?: errorObj["message"]?.jsonPrimitive?.content
                ?: body.take(200)
        } catch (_: Exception) {
            body.take(200)
        }
    }
}

// ─── Repository ───

/**
 * Repository per dati Omada.
 */
class OmadaRepository(
    private val apiClient: OmadaApiClient
) {
    private var loggedIn: Boolean = false

    /**
     * Login al controller.
     */
    suspend fun connect(): OmadaResult<String> {
        return when (val result = apiClient.login()) {
            is OmadaResult.Success -> {
                loggedIn = true
                result
            }
            is OmadaResult.Error -> result
        }
    }

    /**
     * Recupera tutti i dispositivi.
     */
    suspend fun getDevices(): OmadaResult<List<OmadaDevice>> {
        if (!loggedIn) {
            val loginResult = connect()
            if (loginResult is OmadaResult.Error) return loginResult as OmadaResult.Error
        }
        return apiClient.getDevices()
    }

    /**
     * Recupera solo gli AP.
     */
    suspend fun getAccessPoints(): OmadaResult<List<OmadaDevice>> {
        return when (val result = getDevices()) {
            is OmadaResult.Success -> {
                val aps = result.data.filter { it.type == 1 || it.model?.startsWith("EAP") == true }
                OmadaResult.Success(aps)
            }
            is OmadaResult.Error -> result
        }
    }

    /**
     * Recupera i client connessi.
     */
    suspend fun getClients(): OmadaResult<List<OmadaClient>> {
        if (!loggedIn) {
            val loginResult = connect()
            if (loginResult is OmadaResult.Error) return loginResult as OmadaResult.Error
        }
        return apiClient.getClients()
    }

    /**
     * Test di connessione completo.
     */
    suspend fun testConnection(): OmadaResult<String> {
        return when (val result = connect()) {
            is OmadaResult.Success -> {
                when (val devices = getDevices()) {
                    is OmadaResult.Success -> {
                        OmadaResult.Success("${result.data} · ${devices.data.size} dispositivi")
                    }
                    is OmadaResult.Error -> result
                }
            }
            is OmadaResult.Error -> result
        }
    }
}
