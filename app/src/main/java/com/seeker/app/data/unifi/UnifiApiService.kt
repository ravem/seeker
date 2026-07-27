package com.seeker.app.data.unifi

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * ── UniFi Controller API Client ──
 *
 * API per interagire con un controller Ubiquiti UniFi.
 * Supporta UniFi OS (UDM-Pro, Cloud Key) e controller tradizionali.
 *
 * Endpoint base: https://<controller-ip>:8443
 * Autenticazione: sessione (login con cookie) o API key (UniFi OS 3.x+)
 *
 * Documentazione: https://ubntwiki.com/products/software/unifi-controller/api
 */

// ─── Modelli Dati ───

/** Risposta login UniFi */
@Serializable
data class UniFiLoginResponse(
    val meta: UniFiMeta,
    val data: List<UniFiLoginData>? = null
)

@Serializable
data class UniFiMeta(
    val rc: String, // "ok" o "error"
    val msg: String? = null
)

@Serializable
data class UniFiLoginData(
    val name: String? = null
)

/** Dispositivo UniFi (AP, switch, gateway) */
@Serializable
data class UniFiDevice(
    val _id: String? = null,
    val name: String? = null,
    val model: String? = null,
    val type: String? = null,         // "uap", "usw", "ugw", "uxg"
    val mac: String? = null,
    val ip: String? = null,
    val serial: String? = null,
    val version: String? = null,
    val firmwareVersion: String? = null,
    val state: Int? = null,            // 1=online, 0=offline, 2=pending, -1=unknown
    val uptime: Long? = null,
    val lastSeen: Long? = null,
    val `interface`: String? = null,
    val radioTable: List<UniFiRadio>? = null,
    val radioTableNa: List<UniFiRadio>? = null,
    val radioTableNg: List<UniFiRadio>? = null,
    val scanning: String? = null,
    val vwireEnabled: Boolean? = null,
    val isolated: Boolean? = null,
    val connectedAt: Long? = null,
    val configNetwork: UniFiNetworkConfig? = null,
    @SerialName("radio_table") val radioTableLegacy: List<UniFiRadio>? = null
) {
    /** Nome visualizzato del dispositivo */
    val displayName: String get() = name ?: model ?: mac ?: "Sconosciuto"

    /** Stato come stringa */
    val statusLabel: String get() = when (state) {
        1 -> "online"
        0 -> "offline"
        2 -> "pending"
        else -> "sconosciuto"
    }

    /** Radio combinate da tutti i campi radio */
    val allRadios: List<UniFiRadio>
        get() {
            val radios = mutableListOf<UniFiRadio>()
            radios.addAll(radioTable ?: emptyList())
            radios.addAll(radioTableNa ?: emptyList())
            radios.addAll(radioTableNg ?: emptyList())
            radios.addAll(radioTableLegacy ?: emptyList())
            return radios.distinctBy { it.name }
        }
}

@Serializable
data class UniFiRadio(
    val name: String? = null,      // "wifi0", "wifi1", "na", "ng"
    val radio: String? = null,     // "ng", "na", "6e"
    val channel: Int? = null,
    val channelWidth: Int? = null,  // 20, 40, 80
    val txPower: Int? = null,      // dBm
    val txPowerMode: String? = null, // "high", "medium", "low"
    val antennaId: Int? = null,
    val antennas: List<UniFiAntenna>? = null
)

@Serializable
data class UniFiAntenna(
    val id: Int? = null,
    val gain: Int? = null
)

@Serializable
data class UniFiNetworkConfig(
    val ip: String? = null,
    val netmask: String? = null,
    val gateway: String? = null,
    val dns1: String? = null,
    val dns2: String? = null
)

/** Client connesso a un AP UniFi */
@Serializable
data class UniFiClient(
    val _id: String? = null,
    val mac: String? = null,
    val ip: String? = null,
    val hostname: String? = null,
    val name: String? = null,
    val oui: String? = null,        // Vendor dal MAC
    val apMac: String? = null,      // MAC dell'AP a cui è connesso
    val channel: Int? = null,
    val radio: String? = null,      // "ng", "na", "6e"
    val rssi: Int? = null,
    val snr: Int? = null,
    val signal: Int? = null,        // dBm
    val txRate: Int? = null,        // kbps
    val rxRate: Int? = null,
    val txBytes: Long? = null,
    val rxBytes: Long? = null,
    val uptime: Long? = null,
    val isGuest: Boolean? = null,
    val isWired: Boolean? = null,
    val ssid: String? = null,
    val radioName: String? = null
)

/** Risposta lista dispositivi */
@Serializable
data class UniFiDeviceListResponse(
    val meta: UniFiMeta,
    val data: List<UniFiDevice> = emptyList()
)

/** Risposta lista client */
@Serializable
data class UniFiClientListResponse(
    val meta: UniFiMeta,
    val data: List<UniFiClient> = emptyList()
)

/** Stato del sito UniFi */
@Serializable
data class UniFiSiteHealth(
    val status: String? = null,         // "ok", "warning", "error"
    val numAp: Int? = null,
    val numSw: Int? = null,
    val numGw: Int? = null,
    val numClient: Int? = null,
    val wanIp: String? = null,
    val gwMac: String? = null,
    val remoteUserEnabled: Boolean? = null,
    val alerts: List<String>? = null
)

// ─── Risultato ───

sealed class UniFiResult<out T> {
    data class Success<T>(val data: T) : UniFiResult<T>()
    data class Error(val message: String, val httpCode: Int = 0) : UniFiResult<Nothing>()
}

// ─── Client API ───

/**
 * Client per l'API UniFi Controller.
 *
 * Usa autenticazione basata su sessione (login + cookie) o API key (UniFi OS 3.x+).
 * Supporta sia il classico endpoint /api/ che /proxy/network/api/ (UniFi OS).
 */
class UniFiApiClient(
    private val controllerUrl: String = "",
    private val username: String = "",
    private val password: String = ""
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val cookies = AtomicReference<Map<String, String>>(emptyMap())
    private var csrfToken: String? = null

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .cookieJar(object : okhttp3.CookieJar {
            override fun saveFromResponse(url: okhttp3.HttpUrl, cookies: List<okhttp3.Cookie>) {
                val map = mutableMapOf<String, String>()
                cookies.forEach { map[it.name] = it.value }
                this@UniFiApiClient.cookies.set(map)
            }

            override fun loadForRequest(url: okhttp3.HttpUrl): List<okhttp3.Cookie> {
                return cookies.get().map { (name, value) ->
                    val builder = okhttp3.Cookie.Builder()
                        .domain(url.host)
                        .path("/")
                        .name(name)
                        .value(value)
                        .httpOnly()
                    if (url.scheme == "https") {
                        builder.secure()
                    }
                    builder.build()
                }
            }
        })
        .build()

    private val baseUrl: String get() = controllerUrl.trimEnd('/')

    fun isConfigured(): Boolean = controllerUrl.isNotBlank()

    // ──────────────────────────────────────────────
    // Autenticazione
    // ──────────────────────────────────────────────

    /**
     * Login al controller UniFi.
     * Endpoint: POST /api/login
     * Usa le credenziali fornite e salva i cookie di sessione.
     */
    suspend fun login(): UniFiResult<String> {
        if (!isConfigured()) return UniFiResult.Error("URL controller non configurato")
        if (username.isBlank()) return UniFiResult.Error("Username non configurato")

        return try {
            val formBody = FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build()

            val request = Request.Builder()
                .url("${baseUrl}/api/login")
                .post(formBody)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build()

            val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                client.newCall(request).execute()
            }

            val bodyString = response.body?.string()
            if (!response.isSuccessful) {
                val msg = when (response.code) {
                    401 -> "Credenziali non valide"
                    403 -> "Accesso negato"
                    404 -> "Endpoint login non trovato"
                    else -> bodyString?.let { parseError(it) } ?: "Errore HTTP ${response.code}"
                }
                return UniFiResult.Error(msg, response.code)
            }

            // Salva CSRF token se presente negli header
            csrfToken = response.header("X-CSRF-Token")

            // Parse risposta
            if (bodyString != null) {
                try {
                    val loginResponse = json.decodeFromString<UniFiLoginResponse>(bodyString)
                    if (loginResponse.meta.rc == "ok") {
                        // Determina se è UniFi OS (con /proxy/network/api/)
                        val apiPath = detectApiPath()
                        UniFiResult.Success("✅ Connesso a $controllerUrl (${apiPath})")
                    } else {
                        UniFiResult.Error(loginResponse.meta.msg ?: "Login fallito")
                    }
                } catch (_: Exception) {
                    // Alcuni controller non restituiscono JSON strutturato
                    if (bodyString.contains("\"rc\":\"ok\"") || bodyString.isBlank()) {
                        UniFiResult.Success("✅ Connesso a $controllerUrl")
                    } else {
                        UniFiResult.Error(bodyString.take(200))
                    }
                }
            } else {
                UniFiResult.Error("Risposta vuota dal server")
            }
        } catch (e: java.net.UnknownHostException) {
            UniFiResult.Error("Host non raggiungibile: $controllerUrl")
        } catch (e: java.net.SocketTimeoutException) {
            UniFiResult.Error("Timeout connessione (10s)")
        } catch (e: javax.net.ssl.SSLException) {
            UniFiResult.Error("Errore SSL: verifica che il controller usi HTTPS")
        } catch (e: Exception) {
            UniFiResult.Error(e.message ?: "Errore sconosciuto")
        }
    }

    /**
     * Determina il percorso API corretto per il controller.
     * UniFi OS (3.x+) usa /proxy/network/api/.
     * Controller tradizionali usano /api/.
     */
    private suspend fun detectApiPath(): String {
        return try {
            val testRequest = Request.Builder()
                .url("${baseUrl}/proxy/network/api/self")
                .get()
                .build()
            val testResponse = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                client.newCall(testRequest).execute()
            }
            if (testResponse.isSuccessful) "/proxy/network/api" else "/api"
        } catch (_: Exception) {
            "/api"
        }
    }

    // ──────────────────────────────────────────────
    // Dispositivi (AP)
    // ──────────────────────────────────────────────

    /**
     * Recupera tutti i dispositivi (AP, switch, gateway) dal sito default.
     * GET /api/s/default/stat/device
     */
    suspend fun getDevices(apiPath: String = "/api"): UniFiResult<List<UniFiDevice>> {
        return getList("${apiPath}/s/default/stat/device")
    }

    /**
     * Recupera i dettagli di un dispositivo specifico.
     * GET /api/s/default/stat/device/{mac}
     */
    suspend fun getDevice(mac: String, apiPath: String = "/api"): UniFiResult<UniFiDevice> {
        return getSingle("${apiPath}/s/default/stat/device/$mac")
    }

    // ──────────────────────────────────────────────
    // Client
    // ──────────────────────────────────────────────

    /**
     * Recupera tutti i client connessi (ultimi 5 minuti).
     * GET /api/s/default/stat/sta
     */
    suspend fun getClients(apiPath: String = "/api"): UniFiResult<List<UniFiClient>> {
        return getList("${apiPath}/s/default/stat/sta")
    }

    /**
     * Recupera i client connessi a un AP specifico.
     * GET /api/s/default/stat/sta/{apMac}
     */
    suspend fun getClientsByAp(apMac: String, apiPath: String = "/api"): UniFiResult<List<UniFiClient>> {
        return getList("${apiPath}/s/default/stat/sta/$apMac")
    }

    // ──────────────────────────────────────────────
    // Health
    // ──────────────────────────────────────────────

    /**
     * Recupera lo stato di salute del sito.
     * GET /api/s/default/stat/health
     */
    suspend fun getSiteHealth(apiPath: String = "/api"): UniFiResult<UniFiSiteHealth> {
        return getSingle("${apiPath}/s/default/stat/health")
    }

    // ──────────────────────────────────────────────
    // Helper
    // ──────────────────────────────────────────────

    /**
     * Test di connessione completo: login + lista dispositivi.
     */
    suspend fun testConnection(): UniFiResult<String> {
        return when (val loginResult = login()) {
            is UniFiResult.Error -> loginResult
            is UniFiResult.Success -> {
                // Prova a ottenere i dispositivi
                when (val devicesResult = getDevices()) {
                    is UniFiResult.Success -> {
                        val count = devicesResult.data.size
                        UniFiResult.Success("${loginResult.data} · $count dispositivi trovati")
                    }
                    is UniFiResult.Error -> loginResult // Login OK ma dispositivi non ottenibili
                }
            }
        }
    }

    // ──────────────────────────────────────────────
    // Chiamate HTTP
    // ──────────────────────────────────────────────

    @Suppress("UNCHECKED_CAST")
    private suspend inline fun <reified T> getList(path: String): UniFiResult<List<T>> {
        return request(path) { body ->
            try {
                val response = json.decodeFromString<Map<String, JsonElement>>(body)
                val dataArray = response["data"]?.jsonArray
                if (dataArray != null) {
                    dataArray.map { json.decodeFromJsonElement<T>(it) }
                } else {
                    emptyList()
                }
            } catch (_: Exception) {
                json.decodeFromString<UniFiDeviceListResponse>(body).data as? List<T>
                    ?: json.decodeFromString<UniFiClientListResponse>(body).data as? List<T>
                    ?: emptyList()
            }
        }
    }

    private suspend inline fun <reified T> getSingle(path: String): UniFiResult<T> {
        return request(path) { body ->
            val response = json.decodeFromString<Map<String, JsonElement>>(body)
            val dataArray = response["data"]?.jsonArray
            if (dataArray != null && dataArray.isNotEmpty()) {
                json.decodeFromJsonElement<T>(dataArray[0])
            } else {
                json.decodeFromString<T>(body)
            }
        }
    }

    private suspend inline fun <T> request(path: String, parser: (String) -> T): UniFiResult<T> {
        if (!isConfigured()) return UniFiResult.Error("URL controller non configurato")

        return try {
            val csrf = csrfToken
            val headers = Headers.Builder()
                .add("Content-Type", "application/json")
                .add("Accept", "application/json")
            if (csrf != null) {
                headers.add("X-CSRF-Token", csrf)
            }

            val request = Request.Builder()
                .url("$baseUrl$path")
                .get()
                .headers(headers.build())
                .build()

            val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                client.newCall(request).execute()
            }

            val bodyString = response.body?.string()
            if (!response.isSuccessful) {
                val msg = when (response.code) {
                    401 -> "Sessione scaduta, effettua il login"
                    404 -> "Endpoint non trovato: $path"
                    else -> bodyString?.let { parseError(it) } ?: "Errore HTTP ${response.code}"
                }
                return UniFiResult.Error(msg, response.code)
            }

            if (bodyString == null || bodyString.isBlank()) {
                return UniFiResult.Error("Risposta vuota dal server")
            }

            UniFiResult.Success(parser(bodyString))
        } catch (e: java.net.UnknownHostException) {
            UniFiResult.Error("Host non raggiungibile")
        } catch (e: java.net.SocketTimeoutException) {
            UniFiResult.Error("Timeout connessione (10s)")
        } catch (e: javax.net.ssl.SSLException) {
            UniFiResult.Error("Errore SSL")
        } catch (e: kotlinx.serialization.SerializationException) {
            UniFiResult.Error("Errore parsing risposta: ${e.message}")
        } catch (e: Exception) {
            UniFiResult.Error(e.message ?: "Errore sconosciuto")
        }
    }

    private fun parseError(body: String): String {
        return try {
            val errorJson = json.decodeFromString<Map<String, JsonElement>>(body)
            val meta = errorJson["meta"]?.jsonObject
            meta?.get("msg")?.jsonPrimitive?.content
                ?: errorJson["message"]?.jsonPrimitive?.content
                ?: body.take(200)
        } catch (_: Exception) {
            body.take(200)
        }
    }
}

// ─── Repository ───

/**
 * Repository per dati UniFi.
 */
class UniFiRepository(
    private val apiClient: UniFiApiClient
) {
    private var apiPath: String = "/api"
    private var loggedIn: Boolean = false

    /**
     * Login e rilevamento automatico del percorso API.
     */
    suspend fun connect(): UniFiResult<String> {
        return when (val result = apiClient.login()) {
            is UniFiResult.Success -> {
                loggedIn = true
                result
            }
            is UniFiResult.Error -> result
        }
    }

    /**
     * Recupera tutti i dispositivi.
     */
    suspend fun getDevices(): UniFiResult<List<UniFiDevice>> {
        if (!loggedIn) {
            val loginResult = connect()
            if (loginResult is UniFiResult.Error) return loginResult as UniFiResult.Error
        }
        return apiClient.getDevices(apiPath)
    }

    /**
     * Recupera solo gli AP (Access Point).
     */
    suspend fun getAccessPoints(): UniFiResult<List<UniFiDevice>> {
        return when (val result = getDevices()) {
            is UniFiResult.Success -> {
                val aps = result.data.filter { it.type == "uap" || it.model?.startsWith("UAP") == true }
                UniFiResult.Success(aps)
            }
            is UniFiResult.Error -> result
        }
    }

    /**
     * Recupera i client connessi.
     */
    suspend fun getClients(): UniFiResult<List<UniFiClient>> {
        if (!loggedIn) {
            val loginResult = connect()
            if (loginResult is UniFiResult.Error) return loginResult as UniFiResult.Error
        }
        return apiClient.getClients(apiPath)
    }

    /**
     * Recupera client per un AP specifico.
     */
    suspend fun getClientsByAp(apMac: String): UniFiResult<List<UniFiClient>> {
        if (!loggedIn) {
            val loginResult = connect()
            if (loginResult is UniFiResult.Error) return loginResult as UniFiResult.Error
        }
        return apiClient.getClientsByAp(apMac, apiPath)
    }

    /**
     * Test di connessione completo.
     */
    suspend fun testConnection(): UniFiResult<String> {
        return when (val result = connect()) {
            is UniFiResult.Success -> {
                when (val devices = getDevices()) {
                    is UniFiResult.Success -> {
                        UniFiResult.Success("${result.data} · ${devices.data.size} dispositivi")
                    }
                    is UniFiResult.Error -> result
                }
            }
            is UniFiResult.Error -> result
        }
    }
}
