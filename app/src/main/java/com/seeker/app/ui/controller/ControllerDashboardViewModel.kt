package com.seeker.app.ui.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seeker.app.core.model.ControllerDevice
import com.seeker.app.core.model.ControllerDeviceStatus
import com.seeker.app.core.model.ControllerSource
import com.seeker.app.core.model.ControllerStatus
import com.seeker.app.data.meraki.MerakiApiClient
import com.seeker.app.data.meraki.MerakiDevice
import com.seeker.app.data.meraki.MerakiResult
import com.seeker.app.data.omada.OmadaApiClient
import com.seeker.app.data.omada.OmadaDevice
import com.seeker.app.data.omada.OmadaResult
import com.seeker.app.data.settings.SecurePreferences
import com.seeker.app.data.unifi.UniFiApiClient
import com.seeker.app.data.unifi.UniFiDevice
import com.seeker.app.data.unifi.UniFiResult
import com.seeker.app.data.unifi.UniFiRepository
import com.seeker.app.data.omada.OmadaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ControllerDashboardUiState(
    val devices: List<ControllerDevice> = emptyList(),
    val isLoading: Boolean = false,
    val lastRefresh: Long? = null,
    val errorMessage: String? = null,
    val controllerStatuses: List<ControllerStatus> = emptyList(),
    val selectedDevice: ControllerDevice? = null,
    val deviceDetail: DeviceDetailState = DeviceDetailState(),
    val organizationName: String? = null,
    // Selezione organizzazione Meraki
    val merakiOrganizations: List<OrgOption> = emptyList(),
    val showOrgPicker: Boolean = false
)

data class OrgOption(
    val id: String,
    val name: String
)

data class DeviceDetailState(
    val isLoading: Boolean = false,
    val mac: String? = null,
    val serial: String? = null,
    val firmware: String? = null,
    val model: String? = null,
    val ssids: List<String> = emptyList(),
    val clients: Int? = null,
    val clientList: List<DeviceClient> = emptyList(),
    val pingLatencyMs: Long? = null,
    val error: String? = null
)

data class DeviceClient(
    val mac: String = "",
    val ip: String? = null,
    val hostname: String? = null,
    val ssid: String? = null,
    val rssi: Int? = null
)

@HiltViewModel
class ControllerDashboardViewModel @Inject constructor(
    private val securePreferences: SecurePreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ControllerDashboardUiState())
    val uiState: StateFlow<ControllerDashboardUiState> = _uiState.asStateFlow()

    /**
     * Aggiorna la dashboard leggendo le credenziali direttamente da DataStore.
     * Può essere chiamato più volte (ogni volta che la scherma diventa visibile).
     */
    private var pollingJob: kotlinx.coroutines.Job? = null

    /**
     * Avvia il polling periodico. Chiamato quando la scherma diventa visibile.
     * Si ferma automaticamente quando la scherma va in background (lifecycle).
     */
    fun startPolling(intervalMs: Long = 5_000L) {
        stopPolling()
        pollingJob = viewModelScope.launch {
            while (true) {
                refreshOnce()
                kotlinx.coroutines.delay(intervalMs)
            }
        }
    }

    /**
     * Ferma il polling.
     */
    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    /**
     * Esegue un refresh singolo.
     */
    fun refresh() {
        viewModelScope.launch {
            refreshOnce()
        }
    }

    private suspend fun refreshOnce() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        // Legge le credenziali sempre da SecurePreferences (crittografate)
        val merakiApiKey = securePreferences.merakiApiKey.first()
        val merakiOrgId = securePreferences.merakiOrgId.first()
        val unifiUrl = securePreferences.unifiUrl.first()
        val unifiUsername = securePreferences.unifiUsername.first()
        val unifiPassword = securePreferences.unifiPassword.first()
        val omadaUrl = securePreferences.omadaUrl.first()
        val omadaUsername = securePreferences.omadaUsername.first()
        val omadaPassword = securePreferences.omadaPassword.first()

        val allDevices = mutableListOf<ControllerDevice>()
        val statuses = mutableListOf<ControllerStatus>()

        var merakiOrgName: String? = null
        // Meraki
        if (merakiApiKey.isNotBlank()) {
            val (devices, status, orgName) = fetchMerakiDevices(merakiApiKey, merakiOrgId)
            allDevices.addAll(devices)
            statuses.add(status)
            if (orgName != null) merakiOrgName = orgName
        } else {
            statuses.add(ControllerStatus(ControllerSource.MERAKI, isConfigured = false))
        }

        // UniFi
        if (unifiUrl.isNotBlank()) {
            val (devices, status) = fetchUnifiDevices(unifiUrl, unifiUsername, unifiPassword)
            allDevices.addAll(devices)
            statuses.add(status)
        } else {
            statuses.add(ControllerStatus(ControllerSource.UNIFI, isConfigured = false))
        }

        // Omada
        if (omadaUrl.isNotBlank()) {
            val (devices, status) = fetchOmadaDevices(omadaUrl, omadaUsername, omadaPassword)
            allDevices.addAll(devices)
            statuses.add(status)
        } else {
            statuses.add(ControllerStatus(ControllerSource.OMADA, isConfigured = false))
        }

        val errorMsg = if (allDevices.isEmpty() && statuses.any { it.isConfigured && !it.isConnected }) {
            "Impossibile contattare uno o più controller"
        } else if (allDevices.isEmpty() && statuses.none { it.isConfigured }) {
            null // nessun controller configurato
        } else null

        // Ordinamento: offline/alerting/dormant/unknown/pending/online
        val sortedDevices = sortByStatusPriority(allDevices)

        // Usa il nome dell'organizzazione Meraki come titolo (se disponibile)
        val orgName = merakiOrgName ?: statuses.firstOrNull { it.isConfigured }?.source?.label

        _uiState.update {
            it.copy(
                devices = sortedDevices,
                isLoading = false,
                lastRefresh = System.currentTimeMillis(),
                controllerStatuses = statuses,
                errorMessage = errorMsg,
                organizationName = orgName
            )
        }
    }

    /**
     * Ordina i dispositivi mettendo in cima quelli offline/alerting/dormant,
     * poi unknown/pending, poi online in fondo.
     */
    private fun sortByStatusPriority(devices: List<ControllerDevice>): List<ControllerDevice> {
        val priority = mapOf(
            ControllerDeviceStatus.OFFLINE to 0,
            ControllerDeviceStatus.ALERTING to 1,
            ControllerDeviceStatus.DORMANT to 2,
            ControllerDeviceStatus.UNKNOWN to 3,
            ControllerDeviceStatus.PENDING to 4,
            ControllerDeviceStatus.ONLINE to 5
        )
        return devices.sortedBy { priority[it.status] ?: 99 }
    }

    // ── Meraki ──

    private suspend fun fetchMerakiDevices(
        apiKey: String,
        orgId: String
    ): Triple<List<ControllerDevice>, ControllerStatus, String?> {
        val client = MerakiApiClient(apiKey)

        return when (val orgsResult = client.getOrganizations()) {
            is MerakiResult.Error -> {
                Triple(emptyList<ControllerDevice>(), ControllerStatus(
                    ControllerSource.MERAKI, isConfigured = true, isConnected = false,
                    errorMessage = orgsResult.message
                ), null)
            }
            is MerakiResult.Success -> {
                val orgs = orgsResult.data
                if (orgs.isEmpty()) {
                    Triple(emptyList<ControllerDevice>(), ControllerStatus(
                        ControllerSource.MERAKI, isConfigured = true, isConnected = true,
                        deviceCount = 0, onlineCount = 0
                    ), null)
                } else {
                    val orgName = orgs.firstOrNull()?.name
                    // Colleziona dispositivi con il nome della rete di appartenenza
                    data class DeviceWithNetwork(val device: MerakiDevice, val networkName: String)
                    var allDevices = emptyList<DeviceWithNetwork>()
                    var lastError: String? = null

                    for (org in orgs) {
                        when (val netsResult = client.getNetworks(org.id)) {
                            is MerakiResult.Error -> { lastError = netsResult.message }
                            is MerakiResult.Success -> {
                                for (net in netsResult.data) {
                                    when (val devsResult = client.getDevices(net.id)) {
                                        is MerakiResult.Error -> { lastError = devsResult.message }
                                        is MerakiResult.Success -> {
                                            val withNetwork = devsResult.data.map {
                                                DeviceWithNetwork(it.copy(networkId = net.id), net.name)
                                            }
                                            allDevices = allDevices + withNetwork
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Recupera lo stato bulk via /organizations/{orgId}/devices/availabilities
                    val statusMap = mutableMapOf<String, String>()
                    for (org in orgs) {
                        when (val availResult = client.getDevicesAvailabilities(org.id)) {
                            is MerakiResult.Success -> {
                                availResult.data.forEach {
                                    if (it.serial.isNotBlank() && it.status.isNotBlank()) {
                                        statusMap[it.serial] = it.status.lowercase()
                                    }
                                }
                                android.util.Log.d("SeekerCtrl", "Availabilities: ${availResult.data.size} dispositivi, ${statusMap.size} con stato")
                            }
                            is MerakiResult.Error -> {
                                android.util.Log.w("SeekerCtrl", "Availabilities non disponibile: ${availResult.message}")
                            }
                        }
                    }

                    val controllerDevices = allDevices.map { (device, networkName) ->
                        val bulkStatus = statusMap[device.serial]
                        device.toControllerDevice(ControllerSource.MERAKI, bulkStatus, networkName)
                    }
                    val onlineCount = controllerDevices.count { it.status == ControllerDeviceStatus.ONLINE }

                    Triple(controllerDevices, ControllerStatus(
                        ControllerSource.MERAKI, isConfigured = true,
                        isConnected = lastError == null,
                        errorMessage = lastError,
                        deviceCount = controllerDevices.size,
                        onlineCount = onlineCount
                    ), orgName)
                }
            }
        }
    }

    // ── UniFi ──

    private suspend fun fetchUnifiDevices(
        url: String,
        username: String,
        password: String
    ): Pair<List<ControllerDevice>, ControllerStatus> {
        // Riutilizza client/repository se le credenziali non sono cambiate
        val creds = Triple(url, username, password)
        if (cachedUnifiCreds != creds) {
            cachedUnifiCreds = creds
            val client = UniFiApiClient(url, username, password)
            cachedUnifiClient = client
            cachedUnifiRepo = UniFiRepository(client)
        }
        val repo = cachedUnifiRepo ?: return emptyList<ControllerDevice>() to ControllerStatus(
            ControllerSource.UNIFI, isConfigured = true, isConnected = false,
            errorMessage = "Errore inizializzazione client"
        )

        return when (val result = repo.getDevices()) {
            is UniFiResult.Error -> {
                emptyList<ControllerDevice>() to ControllerStatus(
                    ControllerSource.UNIFI, isConfigured = true, isConnected = false,
                    errorMessage = result.message
                )
            }
            is UniFiResult.Success -> {
                val devices = result.data.map { it.toControllerDevice() }
                val onlineCount = devices.count { it.status == ControllerDeviceStatus.ONLINE }
                devices to ControllerStatus(
                    ControllerSource.UNIFI, isConfigured = true, isConnected = true,
                    deviceCount = devices.size, onlineCount = onlineCount
                )
            }
        }
    }

    // ── Omada ──

    private suspend fun fetchOmadaDevices(
        url: String,
        username: String,
        password: String
    ): Pair<List<ControllerDevice>, ControllerStatus> {
        // Riutilizza client/repository se le credenziali non sono cambiate
        val creds = Triple(url, username, password)
        if (cachedOmadaCreds != creds) {
            cachedOmadaCreds = creds
            val client = OmadaApiClient(url, username, password)
            cachedOmadaClient = client
            cachedOmadaRepo = OmadaRepository(client)
        }
        val repo = cachedOmadaRepo ?: return emptyList<ControllerDevice>() to ControllerStatus(
            ControllerSource.OMADA, isConfigured = true, isConnected = false,
            errorMessage = "Errore inizializzazione client"
        )

        return when (val result = repo.getDevices()) {
            is OmadaResult.Error -> {
                emptyList<ControllerDevice>() to ControllerStatus(
                    ControllerSource.OMADA, isConfigured = true, isConnected = false,
                    errorMessage = result.message
                )
            }
            is OmadaResult.Success -> {
                val devices = result.data.map { it.toControllerDevice() }
                val onlineCount = devices.count { it.status == ControllerDeviceStatus.ONLINE }
                devices to ControllerStatus(
                    ControllerSource.OMADA, isConfigured = true, isConnected = true,
                    deviceCount = devices.size, onlineCount = onlineCount
                )
            }
        }
    }

    // ── Dettaglio dispositivo ──

    private var detailPollJob: kotlinx.coroutines.Job? = null

    // Cache dei client/repository per evitare login ripetuti (ogni 10s)
    private var cachedUnifiClient: UniFiApiClient? = null
    private var cachedUnifiRepo: UniFiRepository? = null
    private var cachedUnifiCreds: Triple<String, String, String>? = null
    private var cachedOmadaClient: OmadaApiClient? = null
    private var cachedOmadaRepo: OmadaRepository? = null
    private var cachedOmadaCreds: Triple<String, String, String>? = null

    // ── Selezione organizzazione ──

    /**
     * Carica l'elenco delle organizzazioni Meraki.
     */
    fun loadMerakiOrganizations() {
        viewModelScope.launch {
            val apiKey = securePreferences.merakiApiKey.first()
            if (apiKey.isBlank()) return@launch
            val client = MerakiApiClient(apiKey)
            when (val result = client.getOrganizations()) {
                is MerakiResult.Success -> {
                    val orgs = result.data.map { OrgOption(it.id, it.name) }
                    _uiState.update { it.copy(merakiOrganizations = orgs) }
                }
                is MerakiResult.Error -> { /* silenzioso */ }
            }
        }
    }

    /**
     * Mostra/nasconde il selettore organizzazioni.
     */
    fun toggleOrgPicker() {
        _uiState.update { it.copy(showOrgPicker = !it.showOrgPicker) }
        if (_uiState.value.showOrgPicker && _uiState.value.merakiOrganizations.isEmpty()) {
            loadMerakiOrganizations()
        }
    }

    /**
     * Seleziona un'organizzazione e aggiorna le preferenze.
     */
    fun selectOrganization(org: OrgOption) {
        viewModelScope.launch {
            val apiKey = securePreferences.merakiApiKey.first()
            securePreferences.setMerakiCredentials(apiKey, org.id)
            _uiState.update { it.copy(showOrgPicker = false, organizationName = org.name) }
            // Ricarica i dispositivi con la nuova org
            refresh()
        }
    }

    /**
     * Seleziona un dispositivo e avvia il polling dei dettagli ogni 10 secondi.
     */
    fun selectDevice(device: ControllerDevice) {
        _uiState.update { it.copy(selectedDevice = device, deviceDetail = DeviceDetailState(isLoading = true)) }
        detailPollJob?.cancel()
        detailPollJob = viewModelScope.launch {
            while (true) {
                loadDeviceDetail(device)
                kotlinx.coroutines.delay(10_000L)
            }
        }
    }

    fun clearSelection() {
        detailPollJob?.cancel()
        detailPollJob = null
        _uiState.update { it.copy(selectedDevice = null, deviceDetail = DeviceDetailState()) }
    }

    /**
     * Carica i dettagli del dispositivo (ping, SSID, client).
     * Può essere chiamato più volte per aggiornare i dati.
     */
    private suspend fun loadDeviceDetail(device: ControllerDevice) {
        val mac = device.mac
        val serial = device.serial
        val firmware = device.firmware
        val model = device.model

        var ssids = emptyList<String>()
        var clients: Int? = null
        var clientList = emptyList<DeviceClient>()
        var pingMs: Long? = null
        var error: String? = null

        // Ping latenza (sempre se IP disponibile)
        if (device.ipAddress != null && device.ipAddress != "0.0.0.0") {
            val latencyMonitor = com.seeker.app.core.util.LatencyMonitor()
            pingMs = latencyMonitor.ping(device.ipAddress, 2000)
        }

        // Dettagli specifici per controller
        when (device.controllerSource) {
            ControllerSource.MERAKI -> {
                val result = loadMerakiDeviceDetail(device.serial ?: "", device.ipAddress)
                ssids = result.first
                clients = result.second.size
                clientList = result.second
                if (result.third != null) error = result.third
            }
            ControllerSource.UNIFI -> {
                val result = loadUnifiDeviceDetail(device.serial ?: "", device.mac ?: "", device.ipAddress)
                ssids = result.first
                clients = result.second.size
                clientList = result.second
            }
            ControllerSource.OMADA -> {
                val result = loadOmadaDeviceDetail(device.serial ?: "", device.mac ?: "", device.ipAddress)
                ssids = result.first
                clients = result.second.size
                clientList = result.second
            }
        }

        _uiState.update {
            it.copy(
                deviceDetail = DeviceDetailState(
                    isLoading = false,
                    mac = mac,
                    serial = serial,
                    firmware = firmware,
                    model = model,
                    ssids = ssids,
                    clients = clients,
                    clientList = clientList,
                    pingLatencyMs = pingMs,
                    error = error
                )
            )
        }
    }

    private suspend fun loadUnifiDeviceDetail(
        serial: String,
        mac: String,
        ipAddress: String?
    ): Triple<List<String>, List<DeviceClient>, String?> {
        val url = securePreferences.unifiUrl.first()
        val username = securePreferences.unifiUsername.first()
        val password = securePreferences.unifiPassword.first()
        if (url.isBlank()) return Triple(emptyList(), emptyList(), null)

        val client = com.seeker.app.data.unifi.UniFiApiClient(url, username, password)
        val repo = com.seeker.app.data.unifi.UniFiRepository(client)
        val deviceClients = mutableListOf<DeviceClient>()

        // Client connessi a questo AP
        if (mac.isNotBlank()) {
            when (val clResult = repo.getClientsByAp(mac)) {
                is com.seeker.app.data.unifi.UniFiResult.Success -> {
                    clResult.data.forEach { uc ->
                        deviceClients.add(
                            DeviceClient(
                                mac = uc.mac ?: "",
                                ip = uc.ip,
                                hostname = uc.hostname ?: uc.name,
                                ssid = uc.ssid,
                                rssi = uc.signal
                            )
                        )
                    }
                }
                is com.seeker.app.data.unifi.UniFiResult.Error -> {
                    android.util.Log.d("SeekerCtrl", "UniFi clients non disponibili per $serial: ${clResult.message}")
                }
            }
        }

        // SSIDs: prova a ottenere le WLAN configurate
        val ssids = emptyList<String>()  // TODO: endpoint WLAN config

        return Triple(ssids, deviceClients.toList(), null)
    }

    private suspend fun loadOmadaDeviceDetail(
        serial: String,
        mac: String,
        ipAddress: String?
    ): Triple<List<String>, List<DeviceClient>, String?> {
        val url = securePreferences.omadaUrl.first()
        val username = securePreferences.omadaUsername.first()
        val password = securePreferences.omadaPassword.first()
        if (url.isBlank()) return Triple(emptyList(), emptyList(), null)

        val client = com.seeker.app.data.omada.OmadaApiClient(url, username, password)
        val repo = com.seeker.app.data.omada.OmadaRepository(client)
        val deviceClients = mutableListOf<DeviceClient>()

        // Client connessi
        if (mac.isNotBlank()) {
            when (val clResult = repo.getClients()) {
                is com.seeker.app.data.omada.OmadaResult.Success -> {
                    clResult.data.filter { it.deviceId == serial || it.mac == mac }.forEach { oc ->
                        deviceClients.add(
                            DeviceClient(
                                mac = oc.mac ?: "",
                                ip = oc.ip,
                                hostname = oc.hostname ?: oc.name,
                                ssid = oc.ssid,
                                rssi = oc.signal
                            )
                        )
                    }
                }
                is com.seeker.app.data.omada.OmadaResult.Error -> {
                    android.util.Log.d("SeekerCtrl", "Omada clients non disponibili: ${clResult.message}")
                }
            }
        }

        // SSIDs: dalle radioInfos del dispositivo o WLAN config
        val ssids = emptyList<String>()  // TODO: endpoint SSID config

        return Triple(ssids, deviceClients.toList(), null)
    }

    private suspend fun loadMerakiDeviceDetail(
        serial: String,
        ipAddress: String?
    ): Triple<List<String>, List<DeviceClient>, String?> {
        val apiKey = securePreferences.merakiApiKey.first()
        if (apiKey.isBlank() || serial.isBlank()) return Triple(emptyList(), emptyList(), null)

        // Recupera il modello per sapere se è un AP wireless
        val model = _uiState.value.selectedDevice?.model ?: ""
        val isWireless = model.startsWith("MR")

        val client = MerakiApiClient(apiKey)
        var errorMsg: String? = null
        val ssids = mutableListOf<String>()
        val deviceClients = mutableListOf<DeviceClient>()

        // Wireless status (SSID) — solo per AP (MR series)
        if (isWireless) {
            when (val wsResult = client.getWirelessStatus(serial)) {
                is MerakiResult.Success -> {
                    wsResult.data.basicServiceSets.forEach { bss ->
                        val name = bss.ssid.trim()
                        if (name.isNotBlank()
                            && name !in ssids
                            && !name.contains("unconfigured", ignoreCase = true)
                            && !name.startsWith("_", ignoreCase = true)
                        ) {
                            ssids.add(name)
                        }
                    }
                }
                is MerakiResult.Error -> {
                    android.util.Log.d("SeekerCtrl", "Wireless status non disponibile per $serial: ${wsResult.message}")
                }
            }
        } else {
            android.util.Log.d("SeekerCtrl", "$serial non è un AP wireless (modello=$model) — salto wireless status")
        }

        // Clients connessi — disponibile per AP, switch e alcuni altri
        when (val clResult = client.getDeviceClients(serial)) {
            is MerakiResult.Success -> {
                clResult.data.forEach { mc ->
                    deviceClients.add(
                        DeviceClient(
                            mac = mc.mac ?: "",
                            ip = mc.ip,
                            hostname = mc.description,
                            ssid = mc.ssid,
                            rssi = mc.rssi
                        )
                    )
                }
            }
            is MerakiResult.Error -> {
                android.util.Log.d("SeekerCtrl", "Clients non disponibili per $serial: ${clResult.message}")
            }
        }

        return Triple(ssids.toList(), deviceClients.toList(), errorMsg)
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}

// ── Mappers ──

private fun MerakiDevice.toControllerDevice(
    source: ControllerSource,
    bulkStatus: String? = null,
    networkName: String? = null
): ControllerDevice {
    val effectiveStatus = bulkStatus?.takeIf { it.isNotBlank() }
        ?: this.status?.trim()?.lowercase()

    val status = when (effectiveStatus) {
        "online" -> ControllerDeviceStatus.ONLINE
        "offline" -> ControllerDeviceStatus.OFFLINE
        "alerting" -> ControllerDeviceStatus.ALERTING
        "dormant" -> ControllerDeviceStatus.DORMANT
        "active" -> ControllerDeviceStatus.ONLINE
        "inactive" -> ControllerDeviceStatus.OFFLINE
        "disconnected" -> ControllerDeviceStatus.OFFLINE
        "unknown" -> ControllerDeviceStatus.UNKNOWN
        else -> {
            if (effectiveStatus == null) {
                android.util.Log.d("SeekerCtrl", "Meraki status non disponibile per $name ($serial)")
            } else if (effectiveStatus == "") {
                android.util.Log.d("SeekerCtrl", "Meraki status vuoto per $name ($serial)")
            } else {
                android.util.Log.w("SeekerCtrl", "Meraki status sconosciuto: '$effectiveStatus' per $name ($serial)")
            }
            ControllerDeviceStatus.UNKNOWN
        }
    }
    return ControllerDevice(
        name = name,
        model = model,
        serial = serial,
        mac = mac,
        ipAddress = lanIp ?: publicIp,
        firmware = firmware,
        status = status,
        controllerSource = source,
        networkName = networkName,
        tags = tags
    )
}

private fun UniFiDevice.toControllerDevice(): ControllerDevice {
    val status = when (state) {
        1 -> ControllerDeviceStatus.ONLINE
        0 -> ControllerDeviceStatus.OFFLINE
        2 -> ControllerDeviceStatus.PENDING
        else -> ControllerDeviceStatus.UNKNOWN
    }
    return ControllerDevice(
        name = name ?: "",
        model = model,
        serial = serial,
        mac = mac,
        ipAddress = ip,
        firmware = version,
        status = status,
        controllerSource = ControllerSource.UNIFI,
        radioChannels = allRadios.mapNotNull { it.channel },
        uptime = uptime?.let { it / 1000 } // ms → s
    )
}

private fun OmadaDevice.toControllerDevice(): ControllerDevice {
    val status = when (status) {
        1 -> ControllerDeviceStatus.ONLINE
        0 -> ControllerDeviceStatus.OFFLINE
        -1 -> ControllerDeviceStatus.PENDING
        2 -> ControllerDeviceStatus.PENDING
        else -> ControllerDeviceStatus.UNKNOWN
    }
    return ControllerDevice(
        name = name ?: "",
        model = model,
        serial = serial,
        mac = mac,
        ipAddress = ip,
        firmware = version,
        status = status,
        controllerSource = ControllerSource.OMADA,
        clients = clients,
        radioChannels = radioInfos?.mapNotNull { it.channel } ?: emptyList(),
        uptime = uptime
    )
}

private fun <T> MutableStateFlow<T>.update(block: (T) -> T) {
    value = block(value)
}
